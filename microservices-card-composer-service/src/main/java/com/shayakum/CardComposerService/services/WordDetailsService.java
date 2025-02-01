package com.shayakum.CardComposerService.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shayakum.CardComposerService.dto.ListOfWordsDTO;
import com.shayakum.CardComposerService.models.ListOfWords;
import com.shayakum.CardComposerService.models.Word;
import com.shayakum.CardComposerService.repositories.WordsRepository;
import com.shayakum.CardComposerService.utils.CreateRequest;
import com.shayakum.CardComposerService.utils.resources.YamlPropertySourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = false)
@PropertySource(value = "classpath:auth.yml", factory = YamlPropertySourceFactory.class)
public class WordDetailsService {
    @Value("${translator.url}")
    private String translateUrl;
    @Value("${dictionaryDef.url}")
    private String definitionsUrl;

    private final WordsRepository wordsRepository;
    private final CreateRequest createRequest;
    private final KafkaProducerService kafkaProducerService;
    private final Logger logger = LoggerFactory.getLogger(WordDetailsService.class);
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public WordDetailsService(WordsRepository wordsRepository, CreateRequest createRequest, KafkaProducerService kafkaProducerService) {
        this.wordsRepository = wordsRepository;
        this.createRequest = createRequest;
        this.kafkaProducerService = kafkaProducerService;
    }

    public ListOfWords runTranslate(ListOfWordsDTO listOfWordsDTO) {
        ListOfWords listOfWords = new ListOfWords();
        listOfWords.setOnlyTranslate(listOfWordsDTO.isOnlyTranslate());

        listOfWords.setListOfWords(splitWords(listOfWordsDTO));
        logger.info("Words from list have been split");
        translateWords(listOfWords);
        logger.info("List of words has finished procedure of translation");
        listOfWords.setRandomId();

        try {
            listOfWords.getTranslatedWords().forEach(obj -> kafkaProducerService.produceMessage(obj.getWord() + "::" + listOfWords.getId()));
        } catch (Exception e) {
            logger.error("We have got an Exception during sending message to Kafka Topic [ImageHandlerService]. Data will probably get their images after some time");
            if (!listOfWords.isOnlyTranslate()) {
                throw new RuntimeException();
            }
        } finally {
            return listOfWords;
        }
    }

    protected List<String> splitWords(ListOfWordsDTO words) {
        String[] splitWordsArray = words.getListOfWords().split(";");
        List<String> splittedWords = Arrays.stream(splitWordsArray)
                .filter(word -> !word.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        if (splittedWords.isEmpty()) {
            throw new IllegalArgumentException("List of words is empty.");
        } else {
            return splittedWords;
        }
    }

    private ListOfWords translateWords(ListOfWords listOfWords) {
        logger.info("Starting to translate words in the list");
        Map<String, String> jsonToSend = new HashMap<>();

        for (String word : listOfWords.getListOfWords()) {
            if (!isExists(word)) {
                try {
                    jsonToSend.put("q", word);
                    jsonToSend.put("source", "en");
                    jsonToSend.put("target", "ru");
                    jsonToSend.put("format", "text");
                    jsonToSend.put("alternatives", "0");
                    jsonToSend.put("api_key", "");

                    String response = createRequest.postForObject(jsonToSend, translateUrl);
                    JsonNode jsonNode = objectMapper.readTree(response);

                    Word newWord = new Word();
                    newWord.setWord(word);
                    newWord.setTranslation(jsonNode.get("translatedText").asText());
                    newWord = getDefinitions(newWord);

                    logger.info("Word: " + word.toUpperCase() + " — Has got all definitions and it's going to be saved to a list");

                    listOfWords.getTranslatedWords().add(newWord);
                } catch (Exception e) {
                    logger.error("Have got a error during connection to API's to get definitions or translation or return value is null", e);
                    Word corruptedWord = new Word();
                    corruptedWord.setWord(word);
                    listOfWords.getCorruptedWords().add(corruptedWord);
                }
            } else {
                listOfWords.getWordsAreExist().add(findByWord(word));
                logger.info("Word: " + word.toUpperCase() + " — Word is already exists so it was skipped and added to a separate list");
            }
        }
        saveWords(listOfWords);

        return listOfWords;
    }

    private Word getDefinitions(Word translatedWord) throws JsonProcessingException {
        String url = definitionsUrl + translatedWord.getWord();
        String response = createRequest.getForObject(url);
        JsonNode jsonNode = objectMapper.readTree(response);

        translatedWord.setTranscription(jsonNode.get(0).get("phonetic").asText());
        translatedWord.setMeaning(jsonNode.get(0)
                .get("meanings").get(0)
                .get("definitions").get(0)
                .get("definition").asText());

        return translatedWord;
    }

    private void saveWords(ListOfWords translatedWords) {
        List<Word> validWords = new ArrayList<>();
        List<Word> corruptedWords = new ArrayList<>();

        for (Word word : translatedWords.getTranslatedWords()) {
            if (word.getMeaning() != null && word.getTranslation() != null && word.getTranscription() != null) {
                validWords.add(word);
            } else {
                logger.warn("Word is corrupted: " + word.getWord());
                corruptedWords.add(word);
            }
        }

        translatedWords.getTranslatedWords().removeAll(corruptedWords);
        translatedWords.getCorruptedWords().addAll(corruptedWords);

        if (!validWords.isEmpty()) {
            try {
                logger.info("Saving " + validWords.size() + " valid words into the database.");
                wordsRepository.saveAll(validWords); // Save in batch
            } catch (Exception e) {
                logger.error("Error occurred while saving words in batch.", e);
                throw new RuntimeException();
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean isExists(String word) {
        boolean isExists = wordsRepository.existsByWord(word);

        if (isExists) {
            logger.info("Word: " + word.toUpperCase() + " — Is already exists in a Database");
        } else {
            logger.info("Word: " + word.toUpperCase() + " — Isn't present in a Database");
        }

        return isExists;
    }

    @Transactional(readOnly = true)
    public Word findByWord(String word) {
        return findByWord(word);
    }
}
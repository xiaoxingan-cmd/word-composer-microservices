package com.shayakum.CardComposerService.services;

import com.shayakum.CardComposerService.models.ListOfWords;
import com.shayakum.CardComposerService.models.Word;
import com.shayakum.CardComposerService.repositories.WordsRepository;
import com.shayakum.CardComposerService.utils.patterns.obs.Observer;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class OrderAssemblerService implements Observer {
    private final ListOfWords listOfWords;
    private final int id;
    private boolean isReady = false;
    private int arraySize;
    private int countOfWordsWasHandled = 0;

    WordsRepository wordsRepository;
    private final Logger logger = LoggerFactory.getLogger(OrderAssemblerService.class);

    public OrderAssemblerService(ListOfWords listOfWords, int id, WordsRepository wordsRepository) {
        this.listOfWords = listOfWords;
        this.id = id;
        this.wordsRepository = wordsRepository;
        this.arraySize = listOfWords.getTranslatedWords().size();
    }

    @Override
    public void update(String message) {
        String value = message.substring(0, message.indexOf("::"));
        String status = message.substring(message.indexOf("::") + 2, message.lastIndexOf("::"));
        int messageId = Integer.parseInt(message.substring(message.lastIndexOf("::") + 2));

        logger.info("Received update: {} {} {}", value, status, messageId);

        if (id == messageId) {
            logger.info("ID == MESSAGE_ID {}", id);
            switch (status) {
                case "SUCCESS":
                    for (Word word : listOfWords.getTranslatedWords()) {
                        if(word.getWord().equals(value.toLowerCase())) {
                            Word updatedWord = wordsRepository.findByWord(word.getWord());

                            word.setImageHasFoundInS3(updatedWord.isImageHasFoundInS3());
                            word.setImageHasCardRepresentationInS3(updatedWord.isImageHasCardRepresentationInS3());
                            word.setUrl(updatedWord.getUrl());

                            countOfWordsWasHandled++;
                            break;
                        }
                    }
                    break;
                case "FAILED":
                    for (Word word : listOfWords.getTranslatedWords()) {
                        if (word.getWord().equals(value)) {
                            listOfWords.getCorruptedWords().add(word);
                            listOfWords.getTranslatedWords().remove(word);
                            countOfWordsWasHandled++;
                            break;
                        }
                    }
                    break;
            }
        }

        if (countOfWordsWasHandled == arraySize) {
            isReady = true;
        }
    }
}

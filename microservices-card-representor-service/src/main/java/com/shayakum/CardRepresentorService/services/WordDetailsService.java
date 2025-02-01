package com.shayakum.CardRepresentorService.services;

import com.shayakum.CardRepresentorService.models.Word;
import com.shayakum.CardRepresentorService.repositories.WordsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WordDetailsService {
    private final WordsRepository wordsRepository;
    private final Logger logger = LoggerFactory.getLogger(WordDetailsService.class);

    @Autowired
    public WordDetailsService(WordsRepository wordsRepository) {
        this.wordsRepository = wordsRepository;
    }

    @Transactional(readOnly = true)
    public Word findByWord(String word) {
        return wordsRepository.findByWord(word);
    }

    @Transactional(readOnly = false)
    public boolean setCardValues(String query, boolean value, String url) {
        try {
            Word foundedWord = findByWord(query);
            foundedWord.setImageHasCardRepresentationInS3(value);
            foundedWord.setUrl(url);
            wordsRepository.save(foundedWord);
            return true;
        } catch (Exception e) {
            logger.error("Have got an error during changing 'S3' and 'Url' values in a Database", e);
            return false;
        }
    }
}

package com.shayakum.ImageHandlerService.services;

import com.shayakum.ImageHandlerService.models.Word;
import com.shayakum.ImageHandlerService.repositories.WordsRepository;
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
    private Word findByWord(String word) {
        return wordsRepository.findByWord(word);
    }

    @Transactional(readOnly = false)
    public boolean setS3Value(String query, boolean value) {
        try {
            Word foundedWord = findByWord(query);
            foundedWord.setImageHasFoundInS3(value);
            wordsRepository.save(foundedWord);
            return true;
        } catch (Exception e) {
            logger.error("Have got an error during changing 'S3Value' in a Database", e);
            return false;
        }
    }
}

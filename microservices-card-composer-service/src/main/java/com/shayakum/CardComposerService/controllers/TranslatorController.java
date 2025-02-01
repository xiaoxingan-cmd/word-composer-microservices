package com.shayakum.CardComposerService.controllers;

import com.shayakum.CardComposerService.dto.ListOfWordsDTO;
import com.shayakum.CardComposerService.exceptions.OrderAssemblerTimeoutException;
import com.shayakum.CardComposerService.models.ListOfWords;
import com.shayakum.CardComposerService.repositories.WordsRepository;
import com.shayakum.CardComposerService.services.OrderAssemblerService;
import com.shayakum.CardComposerService.services.PrepareConsumerService;
import com.shayakum.CardComposerService.services.WordDetailsService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeoutException;

@RestController
public class TranslatorController {
    private final WordDetailsService wordDetailsService;
    private final PrepareConsumerService prepareConsumerService;
    private final WordsRepository wordsRepository;
    private final Logger logger = LoggerFactory.getLogger(TranslatorController.class);

    @Autowired
    public TranslatorController(WordDetailsService wordDetailsService, PrepareConsumerService prepareConsumerService, WordsRepository wordsRepository) {
        this.wordDetailsService = wordDetailsService;
        this.prepareConsumerService = prepareConsumerService;
        this.wordsRepository = wordsRepository;
    }

    @PostMapping("/translate")
    public ResponseEntity<Object> wordHandler(@RequestBody @Valid ListOfWordsDTO listOfWordsDTO, BindingResult bindingResult) throws TimeoutException {
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Validation failed");

            return ResponseEntity.badRequest().body("Bad request: " + errorMessage);
        }

        logger.info("Have received a request to translate list of words: " + listOfWordsDTO.toString());
        ListOfWords requestedWords = wordDetailsService.runTranslate(listOfWordsDTO);

        if (!listOfWordsDTO.isOnlyTranslate()) {
            logger.info("OrderAssembler has started to collect data");
            OrderAssemblerService orderAssembler = new OrderAssemblerService(requestedWords, requestedWords.getId(), wordsRepository);
            prepareConsumerService.addObserver(orderAssembler);

            long startTime = System.currentTimeMillis();
            long timeout = 90000;
            while (!orderAssembler.isReady()) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    logger.error("OrderAssembler reached a timeout");
                    throw new OrderAssemblerTimeoutException();
                }
            }

            logger.info("OrderAssembler has finished its work");
            prepareConsumerService.removeObserver(orderAssembler);
            return new ResponseEntity<>(orderAssembler.getListOfWords(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(requestedWords, HttpStatus.ACCEPTED);
        }
    }
}
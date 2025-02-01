package com.shayakum.CardComposerService.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListOfWords {
    private int id = 0;
    private List<String> listOfWords = new ArrayList<>();
    private List<Word> translatedWords = new ArrayList<>();
    private List<Word> wordsAreExist = new ArrayList<>();
    private List<Word> corruptedWords = new ArrayList<>();
    private boolean onlyTranslate = true;

    public void setRandomId() {
        Random random = new Random();
        setId(random.nextInt(1000000));
    }
}
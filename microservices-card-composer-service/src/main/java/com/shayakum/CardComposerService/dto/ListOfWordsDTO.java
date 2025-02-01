package com.shayakum.CardComposerService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListOfWordsDTO {
    @NotBlank
    private String listOfWords;

    @NotNull
    private boolean onlyTranslate;

    @Override
    public String toString() {
        return "ListOfWordsDTO{" +
                "listOfWords='" + listOfWords + '\'' +
                ", onlyTranslate=" + onlyTranslate +
                '}';
    }
}

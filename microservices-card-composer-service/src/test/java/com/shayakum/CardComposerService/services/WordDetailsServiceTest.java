package com.shayakum.CardComposerService.services;

import com.shayakum.CardComposerService.dto.ListOfWordsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
class WordDetailsServiceTest {
    @Mock
    private ListOfWordsDTO listOfWordsDTO;
    @InjectMocks
    private WordDetailsService wordDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initializes @Mock and @InjectMocks
    }

    @Test
    void splitWordsWorksCorrectlyWithStandartFormat() {
        when(listOfWordsDTO.getListOfWords()).thenReturn("test;test;");
        List<String> expectedResult = List.of("test", "test");

        assertEquals(expectedResult, wordDetailsService.splitWords(listOfWordsDTO));
    }

    @Test
    void splitWordsWorksCorrectlyWithSemicolonFirst() {
        when(listOfWordsDTO.getListOfWords()).thenReturn(";test;test;");
        List<String> expectedResult = List.of("test", "test");

        assertEquals(expectedResult, wordDetailsService.splitWords(listOfWordsDTO));
    }

    @Test
    void splitWordsWorksCorrectlyWithDoubleSemicolonFirst() {
        when(listOfWordsDTO.getListOfWords()).thenReturn(";;test;test;");
        List<String> expectedResult = List.of("test", "test");

        assertEquals(expectedResult, wordDetailsService.splitWords(listOfWordsDTO));
    }

    @Test
    void splitWordsWorksCorrectlyWithoutSemicolonEnd() {
        when(listOfWordsDTO.getListOfWords()).thenReturn("test;test");
        List<String> expectedResult = List.of("test", "test");

        assertEquals(expectedResult, wordDetailsService.splitWords(listOfWordsDTO));
    }

    @Test
    void splitWordsThrowsAnExceptionWithoutData() {
        when(listOfWordsDTO.getListOfWords()).thenReturn("");

        assertThrows(IllegalArgumentException.class, () -> {
            wordDetailsService.splitWords(listOfWordsDTO);
        });
    }

    @Test
    void splitWordsThrowsAnExceptionWithOnlySemicolon() {
        when(listOfWordsDTO.getListOfWords()).thenReturn(";;");

        assertThrows(IllegalArgumentException.class, () -> {
            wordDetailsService.splitWords(listOfWordsDTO);
        });
    }
}
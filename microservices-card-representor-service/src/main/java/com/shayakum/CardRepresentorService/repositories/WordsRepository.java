package com.shayakum.CardRepresentorService.repositories;

import com.shayakum.CardRepresentorService.models.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface WordsRepository extends JpaRepository<Word, Long> {
    Word findByWord(String word);
    @Transactional(readOnly = true)
    @Query(value = "SELECT * FROM word_details WHERE image_has_card_representation_in_s3 = FALSE", nativeQuery = true)
    List<Word> findAllWordsWithRepresentationInS3False();
}

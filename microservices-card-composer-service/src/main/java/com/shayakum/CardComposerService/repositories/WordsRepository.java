package com.shayakum.CardComposerService.repositories;

import com.shayakum.CardComposerService.models.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface WordsRepository extends JpaRepository<Word, Long> {
    boolean existsByWord(String word);
    Word findByWord(String word);
    @Modifying
    @Transactional(readOnly = false)
    @Query(value = "DELETE FROM word_details WHERE word = ?1", nativeQuery = true)
    void deleteByWord(String word);
}

package com.shayakum.ImageHandlerService.repositories;

import com.shayakum.ImageHandlerService.models.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordsRepository extends JpaRepository<Word, Long> {
    Word findByWord(String word);
}

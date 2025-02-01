package com.shayakum.CardRepresentorService.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "word_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Word {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "word")
    private String word;

    @Column(name = "translation")
    private String translation;

    @Column(name = "transcription")
    private String transcription;

    @Column(name = "meaning")
    private String meaning;

    @Column(name = "image_has_found_in_s3")
    private boolean imageHasFoundInS3;

    @Column(name = "image_has_card_representation_in_s3")
    private boolean imageHasCardRepresentationInS3;

    @Column(name = "url")
    private String url;
}

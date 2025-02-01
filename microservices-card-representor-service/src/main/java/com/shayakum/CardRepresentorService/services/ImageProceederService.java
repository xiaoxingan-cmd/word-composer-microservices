package com.shayakum.CardRepresentorService.services;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.shayakum.CardRepresentorService.models.Word;
import com.shayakum.CardRepresentorService.utils.KafkaRequest;
import com.shayakum.CardRepresentorService.utils.ReceivedRecord;
import com.shayakum.CardRepresentorService.utils.enums.ObjectStatus;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import jakarta.annotation.PostConstruct;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImageProceederService {
    private final WordDetailsService wordDetailsService;
    private final AWSS3Service awss3Service;
    private final KafkaProducerService kafkaProducerService;
    private final PublicImageService publicImageService;
    private ImageProcessor imageProcessorTemplate;
    private final KafkaRequest kafkaRequest;
    private final Logger logger = LoggerFactory.getLogger(ImageProceederService.class);
    private int templateWidth = 300;
    private int templateHeight = 500;

    @Autowired
    public ImageProceederService(WordDetailsService wordDetailsService, AWSS3Service awss3Service, KafkaProducerService kafkaProducerService, PublicImageService publicImageService, KafkaRequest kafkaRequest) {
        this.wordDetailsService = wordDetailsService;
        this.awss3Service = awss3Service;
        this.kafkaProducerService = kafkaProducerService;
        this.publicImageService = publicImageService;
        this.kafkaRequest = kafkaRequest;
    }

    public void runProceeder(ReceivedRecord record, String base_url, String fromTopic, String value, String id) throws HttpException {
        Word word = wordDetailsService.findByWord(value);
        try {
            logger.info("Starting to handle an image...");

            ByteArrayInputStream inputStream = new ByteArrayInputStream(awss3Service.downloadObject(word.getWord().trim()));
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            ColorProcessor originalProcessor = new ColorProcessor(bufferedImage);
            ImagePlus image = new ImagePlus(word.getWord(), originalProcessor);

            image = prepareImage(image);
            ImageProcessor processor = insertImageToTemplate(image);
            drawCenteredText(processor, word.getWord(), templateWidth, templateHeight, 160, Font.BOLD, 24); // EDITED
            drawCenteredText(processor, word.getTranscription(), templateWidth, templateHeight, 200, Font.ITALIC, 18); // EDITED
            drawCenteredText(processor, word.getTranslation(), templateWidth, templateHeight, 325, Font.BOLD, 24); // EDITED
            drawCenteredText(processor, word.getMeaning(), templateWidth, templateHeight, 450, Font.ITALIC, 14);
            ImagePlus result = new ImagePlus(word.getWord(), processor);

            uploadCard(result);
            commitOffset(record, fromTopic, base_url);
            kafkaProducerService.produceMessage(kafkaProducerService.kafkaTopic, word.getWord(), ObjectStatus.SUCCESS, id);
        } catch (Exception e) {
            logger.error("Have got an error during handling an image", e);
            commitOffset(record, fromTopic, base_url);
            kafkaProducerService.produceMessage(kafkaProducerService.kafkaTopic, word.getWord(), ObjectStatus.FAILED, id);
        }
    }

    private ImagePlus prepareImage(ImagePlus image) {
        ImageProcessor processor = image.getProcessor();

        int originalWidth = processor.getWidth();
        int originalHeight = processor.getHeight();

        int cropWidth = 250;
        int cropHeight = 250;

        int x = (originalWidth - cropWidth) / 2;
        int y = (originalHeight - cropHeight) / 2;

        processor.setRoi(x, y, cropWidth, cropHeight);
        ImageProcessor croppedProcessor = processor.crop();

        ImagePlus croppedImage = new ImagePlus("Cropped Image", croppedProcessor);
        return croppedImage;
    }

    private ImageProcessor insertImageToTemplate(ImagePlus image) {
        ImageProcessor processor = imageProcessorTemplate.duplicate();

        int imageX = 25;
        int imageY = 25;
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        processor.insert(image.getProcessor(), imageX, imageY);

        int borderThickness = 4;
        processor.setColor(0x000000); // Чёрный цвет
        for (int i = 0; i < borderThickness; i++) {
            processor.drawRect(imageX - i, imageY - i, imageWidth + i * 2, imageHeight + i * 2);
        }

        return processor;
    }

    private void drawCenteredText(ImageProcessor processor, String text, int width, int height, int margin, int fontType, int fontSize) {
        processor.setFont(new Font("Arial", fontType, fontSize));
        processor.setColor(0x000000);

        // Максимальная ширина текста на карточке
        int maxTextWidth = width - 20; // Добавляем отступы по краям

        // Разбиваем текст на строки
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (String word : words) {
            // Проверяем, помещается ли текущая строка с добавленным словом
            String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            int testWidth = processor.getStringWidth(testLine);

            if (testWidth > maxTextWidth) {
                // Если не помещается, сохраняем текущую строку и начинаем новую
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                // Если помещается, добавляем слово к текущей строке
                currentLine.append(" ").append(word);
            }
        }
        // Добавляем последнюю строку
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        // Вычисляем высоту текста
        int lineHeight = processor.getFontMetrics().getHeight();
        int totalTextHeight = lines.size() * lineHeight;

        // Определяем начальную позицию для вертикального центрирования текста
        int yStart = (height + margin - totalTextHeight) / 2;

        // Рисуем каждую строку
        for (String line : lines) {
            int textWidth = processor.getStringWidth(line);
            int x = (width - textWidth) / 2;
            processor.drawString(line, x, yStart);
            yStart += lineHeight; // Переход на следующую строку
        }
    }

    private boolean uploadCard(ImagePlus image) throws IOException {
        String fileFormat = "jpg";

        BufferedImage rawImage = image.getBufferedImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(rawImage, fileFormat, baos);
        byte[] imageInByte = baos.toByteArray();
        ByteArrayInputStream imageStream = new ByteArrayInputStream(imageInByte);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + fileFormat);
        metadata.setContentLength(imageInByte.length);

        String base64String = Base64.getEncoder().encodeToString(imageInByte);

        try {
            logger.info("Uploading object in a S3Container...");
            awss3Service.uploadObject(image.getTitle(), imageStream, metadata);
            String url = publicImageService.publicImage(base64String);
            logger.info("Changing 'S3' and 'Url' values in a Database...");
            return wordDetailsService.setCardValues(image.getTitle(), true, url);
        } catch (Exception e) {
            logger.error("Have got an error while working with a S3Container or Database", e);
            return false;
        }
    }

    @PostConstruct
    private void prepareTemplate() {
        ImageProcessor processor = new ColorProcessor(templateWidth, templateHeight);

        processor.setValue(0xefefed);
        processor.fill();
        imageProcessorTemplate = processor;
    }

    private HttpStatusCode commitOffset(ReceivedRecord record, String commitTopic, String base_url) throws HttpException {
        Map<String, Object> offsetData = new HashMap<>();
        offsetData.put("topic", commitTopic);
        offsetData.put("partition", record.getPartition());
        offsetData.put("offset", record.getOffset());

        Map<String, Object> jsonToSend = Map.of(
                "offsets", List.of(offsetData)
        );

        try {
            ResponseEntity<String> response =
                    kafkaRequest.postForEntity(
                            jsonToSend,
                            base_url + "/offsets",
                            kafkaRequest.CONFIGURE_AND_SUBSCRIBE_HEADER);

            return response.getStatusCode();
        } catch (Exception e) {
            logger.error("Have got an error during committing an offset: " + record + ", " + commitTopic, e);
            throw new HttpException();
        }
    }
}

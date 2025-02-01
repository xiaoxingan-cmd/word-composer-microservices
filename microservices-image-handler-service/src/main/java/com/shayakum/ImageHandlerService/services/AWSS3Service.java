package com.shayakum.ImageHandlerService.services;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.shayakum.ImageHandlerService.utils.resources.YamlPropertySourceFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@PropertySource(value = "classpath:auth.yml", factory = YamlPropertySourceFactory.class)
public class AWSS3Service {
    @Value("${s3.apiKey}")
    private String apiKey;
    @Value("${s3.secret}")
    private String secret;
    @Value("${s3.bucketName}")
    private String bucketName;
    @Value("${s3.serviceEndpoint}")
    private String serviceEndpoint;
    @Value("${s3.region}")
    private String region;

    private AmazonS3 s3client;

    // Configure S3 client connection
    @PostConstruct
    public void init() {
        AWSCredentials credentials = new BasicAWSCredentials(apiKey, secret);
        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, region);

        s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpoint)
                .build();
    }

    // Create bucket
    public void checkIfBucketExist() {
        if (!s3client.doesBucketExistV2(bucketName)) {
            s3client.createBucket(bucketName);
        }
    }

    // Upload object
    public void uploadObject(String objectName, InputStream imageObject, ObjectMetadata objectMetadata) {
        s3client.putObject(bucketName, objectName, imageObject, objectMetadata);
    }

    // Download object
    public void downloadObject(String objectName) throws IOException {
        S3Object s3object = s3client.getObject(bucketName, objectName);
        S3ObjectInputStream inputStream = s3object.getObjectContent();

        inputStream.transferTo(new FileOutputStream("downloaded-object"));
    }

    // Delete object
    public void deleteObject(String objectName) {
        s3client.deleteObject(bucketName, objectName);
    }

    // Delete bucket
    public void deleteBucket() {
        s3client.deleteBucket(bucketName);
    }
}

package com.example.demo.services;

import com.example.demo.models.FileDetailsModel;
import eu.europa.esig.dss.model.DSSDocument;
import io.minio.StatObjectResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class MinIOService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    private String bucketName = "bucket";

    public MinIOService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String uploadDSSDocument(DSSDocument document, String id) {
        String fileName = document.getName();

        try (InputStream inputStream = document.openStream()) {
            byte[] content = readContent(inputStream);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName + id)
                    .key(fileName)
                    .contentType("application/octet-stream")
                    .contentLength((long) content.length)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(content)
            );

            return fileName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload DSSDocument", e);
        }
    }


    public String uploadFile(DSSDocument document, String id, String fileUUID) {
        String originalFilename = document.getName();
        String file_extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = fileUUID + file_extension;

        Map<String, String> metadata = new HashMap<>();
        metadata.put("original-filename", URLEncoder.encode(originalFilename, StandardCharsets.UTF_8));


        try (InputStream inputStream = document.openStream()) {
            byte[] content = readContent(inputStream);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName + id)
                    .key(fileName)
                    .contentType("application/octet-stream")
                    .contentLength((long) content.length)
                    .metadata(metadata)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(content)
            );

            return fileUUID;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload DSSDocument", e);
        }
    }

    public String uploadMultipartFileThreadSafe(byte[] fileBytes, String fileOriginalName, String bucketId, String fileUUID) {
        String fileExtension = fileOriginalName.substring(fileOriginalName.lastIndexOf('.'));
        String finalFileName = fileUUID + fileExtension;

        Map<String, String> metadata = new HashMap<>();
        metadata.put("original-filename", URLEncoder.encode(fileOriginalName, StandardCharsets.UTF_8));

        Path tempFilePath = null;
        try {
            tempFilePath = Files.createTempFile("upload-", fileExtension);
            Files.write(tempFilePath, fileBytes);

            File tempFile = tempFilePath.toFile();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName + bucketId)
                    .key(finalFileName)
                    .contentType("application/octet-stream")
                    .contentLength(tempFile.length())
                    .metadata(metadata)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromFile(tempFile)
            );

            return finalFileName;

        } catch (Exception e) {
            System.err.println("Error uploading file to MinIO: " + e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
        } finally {
            if (tempFilePath != null) {
                try {
                    Files.deleteIfExists(tempFilePath);
                } catch (IOException e) {
                    System.err.println("Could not delete temp file: " + e.getMessage());
                }
            }
        }
    }

    public String uploadMultipartFile(MultipartFile document, String id, String fileUUID) {
        String originalFilename = document.getOriginalFilename();
        String file_extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = fileUUID + file_extension;

        Map<String, String> metadata = new HashMap<>();
        metadata.put("original-filename", URLEncoder.encode(document.getOriginalFilename(), StandardCharsets.UTF_8));

        try (InputStream inputStream = document.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName + id)
                    .key(fileName)
                    .contentType("application/octet-stream")
                    .contentLength((long) document.getSize())
                    .metadata(metadata)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(inputStream, document.getSize())
            );

            return fileUUID;

        } catch (Exception e) {
            System.err.println("Error uploading file to MinIO: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to upload DSSDocument", e);
        }
    }

    private byte[] readContent(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }

    public String getPresignedUrl(String fileId, String bucketId, String filename) throws Exception {
        try {
            String bucket = bucketName + bucketId;
            String fileExtension = filename.substring(filename.lastIndexOf("."));
            fileId = fileId + fileExtension;
            System.out.println("Bucket name:" + bucket);
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileId)
                    .build();

            HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);
            Map<String, String> metadata = headObjectResponse.metadata();

            String originalFilename = URLDecoder.decode(metadata.get("original-filename"), StandardCharsets.UTF_8);
            System.out.println("Original filename: " + originalFilename);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileId)
                    .responseContentDisposition("attachment; filename=\"" + originalFilename + "\"")
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofMinutes(1400))
                    .build();


            String presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString();
            return presignedUrl;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL for file:" + filename, e);

        }
    }

    public void createBucketForUser(String id) {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket("bucket" + id)
                    .build();
            s3Client.headBucket(headBucketRequest);
            System.out.println("Bucket-ul pentru urilizator exista deja");

        } catch (NoSuchBucketException e) {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket("bucket" + id)
                    .build();
            s3Client.createBucket(createBucketRequest);
            System.out.println("Bucket create: " + id);
        } catch (Exception e) {
            throw new RuntimeException("Eroare la crearea bucket-ului: " + e.getMessage(), e);
        }

    }

    public byte[] getFile(String filename, String bucketName) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket("bucket" + bucketName).key(filename).build();
            InputStream fileStream = s3Client.getObject(getObjectRequest);
            byte[] bytes = fileStream.readAllBytes();
            return bytes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFileFromMinIO(String fileName, String bucketId) {
        try {
            String bucket = bucketName + bucketId;

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            System.out.println("File deleted successfully: " + fileName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }

    public void deleteBucket(String bucketId) {
        try {
            String bucket = bucketName + bucketId;

            ListObjectsV2Request listObjects = ListObjectsV2Request.builder().bucket(bucket).build();

            ListObjectsV2Response response = s3Client.listObjectsV2(listObjects);

            for (S3Object object : response.contents()) {
                String objectKey = object.key();
                DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build();
                s3Client.deleteObject(deleteObjectRequest);
                System.out.println("File deleted successfully: " + objectKey);
            }

            DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucket).build();
            s3Client.deleteBucket(deleteBucketRequest);
            System.out.println("Bucket deleted successfully: " + bucket);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete bucket", e);
        }
    }


}

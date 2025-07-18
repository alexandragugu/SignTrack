package com.example.demo.services;

import eu.europa.esig.dss.model.DSSDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MinIOServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private DSSDocument dssDocument;

    @InjectMocks
    private MinIOService minIOService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        minIOService = new MinIOService(s3Client, null);
        minIOService.setBucketName("bucket");
    }

    @Test
    void testUploadDSSDocument_success() throws Exception {
        when(dssDocument.getName()).thenReturn("doc.pdf");
        byte[] content = "fake-content".getBytes();
        when(dssDocument.openStream()).thenReturn(new ByteArrayInputStream(content));

        String result = minIOService.uploadDSSDocument(dssDocument, "123");

        assertEquals("doc.pdf", result);
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }


    @Test
    void testCreateBucketForUser_bucketExists() {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
                .thenReturn(HeadBucketResponse.builder().build());

        minIOService.createBucketForUser("123");

        verify(s3Client).headBucket(any(HeadBucketRequest.class));
        verify(s3Client, never()).createBucket(any(CreateBucketRequest.class));
    }


    @Test
    void testCreateBucketForUser_bucketDoesNotExist() {
        doThrow(NoSuchBucketException.builder().build())
                .when(s3Client).headBucket(any(HeadBucketRequest.class));

        when(s3Client.createBucket(any(CreateBucketRequest.class)))
                .thenReturn(CreateBucketResponse.builder().build());

        minIOService.createBucketForUser("456");

        verify(s3Client).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    void testDeleteFileFromMinIO_success() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        minIOService.deleteFileFromMinIO("file.txt", "777");

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void testDeleteBucket_success() {
        ListObjectsV2Response mockListResponse = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key("file1.txt").build())
                .build();

        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(mockListResponse);

        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        when(s3Client.deleteBucket(any(DeleteBucketRequest.class)))
                .thenReturn(DeleteBucketResponse.builder().build());

        minIOService.deleteBucket("999");

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        verify(s3Client).deleteBucket(any(DeleteBucketRequest.class));
    }

    @Test
    void testGetPresignedUrl_success() throws Exception {
        String baseBucketName = "bucket";
        String fileId = "file123";
        String bucketId = "789";
        String fileName = "test.pdf";
        String fullBucketName = baseBucketName + bucketId;
        String fileKey = fileId + ".pdf";

        Map<String, String> mockMetadata = new HashMap<>();
        mockMetadata.put("original-filename", "original-original.pdf");

        HeadObjectResponse headObjectResponse = mock(HeadObjectResponse.class);
        when(headObjectResponse.metadata()).thenReturn(mockMetadata);
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headObjectResponse);

        URI mockUri = new URI("http://localhost:9000/" + fullBucketName + "/" + fileKey);

        S3Presigner s3Presigner = mock(S3Presigner.class);
        PresignedGetObjectRequest presignedRequest = mock(PresignedGetObjectRequest.class);
        when(presignedRequest.url()).thenReturn(mockUri.toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        MinIOService service = new MinIOService(s3Client, s3Presigner);
        service.setBucketName(baseBucketName);

        String url = service.getPresignedUrl(fileId, bucketId, fileName);

        assertNotNull(url);
        assertTrue(url.contains(fileKey));
        assertTrue(url.startsWith("http://localhost:9000"));
    }
}
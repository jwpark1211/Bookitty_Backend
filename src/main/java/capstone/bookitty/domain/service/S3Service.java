package capstone.bookitty.domain.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class S3Service {
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;

    private static final Map<String, String> contentTypeMap = new HashMap<>();
    static {
        contentTypeMap.put("jpeg", "image/jpeg");
        contentTypeMap.put("jpg", "image/jpeg");
        contentTypeMap.put("png", "image/png");
        contentTypeMap.put("txt", "text/plain");
        contentTypeMap.put("csv", "text/csv");
        // Add more content types as needed
    }

    public String uploadFile(MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Invalid file name.");
        }

        String ext = getExtension(fileName);
        String contentType = contentTypeMap.getOrDefault(ext, "application/octet-stream");

        try (InputStream inputStream = multipartFile.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(multipartFile.getSize());

            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (AmazonServiceException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        } catch (SdkClientException e) {
            throw new RuntimeException("Failed to connect to S3", e);
        }

        // Print object summaries
        ListObjectsV2Result listObjectsV2Result = amazonS3.listObjectsV2(bucket);
        List<S3ObjectSummary> objectSummaries = listObjectsV2Result.getObjectSummaries();
        for (S3ObjectSummary object : objectSummaries) {
            System.out.println("object = " + object.toString());
        }

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    public void deleteImage(String filename) {
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, filename));
        } catch (AmazonServiceException e) {
            throw new RuntimeException("Failed to delete file from S3", e);
        } catch (SdkClientException e) {
            throw new RuntimeException("Failed to connect to S3", e);
        }
    }

    private String getExtension(String fileName) {
        String[] parts = fileName.split("\\.");
        return parts[parts.length - 1].toLowerCase();
    }
}

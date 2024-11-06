package ika.services;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import ika.entities.FileEntity;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

import ika.entities.Bucket;
import ika.entities.Usage;
import ika.repositories.BucketRepository;
import ika.repositories.FileRepository;
import ika.utils.CurrentUserProvider;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo", "video/x-ms-wmv", "video/x-matroska"
    );
    private static final long MAX_FILE_SIZE = 64 * 1024 * 1024;  // 64MB

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private BucketRepository bucketRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    public URL generatePresignedUrl(String bucketName, String objectKey) {
        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + 60 * 60 * 1000);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return s3Client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    public FileEntity uploadFile(String bucketDescription, MultipartFile file) throws Exception {
        OffsetDateTime localDateTimeNow = OffsetDateTime.now();
        String originalFilename = file.getOriginalFilename();
        String fileName = originalFilename + "-" + currentUserProvider.getCurrentUserId().toString()+"-"+localDateTimeNow.toString();
        System.out.println(fileName);

        validateFileType(file);

        // Find the bucket by ID in the database
        Optional<Bucket> bucketOptional = bucketRepository.findByDescription(bucketDescription);
        if (bucketOptional.isEmpty()) {
            throw new Exception("Bucket not found in the database");
        }

        Bucket bucket = bucketOptional.get();  // Get bucket entity
        String bucketName = bucket.getName();
        String fileType = file.getContentType();

        // Upload file to S3 bucket
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(fileType);
        // Upload to S3
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));

        // Save file metadata in the database
        FileEntity fileEntity = insertFile(fileName, localDateTimeNow, fileType, bucket);

        // Save file entity in the repository (database)
        return fileEntity;
    }

    private FileEntity insertFile(String fileName, OffsetDateTime localDateTimeNow, String fileType, Bucket bucket) {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setName(fileName);
        fileEntity.setCreatedAt(localDateTimeNow);
        fileEntity.setType(fileType);
        fileEntity.setBucket(bucket);  // Associate with the bucket
        return fileRepository.save(fileEntity);
    }

    /**
     * Deletes a file from S3 and the database
     */
    public void deleteFile(UUID fileId) {
        // Find the file entity in the database
        Optional<FileEntity> fileEntityOptional = fileRepository.findById(fileId);
        if (fileEntityOptional.isEmpty()) {
            throw new ResourceNotFoundException("File not found");
        }

        FileEntity fileEntity = fileEntityOptional.get();
        String bucketName = fileEntity.getBucket().getName();
        String fileName = fileEntity.getName();
        deleteFileFromS3(bucketName, fileName);

        // Delete the file metadata from the database
        fileRepository.delete(fileEntity);
    }

    private void deleteFileFromS3(String bucketName, String fileName){
        // Delete the file from S3
        s3Client.deleteObject(bucketName, fileName);
    }

    private void validateFileType(MultipartFile file) throws NoSuchMethodException {
        String contentType = file.getContentType();
        long fileSize = file.getSize();

        // Check if the content type is in the allowed list
        if (contentType == null || !ALLOWED_VIDEO_TYPES.contains(contentType)) {
            String expectedTypes = String.join(", ", ALLOWED_VIDEO_TYPES);
            throw new IllegalArgumentException("Invalid file type: '" + contentType + "' for parameter 'file'. Expected a video file of type: " + expectedTypes);
        }

        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed limit of 10MB.");
        }

        // If the file is valid, proceed (this is just an example of what you might do next)
        System.out.println("File type is valid: " + contentType);
    }

}

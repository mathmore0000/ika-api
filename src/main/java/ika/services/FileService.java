package ika.services;

import com.amazonaws.services.s3.model.ObjectMetadata;
import ika.entities.FileEntity;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ika.entities.Bucket;
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

    public void uploadFile(String bucketDescription, MultipartFile file) throws IOException, NoSuchMethodException {
        LocalDateTime localDateTimeNow= LocalDateTime.now();
        String originalFilename = file.getOriginalFilename();
        String fileName = originalFilename + "-" + currentUserProvider.getCurrentUserId().toString()+"-"+localDateTimeNow.toString();
        System.out.println(fileName);
        validateFileType(file);
        // Find the bucket by ID in the database
        Optional<Bucket> bucketOptional = bucketRepository.findByDescription(bucketDescription);
        if (bucketOptional.isEmpty()) {
            throw new ResourceNotFoundException("Bucket not found in the database");
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
        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(UUID.randomUUID());
        fileEntity.setName(fileName);
        fileEntity.setCreatedAt(localDateTimeNow);
        fileEntity.setType(fileType);
        fileEntity.setBucket(bucket);  // Associate with the bucket

        // Save file entity in the repository (database)
        fileRepository.save(fileEntity);
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

        // Delete the file from S3
        s3Client.deleteObject(bucketName, fileName);

        // Delete the file metadata from the database
        fileRepository.delete(fileEntity);
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

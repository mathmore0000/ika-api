package ika.services;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import ika.entities.FileEntity;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;

import ika.entities.Bucket;
import ika.repositories.BucketRepository;
import ika.repositories.FileRepository;
import ika.utils.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo", "video/x-ms-wmv", "video/x-matroska"
    );
    private static final long MAX_VIDEO_FILE_SIZE = 64 * 1024 * 1024;  // 64MB

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp"
    );
    private static final long MAX_IMAGE_FILE_SIZE = 10 * 1024 * 1024;  // 10MB

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private BucketRepository bucketRepository;

    public URL generatePresignedUrl(String bucketName, String objectKey) {
        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + 60 * 60 * 1000);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return s3Client.generatePresignedUrl(generatePresignedUrlRequest);
    }

    public FileEntity uploadImage(UUID userId, String bucketDescription, MultipartFile image) throws Exception {
        OffsetDateTime localDateTimeNow = OffsetDateTime.now();
        String originalFilename = image.getOriginalFilename();
        String sanitizedFilename = originalFilename != null ? originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_") : "avatar";
        String fileName = sanitizedFilename + "-" + userId.toString() + "-" + localDateTimeNow.toString();
        System.out.println("Uploading file: " + fileName);

        validateImageType(image);

        // Find the bucket by description in the database
        Optional<Bucket> bucketOptional = bucketRepository.findByDescription(bucketDescription);
        if (bucketOptional.isEmpty()) {
            throw new Exception("Bucket not found in the database");
        }

        Bucket bucket = bucketOptional.get();  // Get bucket entity
        String bucketName = bucket.getName();
        String fileType = image.getContentType();

        // Upload file to S3 bucket
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(image.getSize());
        metadata.setContentType(fileType);
        // Upload to S3
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, image.getInputStream(), metadata));

        // Save file metadata in the database
        FileEntity fileEntity = insertFile(fileName, localDateTimeNow, fileType, bucket);

        // Save file entity in the repository (database)
        return fileRepository.save(fileEntity);
    }

    public FileEntity uploadVideo(UUID userId, String bucketDescription, MultipartFile video) throws Exception {
        OffsetDateTime localDateTimeNow = OffsetDateTime.now();
        String originalFilename = video.getOriginalFilename();
        String fileName = originalFilename + "-" + userId.toString()+"-"+localDateTimeNow.toString();
        System.out.println(fileName);

        validateVideoType(video);

        // Find the bucket by ID in the database
        Optional<Bucket> bucketOptional = bucketRepository.findByDescription(bucketDescription);
        if (bucketOptional.isEmpty()) {
            throw new Exception("Bucket not found in the database");
        }

        Bucket bucket = bucketOptional.get();  // Get bucket entity
        String bucketName = bucket.getName();
        String fileType = video.getContentType();

        // Upload file to S3 bucket
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(video.getSize());
        metadata.setContentType(fileType);
        // Upload to S3
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, video.getInputStream(), metadata));

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

    public String getPublicUrl(FileEntity fileEntity) {
        String bucketName = fileEntity.getBucket().getName();
        String fileName = fileEntity.getName();
        return s3Client.getUrl(bucketName, fileName).toString();
    }

    /**
     * Extracts the FileEntity ID from a given S3 URL.
     *
     * @param urlString The S3 URL of the file.
     * @return The UUID of the corresponding FileEntity.
     * @throws ResourceNotFoundException If the file is not found in the repository.
     * @throws IllegalArgumentException  If the URL is malformed or decoding fails.
     */
    public UUID getFileIdFromUrl(String urlString) {
        try {
            // Parse the URL
            URL url = new URL(urlString);
            String path = url.getPath(); // e.g., /avatar_images/fileName.ext

            // Extract the file name from the path
            String fileName = extractFileNameFromPath(path);
            System.out.println("Encoded file name: " + fileName);

            // Decode the file name
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.name());
            System.out.println("Decoded file name: " + decodedFileName);

            // Retrieve the FileEntity from the repository
            Optional<FileEntity> fileEntityOptional = fileRepository.findByName(decodedFileName);
            if (fileEntityOptional.isEmpty()) {
                throw new ResourceNotFoundException("File not found with name: " + decodedFileName);
            }

            return fileEntityOptional.get().getId();
        } catch (Exception e) {
            // Log the exception details (optional)
            System.err.println("Error in getFileIdFromUrl: " + e.getMessage());
            throw new IllegalArgumentException("Invalid URL or file name: " + e.getMessage(), e);
        }
    }
    /**
     * Extracts the file name from the URL path.
     *
     * @param path The path component of the URL.
     * @return The file name.
     */
    private String extractFileNameFromPath(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("URL path is empty");
        }

        // Assuming the file name is the last segment of the path
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1 || lastSlashIndex == path.length() - 1) {
            throw new IllegalArgumentException("Cannot extract file name from path: " + path);
        }

        return path.substring(lastSlashIndex + 1);
    }

    private void validateImageType(MultipartFile file) throws IllegalArgumentException {
        String contentType = file.getContentType();
        long fileSize = file.getSize();

        // Check if the content type is in the allowed list
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            String expectedTypes = String.join(", ", ALLOWED_IMAGE_TYPES);
            throw new IllegalArgumentException("Invalid file type: '" + contentType + "'. Expected an image file of type: " + expectedTypes);
        }

        if (fileSize > MAX_IMAGE_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed limit of 10MB.");
        }

        // If the file is valid, proceed
        System.out.println("File type is valid: " + contentType);
    }

    private void validateVideoType(MultipartFile file) throws NoSuchMethodException {
        String contentType = file.getContentType();
        long fileSize = file.getSize();

        // Check if the content type is in the allowed list
        if (contentType == null || !ALLOWED_VIDEO_TYPES.contains(contentType)) {
            String expectedTypes = String.join(", ", ALLOWED_VIDEO_TYPES);
            throw new IllegalArgumentException("Invalid file type: '" + contentType + "' for parameter 'file'. Expected a video file of type: " + expectedTypes);
        }

        if (fileSize > MAX_VIDEO_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed limit of 10MB.");
        }

        // If the file is valid, proceed (this is just an example of what you might do next)
        System.out.println("File type is valid: " + contentType);
    }

}

package ika.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 s3Client;

    public void uploadFile(String bucketName, String key, InputStream inputStream, long contentLength) {
        // Define object metadata for the upload
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(contentLength);

        // Use InputStream for file upload
        s3Client.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
    }

    public S3Object downloadFile(String bucketName, String key) {
        return s3Client.getObject(bucketName, key);
    }
}

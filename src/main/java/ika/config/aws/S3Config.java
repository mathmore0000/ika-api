package ika.config.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Bean
    public AmazonS3 s3Client() {
        // Replace these with your actual access keys (use environment variables or a secrets manager in production)
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");


        // Create AWS credentials using access and secret key
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        // Build the S3 client
        return AmazonS3ClientBuilder.standard()
                .withRegion("sa-east-1")  // Specify the region
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();
    }
}

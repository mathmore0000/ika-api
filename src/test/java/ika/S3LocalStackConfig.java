package ika;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
@Profile("test")
public class S3LocalStackConfig {

    @Bean
    public LocalStackContainer localStackContainer() {
        // Criar e configurar o container do LocalStack
        LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                .withServices(LocalStackContainer.Service.S3);
        localStack.start();
        return localStack;
    }

    @Bean
    public AmazonS3 s3Client(LocalStackContainer localStackContainer) {
        // Criar um cliente Amazon S3 usando as credenciais básicas e endpoint do LocalStack
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3).toString(),
                        localStackContainer.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("test", "test")))
                .enablePathStyleAccess()  // Isso é importante para evitar problemas com o formato do bucket
                .build();
    }
}

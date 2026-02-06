package com.alexp.aws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Configuración de AWS S3 Client
 * 
 * Esta clase es marcada con @Configuration, lo que indica a Spring que contiene
 * definiciones de beans que deben ser administrados por el contenedor de Spring.
 * 
 * El método s3Client() está anotado con @Bean, lo que significa que Spring
 * creará una instancia de S3Client y la gestionará automáticamente.
 * Este bean puede ser inyectado en cualquier componente usando @Autowired.
 * 
 * @Value lee valores del archivo application.properties
 */
@Configuration
public class S3Config {

    @Value("${aws.s3.endpoint}")
    private String endpoint;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.path-style-access}")
    private boolean pathStyleAccess;

    /**
     * Crea y configura el cliente S3 para LocalStack
     * 
     * @return S3Client configurado para conectarse a LocalStack
     * 
     * ¿Qué hace este método?
     * 1. Lee configuración desde application.properties
     * 2. Crea credenciales (en LocalStack son dummy: "test"/"test")
     * 3. Configura endpoint (http://localhost:4566 para LocalStack)
     * 4. Habilita forcePathStyle (requerido por LocalStack)
     * 5. Retorna el cliente listo para usar
     * 
     * Spring inyectará este bean automáticamente donde se necesite con @Autowired
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(pathStyleAccess) // Requerido para LocalStack
                .build();
    }
}

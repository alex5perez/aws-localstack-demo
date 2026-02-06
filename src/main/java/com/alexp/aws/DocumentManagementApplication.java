package com.alexp.aws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Document Management System - Aplicación Principal
 * 
 * Esta es la clase principal que arranca la aplicación Spring Boot.
 * 
 * @SpringBootApplication incluye:
 * - @Configuration: Indica que es una clase de configuración
 * - @EnableAutoConfiguration: Activa la autoconfiguración de Spring Boot
 * - @ComponentScan: Escanea automáticamente componentes en este paquete y subpaquetes
 * 
 * Al ejecutar main(), Spring Boot:
 * 1. Inicia el servidor Tomcat embebido (puerto 8080)
 * 2. Escanea y registra todos los @Controller, @Service, @Component
 * 3. Configura beans automáticamente
 * 4. Expone los endpoints REST definidos en los controllers
 */
@SpringBootApplication
public class DocumentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentManagementApplication.class, args);
    }
}

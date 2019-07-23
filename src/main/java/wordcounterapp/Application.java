package wordcounterapp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import wordcounterapp.storage.StorageProperties;
import wordcounterapp.storage.StorageService;
import wordcounterapp.wordcounter.WordCounter;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(StorageProperties.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService, WordCounter wordCounter) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
            wordCounter.init();
        };
    }
}

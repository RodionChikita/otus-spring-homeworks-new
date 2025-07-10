package ru.otus.hw;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.otus.hw.config.AppProperties;
import ru.otus.hw.config.TestConfig;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.CsvQuestionDao;
import ru.otus.hw.dao.QuestionDao;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Bean
    TestFileNameProvider testFileNameProvider(
            @Value(value = "${test.rightAnswersCountToPass}") int rightAnswersCountToPass,
            @Value(value = "${test.fileName}") String testFileName) {
        return new AppProperties(rightAnswersCountToPass, testFileName);
    }

    @Bean
    TestConfig testConfig(
            @Value(value = "${test.rightAnswersCountToPass}") int rightAnswersCountToPass,
            @Value(value = "${test.fileName}") String testFileName) {
        return new AppProperties(rightAnswersCountToPass, testFileName);
    }

    @Bean
    QuestionDao personDao(@Qualifier("testFileNameProvider") TestFileNameProvider testFileNameProvider) {
        return new CsvQuestionDao(testFileNameProvider);
    }
}

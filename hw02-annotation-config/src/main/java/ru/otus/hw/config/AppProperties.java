package ru.otus.hw.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class AppProperties implements TestConfig, TestFileNameProvider {

    private int rightAnswersCountToPass;

    private String testFileName;

    @Autowired
    public AppProperties(int rightAnswersCountToPass, String testFileName) {
        this.rightAnswersCountToPass = rightAnswersCountToPass;
        this.testFileName = testFileName;
    }
}

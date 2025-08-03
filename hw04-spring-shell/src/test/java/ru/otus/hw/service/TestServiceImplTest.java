package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class TestServiceImplTest {

    @MockBean
    private LocalizedIOService ioService;

    @Autowired
    private TestService testService;

    @Test
    @DisplayName("Должен корректно проводить тестирование и возвращать результат")
    void executeTestFor_ShouldConductTestAndReturnResult() {
        Student student = new Student("John", "Doe");

        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1, 1, 3);

        TestResult result = testService.executeTestFor(student);

        assertNotNull(result);
        assertEquals(student, result.getStudent());
        assertEquals(3, result.getAnsweredQuestions().size());
        assertEquals(3, result.getRightAnswersCount());

        verify(ioService).printLine("");
        verify(ioService).printFormattedLineLocalized("TestService.answer.the.questions");
        verify(ioService, times(3)).readIntForRangeWithPromptLocalized(anyInt(), anyInt(), 
                eq("TestService.answer.the.questions"), eq("TestService.invalid.answer"));
    }

    @Test
    @DisplayName("Должен корректно подсчитывать правильные ответы")
    void executeTestFor_ShouldCountCorrectAnswers() {
        Student student = new Student("Jane", "Smith");
        
        // Неправильные ответы: 2, 2, 1 = 0 правильных
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(2, 2, 1);

        TestResult result = testService.executeTestFor(student);

        assertEquals(0, result.getRightAnswersCount());
        assertEquals(3, result.getAnsweredQuestions().size());
        
        verify(ioService, times(3)).readIntForRangeWithPromptLocalized(anyInt(), anyInt(), 
                eq("TestService.answer.the.questions"), eq("TestService.invalid.answer"));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать тест с правильными ответами")
    void executeTestFor_ShouldHandleCorrectAnswers() {
        Student student = new Student("Bob", "Johnson");

        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1, 2, 3);

        TestResult result = testService.executeTestFor(student);

        assertEquals(2, result.getRightAnswersCount());
        assertEquals(3, result.getAnsweredQuestions().size());
        
        verify(ioService, times(3)).readIntForRangeWithPromptLocalized(anyInt(), anyInt(), 
                eq("TestService.answer.the.questions"), eq("TestService.invalid.answer"));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать пустой список вопросов")
    void executeTestFor_ShouldHandleEmptyQuestions() {
        Student student = new Student("Alice", "Wonder");

        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(3, 3, 2);

        TestResult result = testService.executeTestFor(student);

        assertEquals(0, result.getRightAnswersCount());
        assertEquals(3, result.getAnsweredQuestions().size());
        
        verify(ioService).printFormattedLineLocalized("TestService.answer.the.questions");
        verify(ioService, times(3)).readIntForRangeWithPromptLocalized(anyInt(), anyInt(), 
                eq("TestService.answer.the.questions"), eq("TestService.invalid.answer"));
    }

    @Test
    @DisplayName("Должен корректно работать с вопросами содержащими только один ответ")
    void executeTestFor_ShouldHandleSingleAnswerQuestions() {
        Student student = new Student("Charlie", "Brown");

        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(2, 3, 4);

        TestResult result = testService.executeTestFor(student);

        assertEquals(0, result.getRightAnswersCount());
        assertEquals(3, result.getAnsweredQuestions().size());
        
        verify(ioService, times(3)).readIntForRangeWithPromptLocalized(anyInt(), anyInt(), 
                eq("TestService.answer.the.questions"), eq("TestService.invalid.answer"));
    }

    @Test
    @DisplayName("Должен использовать правильные ключи локализации")
    void executeTestFor_ShouldUseCorrectLocalizationKeys() {
        Student student = new Student("Clark", "Kent");
        
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1, 1, 3);

        testService.executeTestFor(student);

        verify(ioService).printFormattedLineLocalized("TestService.answer.the.questions");
        verify(ioService, times(3)).readIntForRangeWithPromptLocalized(anyInt(), anyInt(), 
                eq("TestService.answer.the.questions"), eq("TestService.invalid.answer"));
    }
}
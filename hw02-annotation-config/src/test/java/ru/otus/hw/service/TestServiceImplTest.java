package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    @InjectMocks
    private TestServiceImpl testService;

    @Test
    @DisplayName("Должен корректно проводить тестирование и возвращать результат")
    void executeTestFor_ShouldConductTestAndReturnResult() {
        Student student = new Student("John", "Doe");
        List<Question> questions = List.of(
                new Question("Question 1", List.of(
                        new Answer("Answer 1-1", true),
                        new Answer("Answer 1-2", false)
                )),
                new Question("Question 2", List.of(
                        new Answer("Answer 2-1", false),
                        new Answer("Answer 2-2", true),
                        new Answer("Answer 2-3", false)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);

        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1, 2);

        TestResult result = testService.executeTestFor(student);

        assertNotNull(result);
        assertEquals(student, result.getStudent());
        assertEquals(2, result.getAnsweredQuestions().size());
        assertEquals(2, result.getRightAnswersCount());

        verify(ioService).printLine("");
        verify(ioService).printFormattedLine("Please answer the questions below%n");
        verify(questionDao, times(1)).findAll();

        verify(ioService, times(2)).readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("Должен корректно подсчитывать правильные ответы")
    void executeTestFor_ShouldCountCorrectAnswers() {
        Student student = new Student("Jane", "Smith");
        List<Question> questions = List.of(
                new Question("Question 1", List.of(
                        new Answer("Answer 1-1", true),
                        new Answer("Answer 1-2", false)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(2); // Неправильный ответ (индекс 1, но это false)

        TestResult result = testService.executeTestFor(student);

        assertEquals(0, result.getRightAnswersCount());
        assertEquals(1, result.getAnsweredQuestions().size());
        
        verify(ioService, times(1)).readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("Должен корректно обрабатывать тест с правильными ответами")
    void executeTestFor_ShouldHandleCorrectAnswers() {
        // Given
        Student student = new Student("Bob", "Johnson");
        List<Question> questions = List.of(
                new Question("Question 1", List.of(
                        new Answer("Answer 1-1", true),
                        new Answer("Answer 1-2", false)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1);

        TestResult result = testService.executeTestFor(student);

        assertEquals(1, result.getRightAnswersCount());
        assertEquals(1, result.getAnsweredQuestions().size());
        
        verify(ioService, times(1)).readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("Должен корректно обрабатывать пустой список вопросов")
    void executeTestFor_ShouldHandleEmptyQuestions() {
        Student student = new Student("Alice", "Wonder");
        List<Question> questions = List.of();

        when(questionDao.findAll()).thenReturn(questions);

        TestResult result = testService.executeTestFor(student);

        assertEquals(0, result.getRightAnswersCount());
        assertEquals(0, result.getAnsweredQuestions().size());
        
        verify(ioService, never()).readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("Должен корректно работать с вопросами содержащими только один ответ")
    void executeTestFor_ShouldHandleSingleAnswerQuestions() {
        // Given
        Student student = new Student("Charlie", "Brown");
        List<Question> questions = List.of(
                new Question("Single Answer Question", List.of(
                        new Answer("Only Answer", true)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1);

        TestResult result = testService.executeTestFor(student);

        assertEquals(1, result.getRightAnswersCount());
        assertEquals(1, result.getAnsweredQuestions().size());

        verify(ioService).readIntForRangeWithPrompt(eq(1), eq(1), anyString(), anyString());
    }
}
package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
    private LocalizedIOService ioService;

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
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1, 2);
        TestResult result = testService.executeTestFor(student);

        assertNotNull(result);
        assertEquals(student, result.getStudent());
        assertEquals(2, result.getAnsweredQuestions().size());
        assertEquals(2, result.getRightAnswersCount());

        verify(ioService).printLine("");
        verify(ioService).printFormattedLineLocalized("TestService.answer.the.questions");
        verify(questionDao, times(1)).findAll();

        verify(ioService).printFormattedLine("1. %s", "Question 1");
        verify(ioService).printFormattedLine("  1) %s", "Answer 1-1");
        verify(ioService).printFormattedLine("  2) %s", "Answer 1-2");
        
        verify(ioService).printFormattedLine("2. %s", "Question 2");
        verify(ioService).printFormattedLine("  1) %s", "Answer 2-1");
        verify(ioService).printFormattedLine("  2) %s", "Answer 2-2");
        verify(ioService).printFormattedLine("  3) %s", "Answer 2-3");

        verify(ioService).readIntForRangeWithPromptLocalized(1, 2, 
                "TestService.answer.the.questions", "TestService.invalid.answer");
        verify(ioService).readIntForRangeWithPromptLocalized(1, 3, 
                "TestService.answer.the.questions", "TestService.invalid.answer");
    }

    @Test
    @DisplayName("Должен корректно подсчитывать правильные ответы")
    void executeTestFor_ShouldCountCorrectAnswers() {
        // Given
        Student student = new Student("Jane", "Smith");
        List<Question> questions = List.of(
                new Question("Question 1", List.of(
                        new Answer("Answer 1-1", true),
                        new Answer("Answer 1-2", false)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(2);

        TestResult result = testService.executeTestFor(student);

        assertEquals(0, result.getRightAnswersCount());
        assertEquals(1, result.getAnsweredQuestions().size());
        
        verify(ioService, times(1)).readIntForRangeWithPromptLocalized(1, 2, 
                "TestService.answer.the.questions", "TestService.invalid.answer");
    }

    @Test
    @DisplayName("Должен корректно обрабатывать тест с правильными ответами")
    void executeTestFor_ShouldHandleCorrectAnswers() {
        Student student = new Student("Bob", "Johnson");
        List<Question> questions = List.of(
                new Question("Question 1", List.of(
                        new Answer("Answer 1-1", true),
                        new Answer("Answer 1-2", false)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1);

        TestResult result = testService.executeTestFor(student);

        assertEquals(1, result.getRightAnswersCount());
        assertEquals(1, result.getAnsweredQuestions().size());
        
        verify(ioService, times(1)).readIntForRangeWithPromptLocalized(1, 2, 
                "TestService.answer.the.questions", "TestService.invalid.answer");
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
        
        verify(ioService, never()).readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString());
        verify(ioService).printFormattedLineLocalized("TestService.answer.the.questions");
    }

    @Test
    @DisplayName("Должен корректно работать с вопросами содержащими только один ответ")
    void executeTestFor_ShouldHandleSingleAnswerQuestions() {
        Student student = new Student("Charlie", "Brown");
        List<Question> questions = List.of(
                new Question("Single Answer Question", List.of(
                        new Answer("Only Answer", true)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1);

        TestResult result = testService.executeTestFor(student);

        assertEquals(1, result.getRightAnswersCount());
        assertEquals(1, result.getAnsweredQuestions().size());

        verify(ioService).readIntForRangeWithPromptLocalized(1, 1, 
                "TestService.answer.the.questions", "TestService.invalid.answer");
    }

    @Test
    @DisplayName("Должен правильно отображать вопросы и ответы в нужном порядке")
    void executeTestFor_ShouldDisplayQuestionsInCorrectOrder() {
        Student student = new Student("Diana", "Prince");
        List<Question> questions = List.of(
                new Question("What is 2+2?", List.of(
                        new Answer("3", false),
                        new Answer("4", true),
                        new Answer("5", false)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(2);

        testService.executeTestFor(student);

        InOrder inOrder = inOrder(ioService);
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printFormattedLineLocalized("TestService.answer.the.questions");

        inOrder.verify(ioService).printFormattedLine("1. %s", "What is 2+2?");
        inOrder.verify(ioService).printFormattedLine("  1) %s", "3");
        inOrder.verify(ioService).printFormattedLine("  2) %s", "4");
        inOrder.verify(ioService).printFormattedLine("  3) %s", "5");
        
        inOrder.verify(ioService).readIntForRangeWithPromptLocalized(1, 3, 
                "TestService.answer.the.questions", "TestService.invalid.answer");
    }

    @Test
    @DisplayName("Должен использовать правильные ключи локализации")
    void executeTestFor_ShouldUseCorrectLocalizationKeys() {
        Student student = new Student("Clark", "Kent");
        List<Question> questions = List.of(
                new Question("Test Question", List.of(
                        new Answer("Test Answer", true)
                ))
        );

        when(questionDao.findAll()).thenReturn(questions);
        when(ioService.readIntForRangeWithPromptLocalized(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(1);

        testService.executeTestFor(student);

        verify(ioService).printFormattedLineLocalized("TestService.answer.the.questions");
        verify(ioService).readIntForRangeWithPromptLocalized(1, 1, 
                "TestService.answer.the.questions", "TestService.invalid.answer");
    }
}
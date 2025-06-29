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

import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    @InjectMocks
    private TestServiceImpl testService;

    @Test
    @DisplayName("Должен корректно выводить вопросы и ответы")
    void executeTest_ShouldPrintQuestionsAndAnswers() {
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

        testService.executeTest();

        InOrder inOrder = inOrder(ioService);
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printFormattedLine("Please answer the questions below%n");

        inOrder.verify(ioService).printFormattedLine("1. %s", "Question 1");
        inOrder.verify(ioService).printFormattedLine(" 1) %s", "Answer 1-1");
        inOrder.verify(ioService).printFormattedLine(" 2) %s", "Answer 1-2");

        inOrder.verify(ioService).printFormattedLine("2. %s", "Question 2");
        inOrder.verify(ioService).printFormattedLine(" 1) %s", "Answer 2-1");
        inOrder.verify(ioService).printFormattedLine(" 2) %s", "Answer 2-2");
        inOrder.verify(ioService).printFormattedLine(" 3) %s", "Answer 2-3");
    }
}
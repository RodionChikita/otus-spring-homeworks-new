package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CsvQuestionDaoTest {

    @Mock
    private TestFileNameProvider fileNameProvider;

    private CsvQuestionDao csvQuestionDao;

    @BeforeEach
    public void setUp() {
        csvQuestionDao = new CsvQuestionDao(fileNameProvider);
    }

    @Test
    public void testFindAllExistentFile() {
        String testFileName = "questions.csv";
        when(fileNameProvider.getTestFileName()).thenReturn(testFileName);
        
        List<Question> questions = csvQuestionDao.findAll();

        assertNotNull(questions);
        assertEquals(3, questions.size());

        Question firstQuestion = questions.get(0);
        assertEquals("Is there life on Mars?", firstQuestion.text());
        assertEquals(3, firstQuestion.answers().size());

        List<Answer> firstQuestionAnswers = firstQuestion.answers();
        assertTrue(firstQuestionAnswers.get(0).isCorrect());
        assertEquals("Science doesn't know this yet", firstQuestionAnswers.get(0).text());
        assertFalse(firstQuestionAnswers.get(1).isCorrect());
        assertFalse(firstQuestionAnswers.get(2).isCorrect());

        Question secondQuestion = questions.get(1);
        assertEquals("How should resources be loaded form jar in Java?", secondQuestion.text());
        assertEquals(3, secondQuestion.answers().size());
        assertTrue(secondQuestion.answers().get(0).isCorrect());

        Question thirdQuestion = questions.get(2);
        assertEquals("Which option is a good way to handle the exception?", thirdQuestion.text());
        assertEquals(4, thirdQuestion.answers().size());

        assertTrue(thirdQuestion.answers().get(2).isCorrect());
        assertEquals("Rethrow with wrapping in business exception (for example, QuestionReadException)", 
                thirdQuestion.answers().get(2).text());
    }

    @Test
    public void testFindAllNotExistentFileThrowsException() {
        String testFileName = "non-existent-file.csv";
        when(fileNameProvider.getTestFileName()).thenReturn(testFileName);
        assertThrows(QuestionReadException.class, () -> csvQuestionDao.findAll());
    }
}
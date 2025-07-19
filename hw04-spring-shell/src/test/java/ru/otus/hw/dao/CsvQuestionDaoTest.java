package ru.otus.hw.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CsvQuestionDao.class)
class CsvQuestionDaoTest {

    @MockBean
    private TestFileNameProvider fileNameProvider;

    @Autowired
    private CsvQuestionDao questionDao;

    @Test
    void testFindAllExistentFile() {
        when(fileNameProvider.getTestFileName()).thenReturn("questions.csv");

        List<Question> questions = questionDao.findAll();

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
    void testFindAllNotExistentFileThrowsException() {
        when(fileNameProvider.getTestFileName()).thenReturn("non-existent-file.csv");

        assertThrows(QuestionReadException.class, () -> questionDao.findAll());
    }

    @Test
    void testFindAllParsesAnswersCorrectly() {
        when(fileNameProvider.getTestFileName()).thenReturn("questions.csv");

        List<Question> questions = questionDao.findAll();

        assertNotNull(questions);
        assertFalse(questions.isEmpty());
        
        for (Question question : questions) {
            assertNotNull(question.text());
            assertFalse(question.text().isEmpty());
            
            assertNotNull(question.answers());
            assertFalse(question.answers().isEmpty());

            boolean hasCorrectAnswer = question.answers().stream()
                    .anyMatch(Answer::isCorrect);
            assertTrue(hasCorrectAnswer, "Each question should have at least one correct answer");

            for (Answer answer : question.answers()) {
                assertNotNull(answer.text());
                assertFalse(answer.text().isEmpty());
            }
        }
    }

    @Test
    void testFindAllWithRussianFile() {
        when(fileNameProvider.getTestFileName()).thenReturn("questions_ru.csv");

        List<Question> questions = questionDao.findAll();

        assertNotNull(questions);
        assertEquals(3, questions.size());

        for (Question question : questions) {
            assertNotNull(question.text());
            assertFalse(question.text().isEmpty());
            assertNotNull(question.answers());
            assertFalse(question.answers().isEmpty());
        }
    }

    @Test
    void testFindAllValidatesFileFormat() {
        when(fileNameProvider.getTestFileName()).thenReturn("questions.csv");

        List<Question> questions = questionDao.findAll();

        Question firstQuestion = questions.get(0);

        assertEquals("Is there life on Mars?", firstQuestion.text());

        assertEquals(3, firstQuestion.answers().size());

        Answer firstAnswer = firstQuestion.answers().get(0);
        assertTrue(firstAnswer.isCorrect());
        assertEquals("Science doesn't know this yet", firstAnswer.text());
    }

    @Test 
    void testFindAllHandlesSpecialCharacters() {
        when(fileNameProvider.getTestFileName()).thenReturn("questions.csv");

        List<Question> questions = questionDao.findAll();

        Question thirdQuestion = questions.get(2);

        String expectedText = "Rethrow with wrapping in business exception (for example, QuestionReadException)";
        Answer correctAnswer = thirdQuestion.answers().get(2);
        assertEquals(expectedText, correctAnswer.text());
        assertTrue(correctAnswer.isCorrect());
    }

    @Test
    void testFindAllReturnsConsistentResults() {
        when(fileNameProvider.getTestFileName()).thenReturn("questions.csv");

        List<Question> questions1 = questionDao.findAll();
        List<Question> questions2 = questionDao.findAll();

        assertEquals(questions1.size(), questions2.size());
        
        for (int i = 0; i < questions1.size(); i++) {
            assertEquals(questions1.get(i).text(), questions2.get(i).text());
            assertEquals(questions1.get(i).answers().size(), questions2.get(i).answers().size());
        }
    }
}
package ru.otus.hw.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "test.fileNameByLocaleTag.ru-RU=questions_ru.csv",
    "test.fileNameByLocaleTag.en-US=questions.csv"
})
class CsvQuestionDaoTest {

    @Autowired
    private QuestionDao questionDao;

    @Test
    void testFindAllExistentFile() {
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
    void testFindAllReturnsQuestionsWithCorrectStructure() {
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
        }
    }

    @Test
    void testFindAllParsesAnswersCorrectly() {
        List<Question> questions = questionDao.findAll();

        for (Question question : questions) {
            for (Answer answer : question.answers()) {
                assertNotNull(answer.text());
                assertFalse(answer.text().isEmpty());
            }
        }
    }
}
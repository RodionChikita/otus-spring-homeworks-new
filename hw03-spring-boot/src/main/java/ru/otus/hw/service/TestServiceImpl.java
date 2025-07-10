package ru.otus.hw.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

@Service
public class TestServiceImpl implements TestService {

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Autowired
    public TestServiceImpl(LocalizedIOService ioService, QuestionDao questionDao) {
        this.ioService = ioService;
        this.questionDao = questionDao;
    }


    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLineLocalized("TestService.answer.the.questions");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (int i = 0; i < questions.size(); i++) {
            showQuestion(questions, i);
            var numberOfAnswer = ioService.readIntForRangeWithPromptLocalized(1, questions.get(i).answers().size(),
                    "TestService.answer.the.questions", "TestService.invalid.answer");
            testResult.applyAnswer(questions.get(i), questions.get(i).answers().get(numberOfAnswer - 1).isCorrect());
        }

        return testResult;
    }

    private void showQuestion(List<Question> questions, int questionNumber) {
        ioService.printFormattedLine(questionNumber + 1 + ". %s", questions.get(questionNumber).text());
        var answers = questions.get(questionNumber).answers();
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("  " + (i + 1) + ") %s", answers.get(i).text());
        }
    }
}
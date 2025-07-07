package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (int i = 0; questions.size() > i; i++) {
            StringBuilder questionWithAnswers = new StringBuilder(i + 1 + ". " + questions.get(i).text());
            List<Answer> answers = questions.get(i).answers();
            for (int j = 0; answers.size() > j; j++) {
                questionWithAnswers.append("\n ").append(j + 1).append(") ").append(answers.get(j).text());
            }
            int answerNumber = ioService.readIntForRangeWithPrompt(1, answers.size(),
                    questionWithAnswers.toString(), "Your answer is out of range. Try again.");
            testResult.applyAnswer(questions.get(i), answers.get(answerNumber - 1).isCorrect());
        }
        return testResult;
    }
}

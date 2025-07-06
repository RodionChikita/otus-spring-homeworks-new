package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        List<Question> questions = questionDao.findAll();
        for (int i = 0; questions.size() > i; i++) {
            ioService.printFormattedLine(i + 1 + ". %s", questions.get(i).text());
            List<Answer> answers = questions.get(i).answers();
            for (int j = 0; answers.size() > j; j++) {
                ioService.printFormattedLine(" " + (j + 1) + ") %s", answers.get(j).text());
            }
        }
    }
}

package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.exceptions.QuestionReadException;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
    public void testFindAllThrowsException() {
        String testFileName = "non-existent-file.csv";
        when(fileNameProvider.getTestFileName()).thenReturn(testFileName);
        assertThrows(QuestionReadException.class, () -> csvQuestionDao.findAll());
    }
}
package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(propagation = Propagation.NEVER)
@Sql(scripts = {"/clean.sql","/test-data.sql"})
@DisplayName("Интеграционный тест BookServiceImpl")
class BookServiceImplIT {

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("findById: должен возвращать книгу с доступными author и genres")
    void findById_ShouldReturnBookWithAccessibleRelations() {
        var expected = new Book(1L,"BookTitle_1",
                new Author(1L,"Author_1"),
                List.of(new Genre(1L,"Genre_1"), new Genre(2L,"Genre_2")));

        var foundOpt = bookService.findById(1L);
        assertThat(foundOpt).isPresent();
        assertThat(foundOpt.get())
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("update: должен обновлять книгу")
    void update_ShouldUpdateBook() {
        var updated = bookService.update(1L, "BookTitle_1_Edited", 1L, Set.of(1L));
        var expected = new Book(1L, "BookTitle_1_Edited",
                new Author(1L,"Author_1"),
                List.of(new Genre(1L,"Genre_1")));

        assertThat(updated)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expected);
    }
}
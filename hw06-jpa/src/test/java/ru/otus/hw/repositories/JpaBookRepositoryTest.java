package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;

@DataJpaTest
@Import({JpaBookRepository.class})
@DisplayName("Репозиторий на основе JPA для работы с книгами")
@Sql(scripts = {"/clean.sql","/test-data.sql"})
class JpaBookRepositoryTest {

    private final JpaBookRepository bookRepository;

    @Autowired
    JpaBookRepositoryTest(JpaBookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Test
    @DisplayName("должен загружать книгу по id")
    void shouldReturnCorrectBookById() {
        var expected = new Book(
                1L, "BookTitle_1",
                new Author(1L, "Author_1"),
                List.of(new Genre(1L,"Genre_1"), new Genre(2L,"Genre_2"))
        );

        var actual = bookRepository.findById(1L);
        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("должен загружать список всех книг")
    void shouldReturnCorrectBooksList() {
        var expected = List.of(
                new Book(1L,"BookTitle_1", new Author(1L,"Author_1"),
                        List.of(new Genre(1L,"Genre_1"), new Genre(2L,"Genre_2"))),
                new Book(2L,"BookTitle_2", new Author(2L,"Author_2"),
                        List.of(new Genre(3L,"Genre_3"), new Genre(4L,"Genre_4"))),
                new Book(3L,"BookTitle_3", new Author(3L,"Author_3"),
                        List.of(new Genre(5L,"Genre_5"), new Genre(6L,"Genre_6")))
        );

        var actual = bookRepository.findAll();
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expected);
    }
}
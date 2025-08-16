package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.otus.hw.models.Author;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;

@DataJpaTest
@Import(JpaAuthorRepository.class)
@Sql(scripts = {"/clean.sql","/test-data.sql"})
@DisplayName("JPA-репозиторий авторов")
class JpaAuthorRepositoryTest {

    private final JpaAuthorRepository authorRepository;

    @Autowired
    JpaAuthorRepositoryTest(JpaAuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Test
    @DisplayName("должен находить всех авторов")
    void shouldFindAllAuthors() {
        var expected = List.of(
                new Author(1L, "Author_1"),
                new Author(2L, "Author_2"),
                new Author(3L, "Author_3")
        );
        var all = authorRepository.findAll();
        assertThat(all)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("должен находить автора по id")
    void shouldFindAuthorById() {
        var expected = new Author(2L, "Author_2");
        var actual = authorRepository.findById(2L);
        assertThat(actual).isPresent();
        assertThat(actual.get())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("должен возвращать пусто по несуществующему id")
    void shouldReturnEmptyForMissingId() {
        assertThat(authorRepository.findById(999L)).isEmpty();
    }
} 
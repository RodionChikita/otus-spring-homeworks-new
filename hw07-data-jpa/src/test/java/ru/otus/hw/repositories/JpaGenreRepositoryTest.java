package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;

@DataJpaTest
@Import(JpaGenreRepository.class)
@Sql(scripts = {"/clean.sql","/test-data.sql"})
@DisplayName("JPA-репозиторий жанров")
class JpaGenreRepositoryTest {

    private final JpaGenreRepository genreRepository;

    @Autowired
    JpaGenreRepositoryTest(JpaGenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Test
    @DisplayName("должен находить все жанры")
    void shouldFindAllGenres() {
        var expected = List.of(
                new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2"),
                new Genre(3L, "Genre_3"), new Genre(4L, "Genre_4"),
                new Genre(5L, "Genre_5"), new Genre(6L, "Genre_6")
        );
        var all = genreRepository.findAll();
        assertThat(all)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("должен находить жанры по id-шникам")
    void shouldFindAllByIds() {
        var ids = Set.of(1L, 3L, 6L);
        var expected = List.of(new Genre(1L, "Genre_1"), new Genre(3L, "Genre_3"), new Genre(6L, "Genre_6"));
        var found = genreRepository.findAllByIds(ids);
        assertThat(found)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expected);
    }
} 
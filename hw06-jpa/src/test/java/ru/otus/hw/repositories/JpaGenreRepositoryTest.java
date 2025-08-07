package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaGenreRepository.class)
@DisplayName("JPA-репозиторий жанров")
class JpaGenreRepositoryTest {

    @Autowired
    private JpaGenreRepository genreRepository;

    @Autowired
    private TestEntityManager em;

    private List<Genre> genres;

    @BeforeEach
    void setUp() {
        var g1 = em.persistAndFlush(new Genre(0, "Genre_1"));
        var g2 = em.persistAndFlush(new Genre(0, "Genre_2"));
        var g3 = em.persistAndFlush(new Genre(0, "Genre_3"));
        var g4 = em.persistAndFlush(new Genre(0, "Genre_4"));
        genres = List.of(g1, g2, g3, g4);
        em.clear();
    }

    @Test
    @DisplayName("должен находить все жанры")
    void shouldFindAllGenres() {
        var all = genreRepository.findAll();
        assertThat(all).hasSize(4);
        assertThat(all).allMatch(g -> g.getId() > 0 && g.getName() != null);
    }

    @Test
    @DisplayName("должен находить жанры по id-шникам")
    void shouldFindAllByIds() {
        var ids = Set.of(genres.get(0).getId(), genres.get(2).getId());
        var found = genreRepository.findAllByIds(ids);
        assertThat(found).hasSize(2);
        assertThat(found.stream().map(Genre::getId)).containsExactlyInAnyOrderElementsOf(ids);
    }
} 
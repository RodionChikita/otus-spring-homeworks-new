package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuthorRepository.class)
@DisplayName("JPA-репозиторий авторов")
class JpaAuthorRepositoryTest {

    @Autowired
    private JpaAuthorRepository authorRepository;

    @Autowired
    private TestEntityManager em;

    private List<Author> authors;

    @BeforeEach
    void setUp() {
        var a1 = em.persistAndFlush(new Author(0, "Author_1"));
        var a2 = em.persistAndFlush(new Author(0, "Author_2"));
        var a3 = em.persistAndFlush(new Author(0, "Author_3"));
        authors = List.of(a1, a2, a3);
        em.clear();
    }

    @Test
    @DisplayName("должен находить всех авторов")
    void shouldFindAllAuthors() {
        var all = authorRepository.findAll();
        assertThat(all).hasSize(3);
        assertThat(all).allMatch(a -> a.getId() > 0 && a.getFullName() != null);
    }

    @Test
    @DisplayName("должен находить автора по id")
    void shouldFindAuthorById() {
        var expected = authors.get(1);
        var actual = authorRepository.findById(expected.getId());
        assertThat(actual).isPresent();
        assertThat(actual.get().getFullName()).isEqualTo(expected.getFullName());
    }

    @Test
    @DisplayName("должен возвращать пусто по несуществующему id")
    void shouldReturnEmptyForMissingId() {
        assertThat(authorRepository.findById(999L)).isEmpty();
    }
} 
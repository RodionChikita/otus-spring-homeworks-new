package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;

import jakarta.persistence.EntityManager;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Интеграционный тест BookServiceImpl")
class BookServiceImplIT {

    @Autowired
    private BookService bookService;

    @Autowired
    private EntityManager em;

    @Test
    @Transactional
    @DisplayName("findById должен возвращать книгу с доступными author и genres")
    void findByIdShouldReturnBookWithAccessibleRelations() {
        // Готовим данные через EntityManager: сначала автор и жанры
        var author = new Author(0, "Author_IT");
        em.persist(author);
        var g1 = new Genre(0, "Genre_IT_1");
        var g2 = new Genre(0, "Genre_IT_2");
        em.persist(g1);
        em.persist(g2);
        em.flush();

        var saved = bookService.insert("Book_SVC", author.getId(), Set.of(g1.getId(), g2.getId()));

        var foundOpt = bookService.findById(saved.getId());
        assertThat(foundOpt).isPresent();
        var found = foundOpt.get();

        assertThat(found.getAuthor().getFullName()).isEqualTo("Author_IT");
        assertThat(found.getGenres()).hasSize(2);
        assertThat(found.getGenres().stream().map(Genre::getName)).containsExactlyInAnyOrder("Genre_IT_1", "Genre_IT_2");
    }
}
package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;

import jakarta.persistence.EntityManager;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(propagation = Propagation.NEVER)
@DisplayName("Интеграционный тест BookServiceImpl")
class BookServiceImplIT {

    @Autowired private BookService bookService;
    @Autowired private EntityManager em;
    @Autowired private PlatformTransactionManager txManager;

    private long authorId;
    private long g1Id;
    private long g2Id;

    @BeforeEach
    void setUp() {
        var tx = new TransactionTemplate(txManager);
        tx.executeWithoutResult(status -> {
            em.createNativeQuery("delete from books_genres").executeUpdate();
            em.createQuery("delete from Comment").executeUpdate();
            em.createQuery("delete from Book").executeUpdate();
            em.createQuery("delete from Author").executeUpdate();
            em.createQuery("delete from Genre").executeUpdate();

            var author = new Author(0, "Author_IT");
            em.persist(author);
            var g1 = new Genre(0, "Genre_IT_1");
            var g2 = new Genre(0, "Genre_IT_2");
            em.persist(g1);
            em.persist(g2);
            em.flush();

            authorId = author.getId();
            g1Id = g1.getId();
            g2Id = g2.getId();
        });
    }

    @Test
    @DisplayName("insert: должен сохранять книгу")
    void insert_ShouldSaveBook() {
        var saved = bookService.insert("Book_SVC", authorId, Set.of(g1Id, g2Id));
        assertThat(saved.getId()).isPositive();
        assertThat(saved.getTitle()).isEqualTo("Book_SVC");
        assertThat(saved.getAuthor().getFullName()).isEqualTo("Author_IT");
        assertThat(saved.getGenres()).hasSize(2);
    }

    @Test
    @DisplayName("findById: должен возвращать книгу с доступными author и genres")
    void findById_ShouldReturnBookWithAccessibleRelations() {
        var saved = bookService.insert("Book_SVC", authorId, Set.of(g1Id, g2Id));
        var foundOpt = bookService.findById(saved.getId());
        assertThat(foundOpt).isPresent();
        var found = foundOpt.get();

        assertThat(found.getAuthor().getFullName()).isEqualTo("Author_IT");
        assertThat(found.getGenres().stream().map(Genre::getName))
                .containsExactlyInAnyOrder("Genre_IT_1", "Genre_IT_2");
    }

    @Test
    @DisplayName("findAll: должен возвращать список книг с доступными author и genres")
    void findAll_ShouldReturnBooksWithAccessibleRelations() {
        bookService.insert("B1", authorId, Set.of(g1Id, g2Id));
        bookService.insert("B2", authorId, Set.of(g1Id));
        var all = bookService.findAll();
        assertThat(all).hasSize(2);
        all.forEach(b -> {
            assertThat(b.getAuthor().getFullName()).isEqualTo("Author_IT");
            assertThat(b.getGenres()).isNotEmpty();
        });
    }

    @Test
    @DisplayName("update: должен обновлять книгу")
    void update_ShouldUpdateBook() {
        var saved = bookService.insert("B1", authorId, Set.of(g1Id, g2Id));
        var updated = bookService.update(saved.getId(), "B1_edited", authorId, Set.of(g1Id));
        assertThat(updated.getTitle()).isEqualTo("B1_edited");
        assertThat(updated.getGenres()).hasSize(1);
    }

    @Test
    @DisplayName("deleteById: должен удалять книгу")
    void deleteById_ShouldDeleteBook() {
        var saved = bookService.insert("B1", authorId, Set.of(g1Id, g2Id));
        bookService.deleteById(saved.getId());
        assertThat(bookService.findById(saved.getId())).isEmpty();
    }
}
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
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.CommentRepository;

import jakarta.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import ru.otus.hw.models.Comment;

@SpringBootTest
@Transactional(propagation = Propagation.NEVER)
@DisplayName("Интеграционный тест CommentServiceImpl")
class CommentServiceImplIT {

    @Autowired private CommentService commentService;
    @Autowired private CommentRepository commentRepository;
    @Autowired private EntityManager em;
    @Autowired private PlatformTransactionManager txManager;

    private long bookId;

    @BeforeEach
    void setUp() {
        var tx = new TransactionTemplate(txManager);
        tx.executeWithoutResult(status -> {
            em.createQuery("delete from Comment").executeUpdate();
            em.createNativeQuery("delete from books_genres").executeUpdate();
            em.createQuery("delete from Book").executeUpdate();
            em.createQuery("delete from Author").executeUpdate();
            em.createQuery("delete from Genre").executeUpdate();

            var author = new Author(0, "Author_C_IT");
            em.persist(author);
            var g1 = new Genre(0, "G1");
            var g2 = new Genre(0, "G2");
            em.persist(g1); em.persist(g2);
            var book = new Book(0, "BookForComment", author, List.of(g1, g2));
            em.persist(book);
            em.flush();
            bookId = book.getId();
        });
    }

    @Test
    @DisplayName("insert: должен сохранять комментарий")
    void insert_ShouldSave() {
        var saved = commentService.insert("hello", bookId);
        assertThat(saved.getId()).isPositive();
        assertThat(saved.getText()).isEqualTo("hello");
        // Не трогаем saved.getBook() — связь ленивaя и по логике не нужна снаружи
    }

    @Test
    @DisplayName("findById: должен возвращать комментарий")
    void findById_ShouldReturn() {
        var saved = commentService.insert("hello", bookId);
        var foundOpt = commentService.findById(saved.getId());
        assertThat(foundOpt).isPresent();
        var found = foundOpt.get();
        assertThat(found.getText()).isEqualTo("hello");
    }

    @Test
    @DisplayName("findAllByBookId: должен возвращать список комментариев книги")
    void findAllByBookId_ShouldReturnList() {
        commentService.insert("c1", bookId);
        commentService.insert("c2", bookId);

        var list = commentService.findAllByBookId(bookId);
        assertThat(list).hasSize(2);
        assertThat(list.stream().map(Comment::getText))
                .containsExactlyInAnyOrder("c1", "c2");
    }

    @Test
    @DisplayName("update: должен обновлять комментарий")
    void update_ShouldUpdate() {
        var saved = commentService.insert("old", bookId);
        var updated = commentService.update(saved.getId(), "new");
        assertThat(updated.getText()).isEqualTo("new");
    }

    @Test
    @DisplayName("deleteById: должен удалять комментарий")
    void deleteById_ShouldDelete() {
        var saved = commentService.insert("to delete", bookId);
        commentService.deleteById(saved.getId());
        assertThat(commentRepository.findById(saved.getId())).isEmpty();
    }
}
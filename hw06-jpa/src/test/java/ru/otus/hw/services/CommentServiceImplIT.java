package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.CommentRepository;

import jakarta.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Интеграционный тест CommentServiceImpl")
class CommentServiceImplIT {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager em;

    @Test
    @Transactional
    @DisplayName("findById и findAllByBookId должны возвращать комментарии с доступной книгой")
    void shouldReturnCommentsWithAccessibleBookRelation() {
        var author = new Author(0, "Author_C_IT");
        em.persist(author);
        var g1 = new Genre(0, "G1");
        var g2 = new Genre(0, "G2");
        em.persist(g1);
        em.persist(g2);
        var book = new Book(0, "BookForComment", author, List.of(g1, g2));
        em.persist(book);
        em.flush();

        var savedComment = commentService.insert("hello", book.getId());

        var foundOpt = commentService.findById(savedComment.getId());
        assertThat(foundOpt).isPresent();
        var found = foundOpt.get();
        assertThat(found.getText()).isEqualTo("hello");
        assertThat(found.getBook().getTitle()).isEqualTo("BookForComment");

        var list = commentService.findAllByBookId(book.getId());
        assertThat(list).isNotEmpty();
        assertThat(list.get(0).getBook().getTitle()).isEqualTo("BookForComment");

        var updated = commentService.update(savedComment.getId(), "world");
        assertThat(updated.getText()).isEqualTo("world");
        assertThat(updated.getBook().getTitle()).isEqualTo("BookForComment");

        commentService.deleteById(savedComment.getId());
        assertThat(commentRepository.findById(savedComment.getId())).isEmpty();
    }
}
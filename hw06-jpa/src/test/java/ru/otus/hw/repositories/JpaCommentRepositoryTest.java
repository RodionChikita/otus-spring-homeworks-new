package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaCommentRepository.class)
@DisplayName("JPA-репозиторий комментариев")
class JpaCommentRepositoryTest {

    @Autowired
    private JpaCommentRepository commentRepository;

    @Autowired
    private TestEntityManager em;

    private Book book1;
    private Book book2;
    private List<Comment> comments;

    @BeforeEach
    void setUp() {
        var a1 = em.persistAndFlush(new Author(0, "Author_1"));
        var g1 = em.persistAndFlush(new Genre(0, "Genre_1"));
        var g2 = em.persistAndFlush(new Genre(0, "Genre_2"));
        var a2 = em.persistAndFlush(new Author(0, "Author_2"));
        var g3 = em.persistAndFlush(new Genre(0, "Genre_3"));

        book1 = em.persistAndFlush(new Book(0, "Book_1", a1, List.of(g1, g2)));
        book2 = em.persistAndFlush(new Book(0, "Book_2", a2, List.of(g3)));

        var c1 = em.persistAndFlush(new Comment(0, "Text_1", book1));
        var c2 = em.persistAndFlush(new Comment(0, "Text_2", book1));
        var c3 = em.persistAndFlush(new Comment(0, "Text_3", book2));
        comments = List.of(c1, c2, c3);
        em.clear();
    }

    @Test
    @DisplayName("должен находить комментарий по id")
    void shouldFindById() {
        var expected = comments.get(0);
        var actual = commentRepository.findById(expected.getId());
        assertThat(actual).isPresent();
        assertThat(actual.get().getText()).isEqualTo(expected.getText());
        assertThat(actual.get().getBook().getTitle()).isEqualTo(book1.getTitle());
    }

    @Test
    @DisplayName("должен находить комментарии по id книги")
    void shouldFindAllByBookId() {
        var list = commentRepository.findAllByBookId(book1.getId());
        assertThat(list).hasSize(2);
        assertThat(list).allMatch(c -> c.getBook().getId() == book1.getId());
    }

    @Test
    @DisplayName("должен сохранять и удалять комментарии")
    void shouldSaveAndDelete() {
        var saved = commentRepository.save(new Comment(0, "New", book2));
        em.flush();
        em.clear();

        var found = commentRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getText()).isEqualTo("New");

        commentRepository.deleteById(saved.getId());
        em.flush();
        em.clear();

        assertThat(commentRepository.findById(saved.getId())).isEmpty();
    }
} 
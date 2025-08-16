package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;

@DataJpaTest
@Import(JpaCommentRepository.class)
@Sql(scripts = {"/clean.sql","/test-data.sql"})
@DisplayName("JPA-репозиторий комментариев")
class JpaCommentRepositoryTest {

    private final JpaCommentRepository commentRepository;

    @Autowired
    JpaCommentRepositoryTest(JpaCommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Test
    @DisplayName("должен находить комментарий по id (без доступа к ленивой книге)")
    void shouldFindById() {
        // подготовим комментарий транзакционно через репозиторий
        var saved = commentRepository.save(new Comment(0, "Text_1", new Book(1L, null, null, null)));
        var actual = commentRepository.findById(saved.getId());
        assertThat(actual).isPresent();
        assertThat(actual.get().getText()).isEqualTo("Text_1");
    }

    @Test
    @DisplayName("должен находить комментарии по id книги")
    void shouldFindAllByBookId() {
        // добавим два коммента к книге 1
        commentRepository.save(new Comment(0, "C1", new Book(1L, null, null, null)));
        commentRepository.save(new Comment(0, "C2", new Book(1L, null, null, null)));

        var list = commentRepository.findAllByBookId(1L);
        assertThat(list)
                .usingRecursiveComparison()
                .ignoringFields("id", "book")
                .ignoringCollectionOrder()
                .isEqualTo(List.of(
                        new Comment(0, "C1", null),
                        new Comment(0, "C2", null)
                ));
    }

    @Test
    @DisplayName("должен сохранять и удалять комментарии")
    void shouldSaveAndDelete() {
        var saved = commentRepository.save(new Comment(0, "New", new Book(1L, null, null, null)));
        var found = commentRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getText()).isEqualTo("New");

        commentRepository.deleteById(saved.getId());
        assertThat(commentRepository.findById(saved.getId())).isEmpty();
    }
} 
package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.otus.hw.models.Comment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional(propagation = Propagation.NEVER)
@Sql(scripts = {"/clean.sql","/test-data.sql"})
@DisplayName("Интеграционный тест CommentServiceImpl")
class CommentServiceImplIT {

    @Autowired
    private CommentService commentService;

    @Test
    @DisplayName("insert: должен сохранять комментарий")
    void insert_ShouldSave() {
        var saved = commentService.insert("hello", 1L);
        var expected = new Comment(0, "hello", null);
        assertThat(saved)
                .usingRecursiveComparison()
                .ignoringFields("id", "book")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("findById: должен возвращать комментарий")
    void findById_ShouldReturn() {
        var saved = commentService.insert("hello", 1L);
        var foundOpt = commentService.findById(saved.getId());
        assertThat(foundOpt).isPresent();
        assertThat(foundOpt.get())
                .usingRecursiveComparison()
                .ignoringFields("id", "book")
                .isEqualTo(new Comment(0, "hello", null));
    }

    @Test
    @DisplayName("findByBookId: должен возвращать список комментариев книги")
    void findAllByBookId_ShouldReturnList() {
        commentService.insert("c1", 1L);
        commentService.insert("c2", 1L);

        var list = commentService.findAllByBookId(1L);
        var expected = List.of(
                new Comment(0, "c1", null),
                new Comment(0, "c2", null)
        );
        assertThat(list)
                .usingRecursiveComparison()
                .ignoringFields("id", "book")
                .ignoringCollectionOrder()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("update: должен обновлять комментарий")
    void update_ShouldUpdate() {
        var saved = commentService.insert("old", 1L);
        var updated = commentService.update(saved.getId(), "new");
        assertThat(updated)
                .usingRecursiveComparison()
                .ignoringFields("id", "book")
                .isEqualTo(new Comment(0, "new", null));
    }

    @Test
    @DisplayName("deleteById: должен удалять комментарий")
    void deleteById_ShouldDelete() {
        var saved = commentService.insert("to delete", 1L);
        commentService.deleteById(saved.getId());
        assertThat(commentService.findById(saved.getId())).isEmpty();
    }
}
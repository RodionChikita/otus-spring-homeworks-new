package ru.otus.hw.repositories;

import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends CrudRepository<Book, Long> {

    @EntityGraph("book-author-genres-entity-graph")
    @Override
    Optional<Book> findById(Long id);

    @Nonnull
    @EntityGraph("book-author-entity-graph")
    List<Book> findAll();
}

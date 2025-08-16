package ru.otus.hw.repositories;

import jakarta.annotation.Nonnull;
import org.springframework.data.repository.CrudRepository;
import ru.otus.hw.models.Author;

import java.util.List;

public interface AuthorRepository extends CrudRepository<Author, Long> {
    @Nonnull
    List<Author> findAll();
}

package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с книгами")
@DataJpaTest
@Import({JpaBookRepository.class})
class JpaBookRepositoryTest {

    @Autowired
    private JpaBookRepository bookRepository;

    @Autowired
    private TestEntityManager em;

    private List<Author> dbAuthors;
    private List<Genre> dbGenres;
    private List<Book> dbBooks;

    @BeforeEach
    void setUp() {
        Author author1 = em.persistAndFlush(new Author(0, "Author_1"));
        Author author2 = em.persistAndFlush(new Author(0, "Author_2"));
        Author author3 = em.persistAndFlush(new Author(0, "Author_3"));
        dbAuthors = List.of(author1, author2, author3);

        Genre genre1 = em.persistAndFlush(new Genre(0, "Genre_1"));
        Genre genre2 = em.persistAndFlush(new Genre(0, "Genre_2"));
        Genre genre3 = em.persistAndFlush(new Genre(0, "Genre_3"));
        Genre genre4 = em.persistAndFlush(new Genre(0, "Genre_4"));
        Genre genre5 = em.persistAndFlush(new Genre(0, "Genre_5"));
        Genre genre6 = em.persistAndFlush(new Genre(0, "Genre_6"));
        dbGenres = List.of(genre1, genre2, genre3, genre4, genre5, genre6);

        Book book1 = em.persistAndFlush(new Book(0, "BookTitle_1", author1, List.of(genre1, genre2)));
        Book book2 = em.persistAndFlush(new Book(0, "BookTitle_2", author2, List.of(genre3, genre4)));
        Book book3 = em.persistAndFlush(new Book(0, "BookTitle_3", author3, List.of(genre5, genre6)));
        dbBooks = List.of(book1, book2, book3);

        em.clear();
    }

    @DisplayName("должен загружать книгу по id")
    @Test
    void shouldReturnCorrectBookById() {
        Book expectedBook = dbBooks.get(0);
        var actualBook = bookRepository.findById(expectedBook.getId());
        
        assertThat(actualBook).isPresent();
        Book actualBookValue = actualBook.get();
        
        assertThat(actualBookValue.getId()).isEqualTo(expectedBook.getId());
        assertThat(actualBookValue.getTitle()).isEqualTo(expectedBook.getTitle());
        assertThat(actualBookValue.getAuthor().getId()).isEqualTo(expectedBook.getAuthor().getId());
        assertThat(actualBookValue.getGenres()).hasSize(expectedBook.getGenres().size());
    }

    @DisplayName("должен загружать список всех книг")
    @Test
    void shouldReturnCorrectBooksList() {
        var actualBooks = bookRepository.findAll();

        assertThat(actualBooks).hasSize(3);
        assertThat(actualBooks)
                .allMatch(book -> book.getId() > 0)
                .allMatch(book -> book.getTitle() != null)
                .allMatch(book -> book.getAuthor() != null)
                .allMatch(book -> book.getGenres() != null && !book.getGenres().isEmpty());
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    @Transactional
    void shouldSaveNewBook() {
        var expectedBook = new Book(0, "BookTitle_10500", dbAuthors.get(0), 
                List.of(dbGenres.get(0), dbGenres.get(2)));
        
        var returnedBook = bookRepository.save(expectedBook);
        em.flush();
        em.clear();
        
        assertThat(returnedBook).isNotNull();
        assertThat(returnedBook.getId()).isGreaterThan(0);
        assertThat(returnedBook.getTitle()).isEqualTo(expectedBook.getTitle());
        assertThat(returnedBook.getAuthor().getId()).isEqualTo(expectedBook.getAuthor().getId());
        assertThat(returnedBook.getGenres()).hasSize(2);

        var savedBook = bookRepository.findById(returnedBook.getId());
        assertThat(savedBook).isPresent();
        assertThat(savedBook.get().getTitle()).isEqualTo("BookTitle_10500");
    }

    @DisplayName("должен сохранять измененную книгу")
    @Test
    @Transactional
    void shouldSaveUpdatedBook() {
        Book existingBook = dbBooks.get(0);

        var updatedBook = new Book(existingBook.getId(), "Updated_Title", dbAuthors.get(2),
                List.of(dbGenres.get(4), dbGenres.get(5)));

        var returnedBook = bookRepository.save(updatedBook);
        em.flush();
        em.clear();

        assertThat(returnedBook).isNotNull();
        assertThat(returnedBook.getId()).isEqualTo(existingBook.getId());
        assertThat(returnedBook.getTitle()).isEqualTo("Updated_Title");
        assertThat(returnedBook.getAuthor().getId()).isEqualTo(dbAuthors.get(2).getId());

        var foundBook = bookRepository.findById(existingBook.getId());
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("Updated_Title");
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    @Transactional
    void shouldDeleteBook() {
        Book bookToDelete = dbBooks.get(0);
        long bookId = bookToDelete.getId();
        
        assertThat(bookRepository.findById(bookId)).isPresent();
        
        bookRepository.deleteById(bookId);
        em.flush();
        em.clear();
        
        assertThat(bookRepository.findById(bookId)).isEmpty();
    }

    @DisplayName("должен возвращать пустой Optional при поиске несуществующей книги")
    @Test
    void shouldReturnEmptyOptionalWhenBookNotFound() {
        var actualBook = bookRepository.findById(999L);
        assertThat(actualBook).isEmpty();
    }

    @DisplayName("должен корректно загружать связанные сущности (author и genres)")
    @Test
    void shouldLoadBookWithAssociations() {
        Book expectedBook = dbBooks.get(0);
        var actualBook = bookRepository.findById(expectedBook.getId());
        
        assertThat(actualBook).isPresent();
        Book book = actualBook.get();
        
        assertThat(book.getAuthor()).isNotNull();
        assertThat(book.getAuthor().getFullName()).isNotEmpty();
        
        assertThat(book.getGenres()).isNotNull();
        assertThat(book.getGenres()).isNotEmpty();
        assertThat(book.getGenres().get(0).getName()).isNotEmpty();
    }
}
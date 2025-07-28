package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final GenreRepository genreRepository;

    private final NamedParameterJdbcOperations namedParameterJdbcOperations;

    private final AuthorRepository authorRepository;

    @Override
    public Optional<Book> findById(long id) {
        String query = "select b.id as book_id, b.title, " +
                "a.id as author_id, a.full_name, " +
                "g.id as genre_id, g.name as genre_name " +
                "from books b " +
                "join authors a on b.author_id = a.id " +
                "left join books_genres bg on b.id = bg.book_id " +
                "left join genres g on bg.genre_id = g.id " +
                "where b.id = :id";

        Map<String, Object> params = Collections.singletonMap("id", id);

        Book book = namedParameterJdbcOperations.query(query, params, new BookResultSetExtractor());

        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var relations = getAllGenreRelations();
        var books = getAllBooksWithoutGenres();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        Map<String, Object> params = Collections.singletonMap("id", id);
        namedParameterJdbcOperations.update(
                "delete from books where id = :id", params
        );
    }

    private List<Book> getAllBooksWithoutGenres() {
        return namedParameterJdbcOperations.query("select id, title, author_id from books",
                new JdbcBookRepository.BookRowMapper(authorRepository));
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        return namedParameterJdbcOperations.query("select book_id, genre_id from books_genres",
                new JdbcBookRepository.BookGenreRelationRowMapper());
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres, List<BookGenreRelation> relations) {
        Map<Long, Genre> genreMap = genres.stream()
                .collect(Collectors.toMap(Genre::getId, Function.identity()));

        Map<Long, Set<Long>> bookGenresMap = relations.stream()
                .collect(Collectors.groupingBy(
                        BookGenreRelation::bookId,
                        Collectors.mapping(BookGenreRelation::genreId, Collectors.toSet())
                ));

        for (Book book : booksWithoutGenres) {
            Set<Long> genreIds = bookGenresMap.get(book.getId());

            List<Genre> bookGenres = (genreIds != null) ? genreIds.stream()
                    .map(genreMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
                    : Collections.emptyList();

            book.setGenres(bookGenres);
        }
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();
        SqlParameterSource parameterSource = new MapSqlParameterSource().addValue("title", book.getTitle())
                .addValue("author_id", book.getAuthor().getId());
        namedParameterJdbcOperations.update("insert into books (title, author_id) values (:title, :author_id)",
                parameterSource, keyHolder, new String[]{"id" });
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        SqlParameterSource parameters = new MapSqlParameterSource().addValue
                ("title", book.getTitle()).addValue("authorId", book.getAuthor().getId())
                .addValue("id", book.getId());

        int rowsAffected = namedParameterJdbcOperations
                .update("update books set title = :title, author_id = :authorId WHERE id = :id", parameters);

        if (rowsAffected == 0) {
            throw new EntityNotFoundException("No book found with id " + book.getId());
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        List<MapSqlParameterSource> batchArgs = book.getGenres().stream().map(genre ->
                new MapSqlParameterSource().addValue("bookId", book.getId()).addValue
                        ("genreId", genre.getId())).toList();

        namedParameterJdbcOperations
                .batchUpdate("insert into books_genres (book_id, genre_id) values (:bookId, :genreId)",
                        batchArgs.toArray(new SqlParameterSource[0]));
    }

    private void removeGenresRelationsFor(Book book) {
        Map<String, Object> params = Collections.singletonMap("id", book.getId());
        namedParameterJdbcOperations.update("delete from books_genres where book_id = :id", params);
    }

    @RequiredArgsConstructor
    private static class BookRowMapper implements RowMapper<Book> {

        private final AuthorRepository authorRepository;

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String title = rs.getString("title");
            Optional<Author> author = authorRepository.findById(rs.getLong("author_id"));
            return new Book(id, title, author.get(), null);
        }
    }

    private static class BookGenreRelationRowMapper implements RowMapper<BookGenreRelation> {

        @Override
        public BookGenreRelation mapRow(ResultSet rs, int rowNum) throws SQLException {
            long bookId = rs.getLong("book_id");
            long genreId = rs.getLong("genre_id");
            return new BookGenreRelation(bookId, genreId);
        }
    }

    // Использовать для findById
    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            Book book = null;
            while (rs.next()) {
                if (rs.isFirst()) {
                    book = new Book();
                    book.setId(rs.getLong("book_id"));
                    book.setTitle(rs.getString("title"));
                    Author author = new Author();
                    author.setId(rs.getLong("author_id"));
                    author.setFullName(rs.getString("full_name"));
                    book.setAuthor(author);
                    book.setGenres(new ArrayList<>());
                }
                long genreId = rs.getLong("genre_id");
                Genre genre = new Genre();
                genre.setId(genreId);
                genre.setName(rs.getString("genre_name"));
                List<Genre> genres = book.getGenres();
                genres.add(genre);
                book.setGenres(genres);
            }
            return book;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
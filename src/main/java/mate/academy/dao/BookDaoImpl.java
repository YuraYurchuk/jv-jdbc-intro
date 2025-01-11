package mate.academy.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import mate.academy.lib.Dao;
import mate.academy.model.Book;
import mate.academy.util.ConnectionUtil;
import mate.academy.exception.DataProcessingException;

@Dao
public class BookDaoImpl implements BookDao {
    @Override
    public Book create(Book book) {
        String sql = "INSERT INTO books(title, price) VALUES(?, ?)";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, book.getTitle());
            statement.setBigDecimal(2, book.getPrice());
            int affectedRows = statement.executeUpdate();
            if (affectedRows < 1) {
                throw new DataProcessingException("Can't create a book " + book);
            }
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                Long id = generatedKeys.getObject(1, Long.class);
                book.setId(id);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can not create connection to thr DB", e);
        }
        return book;
    }

    @Override
    public Optional<Book> findById(Long id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String title = resultSet.getString("title");
                BigDecimal price = resultSet.getBigDecimal("price");
                Book book = new Book();
                book.setId(id);
                book.setTitle(title);
                book.setPrice(price);
                return Optional.of(book);
            } else {
                throw new DataProcessingException("Can't find a book by id " + id);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can not create connection to thr DB", e);
        }
    }

    @Override
    public List<Book> findAll() {
        List<Optional<Book>> optionalResult = new ArrayList<>();
        String sql = "SELECT MAX(id) FROM books";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int maxRows = resultSet.getInt("MAX(id)");
                for (long i = 1L; i < maxRows + 1; i++) {
                    optionalResult.add(findById(i));
                }
            } else {
                throw new DataProcessingException("Can't find any books");
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can not create connection to thr DB", e);
        }
        return convertOptionalList(optionalResult);
    }

    @Override
    public Book update(Book book) {
        String sql = "UPDATE books SET title = ?, price = ? WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, book.getTitle());
            statement.setBigDecimal(2, book.getPrice());
            statement.setLong(3, book.getId());
            int affectedRows = statement.executeUpdate();
            if (affectedRows < 1) {
                throw new DataProcessingException("Can't update a book " + book);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Cannot create connection to the DB", e);
        }
        return book;
    }

    @Override
    public boolean deleteById(Long id) {
        int affectedRows;
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection connection = ConnectionUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            affectedRows = statement.executeUpdate();
            if (affectedRows < 1) {
                throw new DataProcessingException("Can't delete a book by id " + id);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Cannot create connection to the DB", e);
        }
        return affectedRows > 0;
    }

    private List<Book> convertOptionalList(List<Optional<Book>> book) {
        return book.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
package com.app.totalizator.dao.impl;

import com.app.totalizator.connection.ConnectionPool;
import com.app.totalizator.dao.UserDao;
import com.app.totalizator.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.app.totalizator.model.Role;
import static com.app.totalizator.dao.sql.UserQuery.*;


public class UserDaoImpl implements UserDao {
    private static final Logger logger = LogManager.getLogger();
    private final ConnectionPool connectionPool;


    public UserDaoImpl(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Optional<User> findById(Integer id) {
        ResultSet resultSet;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)){

            statement.setInt(1, id);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapResultSetToUser(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Error finding user by id: {}", id, e);
        }

        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        ResultSet resultSet;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL)){

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Error finding all users", e);
        }

        return users;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        ResultSet resultSet;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME)){

            statement.setString(1, username);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapResultSetToUser(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        ResultSet resultSet;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL)){

            statement.setString(1, email);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapResultSetToUser(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Error finding user by email: {}", email, e);
        }

        return Optional.empty();
    }

    @Override
    public User save(User user) {
        ResultSet generatedKeys;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)){

            changeUserStatement(user, statement);

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                user.setId(generatedKeys.getInt(1));
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }

            logger.info("User saved with id: {}", user.getId());
            return user;
        } catch (SQLException e) {
            logger.error("Error saving user", e);
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public boolean update(User user) {

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)){

            changeUserStatement(user, statement);
            statement.setInt(9, user.getId());

            int affectedRows = statement.executeUpdate();
            logger.info("User updated: {} rows affected", affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating user", e);
            return false;
        }
    }

    private void changeUserStatement(User user, PreparedStatement statement) throws SQLException {
        statement.setString(1, user.getUsername());
        statement.setString(2, user.getEmail());
        statement.setString(3, user.getPassword());
        statement.setString(4, user.getFirstName());
        statement.setString(5, user.getLastName());
        statement.setInt(6, user.getRole().getId());
        statement.setBigDecimal(7, user.getBalance());
        statement.setBoolean(8, user.isActive());
    }

    @Override
    public boolean deleteById(Integer id) {
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)){

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();
            logger.info("User deleted: {} rows affected", affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting user", e);
            return false;
        }
    }


    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setBalance(resultSet.getBigDecimal("balance"));
        user.setActive(resultSet.getBoolean("is_active"));

        Timestamp createdAt = resultSet.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        Role role = new Role();
        role.setId(resultSet.getInt("role_id"));
        role.setName(resultSet.getString("role_name"));
        role.setDescription(resultSet.getString("role_description"));
        user.setRole(role);

        return user;
    }
}

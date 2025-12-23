package com.app.totalizator.service.impl;

import com.app.totalizator.dao.UserDao;
import com.app.totalizator.model.Role;
import com.app.totalizator.model.User;
import com.app.totalizator.service.UserService;
import com.app.totalizator.service.factory.DaoFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserService.
 *
 * @author Totalizator Team
 * @version 1.0
 */
public class UserServiceImpl implements UserService {
    private static final Logger logger = LogManager.getLogger();
    private final UserDao userDao;

    public UserServiceImpl() {
        this.userDao = DaoFactory.getInstance().getUserDao();
    }

    public User createUserFromRequest(HttpServletRequest request) {
        return User.builder()
                .username(request.getParameter("username"))
                .email(request.getParameter("email"))
                .password(request.getParameter("password"))
                .firstName(request.getParameter("firstName"))
                .lastName(request.getParameter("lastName"))
                .balance(BigDecimal.ZERO)
                .role(new Role(3, "CLIENT", "Клиент"))
                .build();
    }

    public boolean checkUserFromRequest(HttpServletRequest request) {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (username.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean checkUserConfirmPasswordFromRequest(HttpServletRequest request) {
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!password.equals(confirmPassword)) {
            return false;
        }
        return true;
    }

    @Override
    public Optional<User> findById(Integer id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return userDao.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username.isBlank()) {
            return Optional.empty();
        }
        return userDao.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email.isBlank()) {
            return Optional.empty();
        }
        return userDao.findByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        if (username.isBlank() || password.isBlank()) {
            return Optional.empty();
        }

        Optional<User> userOptional = userDao.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String hashedPassword = hashPassword(password);
            if (hashedPassword.equals(user.getPassword()) && user.isActive()) {
                logger.info("User authenticated: {}", username);
                return Optional.of(user);
            }
        }
        logger.warn("Authentication failed for username: {}", username);
        return Optional.empty();
    }

    @Override
    public boolean deleteUser(Integer id) {
        if (id == null || id <= 0) {
            return false;
        }
        return userDao.deleteById(id);
    }

    @Override
    public boolean updateUser(User user) {
        if (user == null || user.getId() <= 0) {
            return false;
        }

        // If password is being updated, hash it
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // Check if password is already hashed (simple check)
            if (user.getPassword().length() < 60) {
                user.setPassword(hashPassword(user.getPassword()));
            }
        } else {
            // Keep existing password
            Optional<User> existingUser = userDao.findById(user.getId());
            if (existingUser.isPresent()) {
                user.setPassword(existingUser.get().getPassword());
            }
        }

        return userDao.update(user);
    }

    @Override
    public User register(User user) {
        validateUser(user);

        // Check if username already exists
        if (userDao.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userDao.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Hash password
        user.setPassword(hashPassword(user.getPassword()));

        // Set default role if not set (CLIENT)
        if (user.getRole() == null) {
            Role clientRole = new Role(3, "CLIENT", "Клиент");
            user.setRole(clientRole);
        }

        // Set default balance if not set
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }

        logger.info("Registering new user: {}", user.getUsername());
        return userDao.save(user);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (user.getUsername().length() < 3 || user.getUsername().length() > 50) {
            throw new IllegalArgumentException("Username must be between 3 and 50 characters");
        }
        if (user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error hashing password", e);
            throw new RuntimeException("Password hashing failed", e);
        }
    }
}

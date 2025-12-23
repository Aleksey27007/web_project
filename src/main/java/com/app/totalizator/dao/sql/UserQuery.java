package com.app.totalizator.dao.sql;

public class UserQuery {

    public static final String FIND_BY_ID = "SELECT u.id, u.username, u.email, u.password, " +
            "u.first_name, u.last_name, u.role_id, u.balance, u.is_active, u.created_at, u.updated_at, " +
            "r.name as role_name, r.description as role_description " +
            "FROM users u JOIN roles r ON u.role_id = r.id WHERE u.id = ?";

    public static final String FIND_ALL = "SELECT u.id, u.username, u.email, u.password, " +
            "u.first_name, u.last_name, u.role_id, u.balance, u.is_active, u.created_at, u.updated_at, " +
            "r.name as role_name, r.description as role_description " +
            "FROM users u JOIN roles r ON u.role_id = r.id ORDER BY u.id";

    public static final String FIND_BY_USERNAME = "SELECT u.id, u.username, u.email, u.password, " +
            "u.first_name, u.last_name, u.role_id, u.balance, u.is_active, u.created_at, u.updated_at, " +
            "r.name as role_name, r.description as role_description " +
            "FROM users u JOIN roles r ON u.role_id = r.id WHERE u.username = ?";

    public static final String FIND_BY_EMAIL = "SELECT u.id, u.username, u.email, u.password, " +
            "u.first_name, u.last_name, u.role_id, u.balance, u.is_active, u.created_at, u.updated_at, " +
            "r.name as role_name, r.description as role_description " +
            "FROM users u JOIN roles r ON u.role_id = r.id WHERE u.email = ?";

    public static final String INSERT = "INSERT INTO users (username, email, password, first_name, " +
            "last_name, role_id, balance, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String UPDATE = "UPDATE users SET username = ?, email = ?, password = ?, " +
            "first_name = ?, last_name = ?, role_id = ?, balance = ?, is_active = ? WHERE id = ?";

    public static final String DELETE = "DELETE FROM users WHERE id = ?";
}

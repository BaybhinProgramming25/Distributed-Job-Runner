package com.example.repository;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.dto.User;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public User save(String username, String email, String passwordHash, String phoneNumber) {

        return jdbc.queryForObject("""
            INSERT INTO dist_jobs_scheduler.users (username, email, password_hash, phone_number)
            VALUES (?, ?, ?, ?)
            RETURNING id, username, email, password_hash, phone_number
            """,
            this::mapRow,
            username, email, passwordHash, phoneNumber
        );
    }

    public Optional<User> findByUsername(String username) {

        return jdbc.query("""
            SELECT id, username, email, password_hash, phone_number
            FROM dist_jobs_scheduler.users
            WHERE username = ?
            """,
            this::mapRow,
            username)
        .stream().findFirst();
    }

    public boolean existsByUsernameOrEmail(String username, String email) {

        Integer count = jdbc.queryForObject("""
            SELECT COUNT(*) FROM dist_jobs_scheduler.users
            WHERE username = ? OR email = ?    
            """,
            Integer.class, username, email);
            
        return count != null && count > 0;
    }

    private User mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {

        return new User(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password_hash"),
            rs.getString("phone_number")
        );
    }
    
}

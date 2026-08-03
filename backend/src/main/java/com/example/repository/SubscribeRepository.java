package com.example.repository;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SubscribeRepository {

    private final JdbcTemplate jdbc;

    public SubscribeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<String> findUsernamesWatching(String company) {

        return jdbc.queryForList("""
                SELECT u.username
                FROM dist_jobs_scheduler.users u
                JOIN dist_jobs_scheduler.watched_companies w ON w.user_id = u.id
                WHERE w.company_name = ?
                """, 
                String.class, company
            );
    }

    public void saveUserCompanies(Long id, List<String> companies) {

        jdbc.update("DELETE FROM dist_jobs_scheduler.watched_companies WHERE user_id = ?", id);

        jdbc.batchUpdate("INSERT INTO dist_jobs_scheduler.watched_companies (user_id, company_name) VALUES (?, ?)", companies, companies.size(), (ps, company) -> {
            ps.setLong(1, id);
            ps.setString(2, company);
        });
    }    
}

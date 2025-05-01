package ch.epai.ict.m295.messaging.backend.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserBuilder;
import ch.epai.ict.m295.messaging.backend.domain.UserRepository;
import ch.epai.ict.m295.messaging.backend.domain.UserRoles;


public class SqlUserRepository implements UserRepository {

    private class UserRowMapper implements RowMapper<User> {

        @Override
        @Nullable
        public User mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return UserBuilder.create()
                .setId(rs.getLong("user_id"))
                .setUsername(rs.getString("username"))
                .setDisplayName(rs.getString("display_name"))
                .setRole(UserRoles.valueOf(rs.getString("user_role")))
                .build();
        }
    }

    NamedParameterJdbcTemplate jdbcTemplate;

    public SqlUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<User> getUsers() {
        return jdbcTemplate.query("SELECT * FROM user;", new UserRowMapper());
    }

    @Override
    public User getUser(long id) {
        try {
            return this.jdbcTemplate.queryForObject(
                "SELECT * FROM user WHERE user_id = :id", 
                new MapSqlParameterSource("id", id),
                new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public User getUserByUsername(String username) {
        try {
            return this.jdbcTemplate.queryForObject(
                "SELECT * FROM user WHERE username = :username", 
                new MapSqlParameterSource("username", username),
                new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void createUser(User user, String password) {
        jdbcTemplate.update(
            "INSERT INTO user (user_id, username, display_name, user_password, user_role) VALUE (:id, :username, :display_name, :user_password, :user_role)",
            new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("username", user.getUsername())
                .addValue("display_name", user.getDisplayName())
                .addValue("user_password", new BCryptPasswordEncoder().encode(password))
                .addValue("user_role", user.getRole().name()));
    }

    @Override
    public void deleteUser(long id) {
        jdbcTemplate.update(
            "DELETE FROM user WHERE id = :id)",
            new MapSqlParameterSource("id", id));
    }

    @Override
    public boolean validate(String username, String password) {
        SqlRowSet rs = this.jdbcTemplate.queryForRowSet(
            "SELECT user_password FROM user WHERE username = :username", 
            new MapSqlParameterSource("username", username));
        if (rs.next()) {
            String cipheredPassword = rs.getString("user_password");
            return new BCryptPasswordEncoder().matches(password, cipheredPassword);
        }
        return false;
    }

    @Override
    public void updateUser(User user) {
        jdbcTemplate.update(
            "UPDATE user SET username = :username, display_name = :display_name WHERE user_id = :id",
            new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("username", user.getUsername())
                .addValue("display_name", user.getDisplayName()));
    }

    @Override
    public void updateUserPassword(long id, String password) {
        jdbcTemplate.update(
            "UPDATE user SET user_password = :user_password WHERE user_id = :id",
            new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("user_password", new BCryptPasswordEncoder().encode(password)));
    }
}

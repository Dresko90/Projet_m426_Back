package ch.epai.ict.m295.messaging.backend.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
import ch.epai.ict.m295.messaging.backend.domain.security.Token;
import ch.epai.ict.m295.messaging.backend.domain.security.TokenRepository;

public class SqlUserRepository implements UserRepository, TokenRepository {

    private class UserRowMapper implements RowMapper<User> {

        @Override
        @Nullable
        public User mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return UserBuilder.create()
                .setId(rs.getLong("user_id"))
                .setEmail(rs.getString("email"))
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
        return this.jdbcTemplate.queryForObject(
            "SELECT * FROM user WHERE user_id = :id", 
            new MapSqlParameterSource("id", id),
            new UserRowMapper());
    }

    @Override
    public User getUserByEmail(String email) {
        return this.jdbcTemplate.queryForObject(
            "SELECT * FROM user WHERE email = :email", 
            new MapSqlParameterSource("email", email),
            new UserRowMapper());
    }

    @Override
    public void createUser(User user, String password) {
        jdbcTemplate.update(
            "INSERT INTO user (user_id, email, display_name, user_password, user_role) VALUE (:id, :email, :display_name, :user_password, :user_role)",
            new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("email", user.getEmail())
                .addValue("display_name", user.getDisplayName())
                .addValue("user_password", new BCryptPasswordEncoder().encode(password))
                .addValue("user_role", user.getRole().name()));
    }

    @Override
    public void deleteUser(long id) {
        jdbcTemplate.update(
            "DELETE user WHERE id = :id)",
            new MapSqlParameterSource("id", id));
    }

    @Override
    public boolean validate(String email, String password) {
        SqlRowSet rs = this.jdbcTemplate.queryForRowSet(
            "SELECT user_password FROM user WHERE email = :email", 
            new MapSqlParameterSource("email", email));
        if (rs.next()) {
            String cipheredPassword = rs.getString("user_password");
            return new BCryptPasswordEncoder().matches(password, cipheredPassword);
        }
        return false;
    }

    @Override
    public void deleteToken(Token token) {
        jdbcTemplate.update(
            "DELETE token WHERE token = :token)",
            new MapSqlParameterSource("token", token.toString()));
    }

    @Override
    public void addToken(Token token, User user) {
        jdbcTemplate.update(
            "INSERT INTO token (user_id, token) VALUE (:user_id, :token)",
            new MapSqlParameterSource()
                .addValue("user_id", user.getId())
                .addValue("token", token.toString()));
    }

    @Override
    public User getUserFromToken(Token token) {
        return this.jdbcTemplate.queryForObject(
            "SELECT user.* FROM user INNER JOIN token ON token.user_id = user.user_id WHERE token = :token", 
            new MapSqlParameterSource("token", token.toString()),
            new UserRowMapper());
    }
}

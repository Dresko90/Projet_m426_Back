package ch.epai.ict.m295.messaging.backend.data;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.security.Token;
import ch.epai.ict.m295.messaging.backend.domain.security.TokenRepository;

public class SqlTokenRepository implements TokenRepository {

    NamedParameterJdbcTemplate jdbcTemplate;

    public SqlTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public void deleteToken(Token token) {
        jdbcTemplate.update(
            """
            DELETE FROM token 
            WHERE user_id = (
                SELECT user_id 
                FROM token
                WHERE token = :token);
            """,
            new MapSqlParameterSource("token", token.toString()));
    }

    @Override
    public void deleteAllUserToken(User user) {
        jdbcTemplate.update(
            """
            DELETE FROM token 
            WHERE user_id = :user_id;
            """,
            new MapSqlParameterSource("user_id", user.getId()));
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
        try {
            return this.jdbcTemplate.queryForObject(
                """
                SELECT user.* 
                FROM user 
                INNER JOIN token ON token.user_id = user.user_id 
                WHERE token = :token
                """, 
                new MapSqlParameterSource("token", token.toString()),
                new UserRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}


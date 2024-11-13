package ch.epai.ict.m295.message_api.data;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ch.epai.ict.m295.message_api.domain.User;
import ch.epai.ict.m295.message_api.domain.UserDirectory;

public class SqlUserRepository implements UserDirectory {

    NamedParameterJdbcTemplate jdbcTemplate;

    public SqlUserRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
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
            "DELETE  user WHERE id = :id)",
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
}

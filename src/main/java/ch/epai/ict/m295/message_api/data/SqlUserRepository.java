package ch.epai.ict.m295.message_api.data;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import ch.epai.ict.m295.message_api.domain.User;
import ch.epai.ict.m295.message_api.domain.UserDirectory;

public class SqlUserRepository implements UserDirectory {

    NamedParameterJdbcTemplate jdbcTemplate;

    public SqlUserRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<User> getUsers() {
        RowMapper<User> rowMapper = new UserRowMapper();
        return jdbcTemplate.query("SELECT * FROM user;", rowMapper);
    }

    @Override
    public User getUser(long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUser'");
    }

    @Override
    public void createUser(User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }

    @Override
    public void deleteUser(long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

}

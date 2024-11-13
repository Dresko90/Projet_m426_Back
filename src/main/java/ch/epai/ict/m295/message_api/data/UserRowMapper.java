package ch.epai.ict.m295.message_api.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import ch.epai.ict.m295.message_api.domain.User;
import ch.epai.ict.m295.message_api.domain.UserBuilder;
import ch.epai.ict.m295.message_api.domain.UserRoles;

public class UserRowMapper implements RowMapper<User> {

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
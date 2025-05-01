package ch.epai.ict.m295.messaging.backend.data;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserBuilder;
import ch.epai.ict.m295.messaging.backend.domain.UserRoles;

public class UserRowMapper implements RowMapper<User> {

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

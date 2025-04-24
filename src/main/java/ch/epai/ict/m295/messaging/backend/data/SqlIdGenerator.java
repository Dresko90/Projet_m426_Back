package ch.epai.ict.m295.messaging.backend.data;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import ch.epai.ict.m295.messaging.backend.domain.IdGenerator;

public class SqlIdGenerator implements IdGenerator {

    private SimpleJdbcCall simpleJdbcCall;
    private String tableName;

    public SqlIdGenerator(JdbcTemplate jdbcTemplate, String tableName) {
        this.simpleJdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("get_next_id");
        this.tableName = tableName;
    }

    @Override
    public long getNextId() {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("entity_name", this.tableName)
            .addValue("next_id", null);

        Map<String, Object> res = this.simpleJdbcCall.execute(parameters);
        return (long) res.get("next_id");
    }
}

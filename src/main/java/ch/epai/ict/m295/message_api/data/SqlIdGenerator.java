package ch.epai.ict.m295.message_api.data;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import ch.epai.ict.m295.message_api.domain.IdGenerator;

public class SqlIdGenerator implements IdGenerator {

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcCall simpleJdbcCall;
    private String tableName;

    public SqlIdGenerator(DataSource dataSource, String tableName) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
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

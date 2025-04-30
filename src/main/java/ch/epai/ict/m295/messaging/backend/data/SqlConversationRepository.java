package ch.epai.ict.m295.messaging.backend.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ch.epai.ict.m295.messaging.backend.domain.Conversation;
import ch.epai.ict.m295.messaging.backend.domain.ConversationBuilder;
import ch.epai.ict.m295.messaging.backend.domain.ConversationRepository;
import ch.epai.ict.m295.messaging.backend.domain.Participant;
import ch.epai.ict.m295.messaging.backend.domain.ParticipantBuilder;
import ch.epai.ict.m295.messaging.backend.domain.User;

public class SqlConversationRepository implements ConversationRepository {

    private class ParticipantRowMapper implements RowMapper<Participant> {
        @Override
        public Participant mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return new ParticipantBuilder()
                .setId(rs.getLong("user_id"))
                .setUsername(rs.getString("username"))
                .setRole(Participant.Role.valueOf(rs.getString("participant_role")))
                .setStatus(Participant.Status.valueOf(rs.getString("participant_status")))
                .build();
        }
    }

    private class ConversationRowMapper implements RowMapper<Conversation> {
        @Override
        public Conversation mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return ConversationBuilder.create()
                .setId(rs.getLong("conversation_id"))
                .setParticipants(getParticipants(rs.getLong("conversation_id")))
                .build();
        }
    }

    private class CreateConversationTransaction implements TransactionCallback<Object> {
        private final Conversation conversation;

        public CreateConversationTransaction(Conversation conversation) {
            this.conversation = conversation;
        }

        @Override
        public Object doInTransaction(@NonNull TransactionStatus status) {
            try {
                jdbcTemplate.update(
                    "INSERT INTO conversation (conversation_id) VALUES (:conversationId)",
                    new MapSqlParameterSource("conversationId", conversation.getId())
                );

                for (Participant participant : conversation.getParticipants()) {
                    addParticipant(conversation.getId(), participant);
                }
            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }
            return null;
        }
    }

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SqlConversationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public void createConversation(Conversation conversation) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        CreateConversationTransaction transaction = new CreateConversationTransaction(conversation);
        transactionTemplate.execute(transaction);
    }

    @Override
    public Conversation getConversation(long conversationId) {
        return jdbcTemplate.queryForObject(
            "SELECT * FROM conversation WHERE conversation_id = :conversationId",
            new MapSqlParameterSource("conversationId", conversationId),
            new ConversationRowMapper()
        );
    }

    @Override
    public List<Conversation> findConversationsByUser(User user) {
        return jdbcTemplate.query(
            "SELECT DISTINCT c.conversation_id " +
            "FROM conversation c " +
            "INNER JOIN participant p ON c.conversation_id = p.conversation_id " +
            "WHERE p.user_id = :userId",
            new MapSqlParameterSource("userId", user.getId()),
            new ConversationRowMapper()
        );
    }

    @Override
    public void updateConversation(Conversation conversation) {
        jdbcTemplate.update(
            "DELETE FROM participant WHERE conversation_id = :conversationId",
            new MapSqlParameterSource("conversationId", conversation.getId())
        );

        for (Participant participant : conversation.getParticipants()) {
            addParticipant(conversation.getId(), participant);
        }
    }

    @Override
    public void addParticipant(long conversationId, Participant participant) {
        jdbcTemplate.update(
            "INSERT INTO participant (conversation_id, user_id, participant_role, participant_status) " +
            "VALUES (:conversationId, :userId, :role, :status)",
            new MapSqlParameterSource()
                .addValue("conversationId", conversationId)
                .addValue("userId", participant.getUserId())
                .addValue("role", participant.getRole().name())
                .addValue("status", participant.getStatus().name())
        );
    }

    @Override
    public void removeParticipant(long conversationId, long userId) {
        jdbcTemplate.update(
            "DELETE FROM participant WHERE conversation_id = :conversationId AND user_id = :userId",
            new MapSqlParameterSource()
                .addValue("conversationId", conversationId)
                .addValue("userId", userId)
        );
    }

    @Override
    public void deleteConversation(long conversationId) {
        jdbcTemplate.update(
            "DELETE FROM conversation WHERE conversation_id = :conversationId",
            new MapSqlParameterSource("conversationId", conversationId)
        );
    }

    private List<Participant> getParticipants(long conversationId) {
        return jdbcTemplate.query(
            "SELECT p.user_id, u.username, p.participant_role, p.participant_status " +
            "FROM participant p " +
            "INNER JOIN user u ON p.user_id = u.user_id " +
            "WHERE p.conversation_id = :conversationId",
            new MapSqlParameterSource("conversationId", conversationId),
            new ParticipantRowMapper()
        );
    }
}
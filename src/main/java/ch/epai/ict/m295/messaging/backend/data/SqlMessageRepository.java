package ch.epai.ict.m295.messaging.backend.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ch.epai.ict.m295.messaging.backend.domain.Message;
import ch.epai.ict.m295.messaging.backend.domain.MessageBuilder;
import ch.epai.ict.m295.messaging.backend.domain.MessageRepository;
import ch.epai.ict.m295.messaging.backend.domain.MessageStatus;
import ch.epai.ict.m295.messaging.backend.domain.MessageStatusBuilder;


public class SqlMessageRepository implements MessageRepository {

    private class CreateMessageTransaction implements TransactionCallback<Object> {
        private final Message message;

        public CreateMessageTransaction(Message message) {
            this.message = message;
        }

        @Override
        public Object doInTransaction(@NonNull TransactionStatus status) {
            try {
                jdbcTemplate.update(
                    "INSERT INTO message (message_id, conversation_id, sender_id, body) " +
                    "VALUES (:messageId, :conversationId, :senderId, :content)",
                    new MapSqlParameterSource()
                        .addValue("messageId", message.getId())
                        .addValue("conversationId", message.getConversationId())
                        .addValue("senderId", message.getSenderId())
                        .addValue("content", message.getBody())
                );
        
                for(MessageStatus messageStatus: message.getMessageStatus()) {
                    jdbcTemplate.update(
                        "INSERT INTO message_status (message_id, user_id, read_at, deleted) " +
                        "VALUES (:messageId, :userId, :readAt, :deleted)",
                        new MapSqlParameterSource()
                            .addValue("messageId", message.getId())
                            .addValue("userId", messageStatus.getUserId())
                            .addValue("readAt", messageStatus.getReadAt())
                            .addValue("deleted", messageStatus.isDeleted())
                    );
                }
            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }
            return null;
        }
    }

    private class MessageRowMapper implements RowMapper<Message> {
        @Override
        @Nullable
        public Message mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return MessageBuilder.create()
                .setId(rs.getLong("message_id"))
                .setConversationId(rs.getLong("conversation_id"))
                .setSenderId(rs.getLong("sender_id"))
                .setContent(rs.getString("body"))
                .setSentDateTime(rs.getTimestamp("send_at").toLocalDateTime())
                .setMessageStatus(getMessageStatus(rs.getLong("message_id")))
                .build();
        }
    }

    public class MessageStatusRowMapper implements RowMapper<MessageStatus> {
        @Override
        @Nullable
        public MessageStatus mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return MessageStatusBuilder.create()
                .setUserId(rs.getLong("user_id"))
                .setReadAt(rs.getTimestamp("read_at") != null ? rs.getTimestamp("read_at").toLocalDateTime() : null)
                .setDeleted(rs.getBoolean("deleted"))
                .build();
        }
    }   

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SqlMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public void createMessage(Message message) {
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        CreateMessageTransaction transaction = new CreateMessageTransaction(message);
        transactionTemplate.execute(transaction);
    }

    @Override
    public List<Message> getMessages(long conversationId) {
        return jdbcTemplate.query(
            "SELECT * FROM message WHERE conversation_id = :conversationId ORDER BY send_at ASC",
            new MapSqlParameterSource("conversationId", conversationId),
            new MessageRowMapper()
        );
    }

    @Override
    public void updateMessageStatusForUser(long messageId, long userId) {
        jdbcTemplate.update(
            "UPDATE message_status SET read_at = CURRENT_TIMESTAMP WHERE message_id = :messageId AND user_id = :userId",
            new MapSqlParameterSource()
                .addValue("messageId", messageId)
                .addValue("userId", userId)
        );
    }

    @Override
    public void deleteMessageForUser(long messageId, long userId) {
        jdbcTemplate.update(
            "UPDATE message_status SET deleted = TRUE WHERE message_id = :messageId AND user_id = :userId",
            new MapSqlParameterSource()
                .addValue("messageId", messageId)
                .addValue("userId", userId)
        );
    }

    private List<MessageStatus> getMessageStatus(long messageId) {
        return jdbcTemplate.query(
            "SELECT * FROM message_status WHERE message_id = :messageId",
            new MapSqlParameterSource("messageId", messageId),
            new MessageStatusRowMapper()
        );
    } 
}
package ch.epai.ict.m295.messaging.backend.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ch.epai.ict.m295.messaging.backend.domain.Conversation;
import ch.epai.ict.m295.messaging.backend.domain.ConversationBuilder;
import ch.epai.ict.m295.messaging.backend.domain.ConversationRepository;
import ch.epai.ict.m295.messaging.backend.domain.Message;
import ch.epai.ict.m295.messaging.backend.domain.MessageBuilder;
import ch.epai.ict.m295.messaging.backend.domain.MessageStatus;
import ch.epai.ict.m295.messaging.backend.domain.MessageStatusBuilder;
import ch.epai.ict.m295.messaging.backend.domain.Participant;
import ch.epai.ict.m295.messaging.backend.domain.ParticipantBuilder;
import ch.epai.ict.m295.messaging.backend.domain.User;

public class SqlConversationRepository implements ConversationRepository {

    private class ParticipantRowMapper implements RowMapper<Participant> {
        @Override
        public Participant mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return ParticipantBuilder.create()
                .setId(rs.getLong("user_id"))
                .setConversationId(rs.getLong("conversation_id"))
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
                .setIsGroup(rs.getBoolean("is_group"))
                .setParticipants(getParticipants(rs.getLong("conversation_id")))
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

    private class CreateConversationTransaction implements TransactionCallback<Object> {
        private final Conversation conversation;

        public CreateConversationTransaction(Conversation conversation) {
            this.conversation = conversation;
        }

        @Override
        public Object doInTransaction(@NonNull TransactionStatus status) {
            try {
                jdbcTemplate.update(
                    """
                    INSERT INTO conversation (conversation_id, is_group) 
                    VALUES (:conversationId, :isGroup)
                    """,
                    new MapSqlParameterSource()
                        .addValue("conversationId", conversation.getId())
                        .addValue("isGroup", conversation.isGroup())
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

    private class MessageRowMapper implements RowMapper<Message> {
        @Override
        @Nullable
        public Message mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            return MessageBuilder.create()
                .setId(rs.getLong("message_id"))
                .setConversationId(rs.getLong("conversation_id"))
                .setSenderId(rs.getLong("sender_id"))
                .setBody(rs.getString("body"))
                .setSentDateTime(rs.getTimestamp("send_at").toLocalDateTime())
                .setMessageStatus(getMessageStatus(rs.getLong("message_id")))
                .build();
        }
    }

    private class CreateMessageTransaction implements TransactionCallback<Object> {
        private final Message message;

        public CreateMessageTransaction(Message message) {
            this.message = message;
        }

        @Override
        public Object doInTransaction(@NonNull TransactionStatus status) {
            try {
                jdbcTemplate.update(
                    """
                    UPDATE participant p
                    JOIN conversation c ON c.conversation_id = p.conversation_id
                    SET p.participant_status = "ACTIVE"
                    WHERE c.conversation_id = :conversationId AND NOT c.is_group;
                    """,
                    new MapSqlParameterSource("conversationId", message.getConversationId())
                );
                jdbcTemplate.update(
                    """
                    INSERT INTO message (message_id, conversation_id, sender_id, body)
                    VALUES (:messageId, :conversationId, :senderId, :body)
                    """,
                    new MapSqlParameterSource()
                        .addValue("messageId", message.getId())
                        .addValue("conversationId", message.getConversationId())
                        .addValue("senderId", message.getSenderId())
                        .addValue("body", message.getBody())
                );
                for(MessageStatus messageStatus: message.getMessageStatus()) {
                    jdbcTemplate.update(
                        """
                        INSERT INTO message_status 
                            (message_id, user_id, read_at, deleted)
                        SELECT :messageId, :userId, :readAt, :deleted
                        FROM participant
                        WHERE conversation_id = :conversationId
                            AND user_id = :userId 
                            AND participant_status = "ACTIVE"
                        """,
                        new MapSqlParameterSource()
                            .addValue("conversationId", message.getConversationId())
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

    private class AddParticipantsTransaction implements TransactionCallback<Object> {
        private final long conversationId;
        private final List<Participant> participants;

        public AddParticipantsTransaction(long conversationId, List<Participant> participants) {
            this.conversationId = conversationId;
            this.participants = participants;
        }

        @Override
        public Object doInTransaction(@NonNull TransactionStatus status) {
            try {
                for (Participant participant : participants) {
                    addParticipant(conversationId, participant);
                }
            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }
            return null;
        }
    }


    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final PlatformTransactionManager transactionManager;

    public SqlConversationRepository(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.transactionManager = transactionManager;
    }

    @Override
    public void createConversation(Conversation conversation) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
        CreateConversationTransaction transaction = new CreateConversationTransaction(conversation);
        transactionTemplate.execute(transaction);
    }

    @Override
    public Conversation getConversationById(long conversationId) {
        try {
            return jdbcTemplate.queryForObject(
                """
                SELECT * 
                FROM conversation
                WHERE conversation_id = :conversationId
                """,
                new MapSqlParameterSource("conversationId", conversationId),
                new ConversationRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Conversation> getConversationsByUser(User user, int pageNumber, int pageSize) {
        return jdbcTemplate.query(
            """
            SELECT DISTINCT c.*, MAX(m.send_at) as last_message
            FROM conversation c
                INNER JOIN participant p ON c.conversation_id = p.conversation_id
                LEFT JOIN message m ON c.conversation_id = m.conversation_id
            WHERE p.user_id = :userId 
                AND p.participant_status != "INACTIVE"
            GROUP BY c.conversation_id
            ORDER BY last_message DESC
            LIMIT :limit OFFSET :offset;
            """,
            new MapSqlParameterSource()
                .addValue("userId", user.getId())
                .addValue("limit", pageSize)
                .addValue("offset", pageNumber * pageSize),
            new ConversationRowMapper()
        );
    }

    @Override
    public long getNumberOfConvesationForUser(User user) {
        return Objects.requireNonNullElse(
            jdbcTemplate.queryForObject(
                """
                SELECT COUNT(p.user_id) 
                FROM conversation c
                INNER JOIN participant p ON c.conversation_id = p.conversation_id
                WHERE p.user_id = :userId AND p.participant_status != "INACTIVE"
                """,
                new MapSqlParameterSource("userId", user.getId()), 
                Long.class),
            0L
        );
    }

    @Override
    public List<Conversation> getConversationsByParticipants(Participant participant1, Participant participant2) {
        return jdbcTemplate.query(
            """
            SELECT c.*
            FROM conversation c 
                INNER JOIN participant p1 ON c.conversation_id = p1.conversation_id 
                INNER JOIN participant p2 ON c.conversation_id = p2.conversation_id 
            WHERE NOT c.is_group 
                AND p1.user_id = :participantId1 
                AND p2.user_id = :participantId2
            ORDER BY c.conversation_id DESC
            LIMIT 1;
            """,
            new MapSqlParameterSource()
                .addValue("participantId1", participant1.getUserId())
                .addValue("participantId2", participant2.getUserId()),
            new ConversationRowMapper()
        );
    }

    @Override
    public void updateConversation(Conversation conversation) {
        jdbcTemplate.update(
            """
            DELETE FROM participant 
            WHERE conversation_id = :conversationId
            """,
            new MapSqlParameterSource("conversationId", conversation.getId())
        );

        for (Participant participant : conversation.getParticipants()) {
            addParticipant(conversation.getId(), participant);
        }
    }

    @Override
    public void addParticipants(long conversationId, List<Participant> participants) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
        AddParticipantsTransaction transaction = new AddParticipantsTransaction(conversationId, participants);
        transactionTemplate.execute(transaction);
    }

    @Override
    public void addParticipant(long conversationId, Participant participant) {
        jdbcTemplate.update(
            """
            INSERT INTO participant 
                (conversation_id, user_id, participant_role, participant_status)
            VALUES 
                (:conversationId, :userId, :role, :status)
            ON DUPLICATE KEY UPDATE
                participant_role = :role, participant_status = :status
            """,
            new MapSqlParameterSource()
                .addValue("conversationId", conversationId)
                .addValue("userId", participant.getUserId())
                .addValue("role", participant.getRole().name())
                .addValue("status", participant.getStatus().name()));
    }

    @Override
    public void updateParticipant(long conversationId, Participant participant) {
        jdbcTemplate.update(
            """
            UPDATE participant 
            SET participant_role = :role, participant_status = :status
            WHERE conversation_id = :conversationId AND user_id = :userId
            """,
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
            """
            DELETE FROM participant 
            WHERE conversation_id = :conversationId AND user_id = :userId
            """,
            new MapSqlParameterSource()
                .addValue("conversationId", conversationId)
                .addValue("userId", userId)
        );
    }

    @Override
    public void deleteConversation(long conversationId) {
        jdbcTemplate.update(
            """
            DELETE FROM conversation 
            WHERE conversation_id = :conversationId
            """,
            new MapSqlParameterSource("conversationId", conversationId)
        );
    }

    @Override
    public void createMessage(Message message) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
        CreateMessageTransaction transaction = new CreateMessageTransaction(message);
        transactionTemplate.execute(transaction);
    }

    @Override
    public List<Message> getMessages(long conversationId, long userId, int pageNumber, int pageSize) {
        return jdbcTemplate.query(
            """
            SELECT * 
            FROM message
                INNER JOIN message_status ms ON message.message_id = ms.message_id
            WHERE conversation_id = :conversationId 
                AND user_id = :userId 
                AND deleted = FALSE
            ORDER BY send_at ASC
            LIMIT :limit OFFSET :offset;
            """,
            new MapSqlParameterSource()
                .addValue("conversationId", conversationId)
                .addValue("userId", userId)
                .addValue("limit", pageSize)
                .addValue("offset", pageNumber * pageSize),
            new MessageRowMapper()
        );
    }

    @Override
    public long getNumberOfMessagesForConversation(long conversationId) {
        return Objects.requireNonNullElse(
            jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) 
                FROM message 
                WHERE conversation_id = :conversationId
                """,
                new MapSqlParameterSource("conversationId", conversationId), 
                Long.class),
            0L
        );
    }

    @Override
    public Message getMessageById(long messageId) {
        try {
            return jdbcTemplate.queryForObject(
                """
                SELECT * 
                FROM message 
                WHERE message_id = :messageId
                """,
                new MapSqlParameterSource("messageId", messageId),
                new MessageRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public void markMessageAsReadForUser(long messageId, long userId) {
        jdbcTemplate.update(
            """
            UPDATE message_status 
            SET read_at = :readAt 
            WHERE message_id = :messageId AND user_id = :userId;
            """,
            new MapSqlParameterSource()
                .addValue("messageId", messageId)
                .addValue("userId", userId)
                .addValue("readAt", Timestamp.valueOf(LocalDateTime.now()))
        );
    }

    @Override
    public void markMessageAsDeletedForUser(long messageId, long userId) {
        jdbcTemplate.update(
            """
            UPDATE message_status 
            SET deleted = TRUE 
            WHERE message_id = :messageId AND user_id = :userId
            """,
            new MapSqlParameterSource()
                .addValue("messageId", messageId)
                .addValue("userId", userId)
        );
    }

    private List<Participant> getParticipants(long conversationId) {
        return jdbcTemplate.query(
            """
            SELECT p.user_id, u.username, p.participant_role, p.participant_status, p.conversation_id
            FROM participant p
                INNER JOIN user u ON p.user_id = u.user_id
            WHERE p.conversation_id = :conversationId
            """,
            new MapSqlParameterSource("conversationId", conversationId),
            new ParticipantRowMapper()
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

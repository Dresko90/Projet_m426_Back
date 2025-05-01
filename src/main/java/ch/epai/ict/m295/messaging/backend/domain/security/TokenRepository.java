package ch.epai.ict.m295.messaging.backend.domain.security;

import ch.epai.ict.m295.messaging.backend.domain.User;

public interface TokenRepository {
    public void addToken(Token token, User user);
    public User getUserFromToken(Token token);
    public void deleteToken(Token token);
    public void deleteAllUserToken(User user);
}

package ch.epai.ict.m295.messaging.backend.domain;

import java.util.List;

public interface UserRepository {
    void createUser(User user, String password);
    List<User> getUsers(int pageNumber, int pageSize);
    User getUserById(long id);
    long getNumberOfUsers();
    User getUserByUsername(String username);
    void updateUser(User user);
    void updateUserPassword(User user, String password);
    void deleteUser(User user);
    boolean validate(String username, String password);
}

package ch.epai.ict.m295.messaging.backend.domain;

import java.util.List;

public interface UserRepository {
    public void createUser(User user, String password);
    public List<User> getUsers();
    public User getUser(long id);
    public User getUserByUsername(String username);
    public void updateUser(User user);
    public void updateUserPassword(long id, String password);
    public void deleteUser(long id);
    public boolean validate(String username, String password);
}

package ch.epai.ict.m295.message_api.domain;

import java.util.List;

public interface UserDirectory {
    public List<User> getUsers();
    public User getUser(long id);
    public void createUser(User user, String password);
    public void deleteUser(long id);
    public boolean validate(String email, String password);
    public User getUserByEmail(String email);
}

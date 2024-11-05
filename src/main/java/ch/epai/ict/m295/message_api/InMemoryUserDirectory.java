package ch.epai.ict.m295.message_api;

import java.util.ArrayList;
import java.util.List;

public class InMemoryUserDirectory implements UserDirectory {
    private List<User> users;

    public InMemoryUserDirectory(String url, String user, String password) {

        System.out.println(url);

        // TODO : ouvrir la connexion vers la base de données.

        this.users = new ArrayList<User>();
        this.users.add(new User(1, "user1@example.com", "user1"));
        this.users.add(new User(2, "user2@example.com", "user2"));
        this.users.add(new User(3, "user3@example.com", "user3"));
        this.users.add(new User(4, "user4@example.com", "user4"));
    }

    @Override
    public List<User> getUsers() {
        return this.users;
    }

    @Override
    public User getUser(long id) {
        for (User user : this.users) {
            if (user.getId() == id) {
                return user;
            }
        }
        throw new IllegalArgumentException("User not found");
    }

    @Override
    public void createUser(User user) {
        if (userExists(user.getId())) {
            throw new IllegalArgumentException("User already exists");
        }
        this.users.add(user);
    }

    @Override
    public void deleteUser(long id) {
        for (int i = 0; i < this.users.size(); i += 1) {
            User curUser = this.users.get(i);
            if (curUser.getId() == id) {
                this.users.remove(i);
            }
        }
    }

    private boolean userExists(long id) {
        for (User user : this.users) {
            if (user.getId() == id) {
                return true;
            }
        }
        return false;
    }
}

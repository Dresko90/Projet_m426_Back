package ch.epai.ict.m295.message_api;

import java.util.ArrayList;
import java.util.List;

public class UserDirectory {
    private List<User> users;

    public UserDirectory() {
        this.users = new ArrayList<User>();
        this.users.add(new User(1, "user1@example.com", "user1"));
        this.users.add(new User(2, "user2@example.com", "user2"));
        this.users.add(new User(3, "user3@example.com", "user3"));
        this.users.add(new User(4, "user4@example.com", "user4"));
    }

    public List<User> getUsers() {
        return this.users;
    }

    public User getUser(long id) {
        for (User user : this.users) {
            if (user.getId() == id) {
                return user;
            }
        }
        throw new IllegalArgumentException("User not found");
    }

    public void updateUser(User user) {
        if (userExists(user.getId())) {
            replaceUser(user);
        } else {
            this.users.add(user);
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

    private void replaceUser(User user) {
        for (int i = 0; i < this.users.size(); i += 1) {
            User curUser = this.users.get(i);
            if (curUser.getId() == user.getId()) {
                this.users.set(i, user);
                return;
            }
        }
    }
}

package ch.epai.ict.m295.message_api;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class UserController {
    private UserDirectory userDir;

    public UserController() {
        this.userDir = new UserDirectory();
    }

    @GetMapping(path = "/api/v1/users", produces = "application/json")
    public List<User> handleGetUsers() {
        return this.userDir.getUsers();
    }

    @GetMapping(path = "/api/v1/users/{id}", produces = "application/json")
    public User handleGetUser(@PathVariable long id) {
        return this.userDir.getUser(id);
    }

    @PostMapping(path = "/api/v1/users", 
            consumes = "application/json", produces = "application/json" )
    public User handlePostUser(@RequestBody User newUser) {
        this.userDir.updateUser(newUser);
        return newUser;
    }
}

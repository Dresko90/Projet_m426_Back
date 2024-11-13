package ch.epai.ict.m295.message_api.api;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import ch.epai.ict.m295.message_api.domain.User;
import ch.epai.ict.m295.message_api.domain.UserBuilder;
import ch.epai.ict.m295.message_api.domain.UserDirectory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
public class UserController {
    private UserDirectory userDir;

    public UserController(UserDirectory userDirectory) {
        this.userDir = userDirectory;
    }

    @GetMapping(path = "/api/v1/users", produces = "application/json")
    public List<User> handleGetUsers() {
        return this.userDir.getUsers();
    }

    @PostMapping(path = "/api/v1/users", 
            consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto handlePostUsers(@RequestBody CreateUserDto createUserDto) {
        User user = UserBuilder.create()
            .setEmail(createUserDto.email)
            .setDisplayName(createUserDto.displayName)
            .build();
        this.userDir.createUser(user, createUserDto.password);

        UserDto res = new UserDto();
        res.id = user.getId();
        res.email = user.getEmail();
        res.displayName = user.getDisplayName();

        return res;
    }

    @GetMapping(path = "/api/v1/users/{id}", produces = "application/json")
    public User handleGetUser(@PathVariable long id) {
        return this.userDir.getUser(id);
    }

    @DeleteMapping(path = "/api/v1/users/{id}")
    public void handleDeleteUser(@PathVariable long id) {
        this.userDir.deleteUser(id);
    }
}

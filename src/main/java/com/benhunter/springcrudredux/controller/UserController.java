package com.benhunter.springcrudredux.controller;

import com.benhunter.springcrudredux.model.Authentication;
import com.benhunter.springcrudredux.model.Count;
import com.benhunter.springcrudredux.model.User;
import com.benhunter.springcrudredux.model.UserDto;
import com.benhunter.springcrudredux.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    UserRepository repository;

    @Autowired
    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("")
    public Iterable<User> getAllUsers() {
        return this.repository.findAll();
    }

    @PostMapping("")
    public User createUser(@RequestBody User user) {
        return this.repository.save(user);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return this.repository.findById(id).get();
    }

    @PatchMapping("/{id}")
    public User patchUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        User currentUser = this.repository.findById(id).get();
        currentUser.updateFromUserDto(userDto);
        return this.repository.save(currentUser);
    }

    @DeleteMapping("/{id}")
    public Count deleteUserByIdAndRenderCount(@PathVariable Long id) {
        this.repository.deleteById(id);
        return new Count().setCount(this.repository.count());
    }

    @PostMapping("/authenticate")
    public Authentication postAuthenticateUserRendersAuthentication(@RequestBody UserDto userDto) {
        User currentUser = this.repository.findUserByEmail(userDto.getEmail());
        Authentication authentication = new Authentication();
        if (currentUser.getPassword().equals(userDto.getPassword())) {
            authentication.setAuthenticated(true)
                    .setUser(currentUser);
        } else {
            authentication.setAuthenticated(false);
        }
        return authentication;
    }
}

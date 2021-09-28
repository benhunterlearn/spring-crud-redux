package com.benhunter.springcrudredux;

import com.benhunter.springcrudredux.model.User;
import com.benhunter.springcrudredux.model.UserDto;
import com.benhunter.springcrudredux.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.core.Is.is;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class UserControllerTest {

    @Autowired
    MockMvc mvc;

    UserRepository repository;
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public UserControllerTest(UserRepository repository) {
        this.repository = repository;
    }

    @Test
    public void getAllUsersFromRepository() throws Exception {
        User firstUser = this.repository.save(new User().setEmail("first@user.com").setPassword("first password"));
        User secondUser = this.repository.save(new User().setEmail("second@user.com").setPassword("second password"));
        RequestBuilder request = MockMvcRequestBuilders.get("/users")
                .accept(MediaType.APPLICATION_JSON);

        // Note: use of literal index in jsonPath() is fragile. For example: "$[0]"
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(firstUser.getId().intValue())))
                .andExpect(jsonPath("$[0].email", is(firstUser.getEmail())))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].id", is(secondUser.getId().intValue())))
                .andExpect(jsonPath("$[1].email", is(secondUser.getEmail())))
                .andExpect(jsonPath("$[1].password").doesNotExist());
    }

    @Test
    public void createUserWithValidInput() throws Exception {
        User newUser = new User().setEmail("new@email.com").setPassword("asdfllkj");
        RequestBuilder request = MockMvcRequestBuilders.post("/users")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(newUser));
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
//                .andExpect(jsonPath("$.id", is(firstUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is(newUser.getEmail())))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void getUserById() throws Exception {
        User firstUser = this.repository.save(new User().setEmail("first@user.com").setPassword("first password"));
        RequestBuilder request = MockMvcRequestBuilders.get("/users/" + firstUser.getId())
                .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is(firstUser.getEmail())))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void patchUserWithEmail() throws Exception {
        User firstUser = this.repository.save(new User().setEmail("first@user.com").setPassword("first password"));
        User updatedUser = new User().setEmail("new@email.com");
        RequestBuilder request = MockMvcRequestBuilders.patch("/users/" + firstUser.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedUser));
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void patchUserWithPassword() throws Exception {
        User firstUser = this.repository.save(new User().setEmail("first@user.com").setPassword("first password"));
        UserDto updatedUser = new UserDto().setPassword("newbadpassword");
        RequestBuilder request = MockMvcRequestBuilders.patch("/users/" + firstUser.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedUser));
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is(firstUser.getEmail())))
                .andExpect(jsonPath("$.password").doesNotExist());
        String expectedPassword = updatedUser.getPassword();
        String actualPassword = this.repository.findById(firstUser.getId()).get().getPassword();
        assertEquals(expectedPassword, actualPassword);
    }

    @Test
    public void patchUserWithEmailAndPassword() throws Exception {
        User firstUser = this.repository.save(new User().setEmail("first@user.com").setPassword("first password"));
        UserDto updatedUser = new UserDto()
                .setEmail("new@email.com")
                .setPassword("newbadpassword");
        RequestBuilder request = MockMvcRequestBuilders.patch("/users/" + firstUser.getId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(updatedUser));
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(firstUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())))
                .andExpect(jsonPath("$.password").doesNotExist());
        String expectedPassword = updatedUser.getPassword();
        String actualPassword = this.repository.findById(firstUser.getId()).get().getPassword();
        assertEquals(expectedPassword, actualPassword);
    }

    @Test
    public void deleteExistingUserById() throws Exception {
        User firstUser = this.repository.save(new User().setEmail("first@user.com").setPassword("first password"));
        RequestBuilder request = MockMvcRequestBuilders.delete("/users/" + firstUser.getId())
                .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").isNumber());
    }

    @Test
    public void authenticateWithValidUser() throws Exception {
        User firstUser = this.repository.save(new User().setEmail("first@user.com").setPassword("first password"));
        UserDto userDto = new UserDto()
                .setEmail(firstUser.getEmail())
                .setPassword(firstUser.getPassword());
        RequestBuilder request = MockMvcRequestBuilders.post("/users/authenticate")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(userDto));
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated", is(true)))
                .andExpect(jsonPath("$.user.id", is(firstUser.getId().intValue())))
                .andExpect(jsonPath("$.user.email", is(firstUser.getEmail())))
                .andExpect(jsonPath("$.user.password").doesNotExist());
    }

    @Test
    public void authenticateWithInvalidUser() throws Exception {
        User firstUser = this.repository.save(new User().setEmail("first@user.com").setPassword("first password"));
        UserDto userDto = new UserDto()
                .setEmail(firstUser.getEmail())
                .setPassword("wrong password");
        RequestBuilder request = MockMvcRequestBuilders.post("/users/authenticate")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.mapper.writeValueAsString(userDto));
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated", is(false)))
                .andExpect(jsonPath("$.user").doesNotExist());
    }
}

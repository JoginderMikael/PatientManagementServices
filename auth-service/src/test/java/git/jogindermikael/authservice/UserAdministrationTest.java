package git.jogindermikael.authservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import git.jogindermikael.authservice.model.User;
import git.jogindermikael.authservice.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserAdministrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;

    @Test
    void administratorCanCreateAndDisableAUser() throws Exception {
        String email = "created-" + UUID.randomUUID() + "@example.test";
        String response = mvc.perform(post("/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Temporary!234","role":"PATIENT"}
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();
        String id = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response).path("id").asText();

        mvc.perform(patch("/admin/users/{id}/status", id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISABLED"));
    }

    @Test
    void patientCannotCreateUsers() throws Exception {
        mvc.perform(post("/admin/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PATIENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"forbidden@example.test","password":"Temporary!234","role":"PATIENT"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void disabledUserCannotLogin() throws Exception {
        User user = new User();
        user.setEmail("disabled-" + UUID.randomUUID() + "@example.test");
        user.setPassword(passwords.encode("Temporary!234"));
        user.setRole("PATIENT");
        user.setStatus("DISABLED");
        users.saveAndFlush(user);

        mvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Temporary!234"}
                                """.formatted(user.getEmail())))
                .andExpect(status().isUnauthorized());
    }
}

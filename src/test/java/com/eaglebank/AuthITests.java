package com.eaglebank;

import static com.eaglebank.testutils.UserTestCommons.EMAIL;
import static com.eaglebank.testutils.UserTestCommons.PASSWORD;
import static com.eaglebank.testutils.UserTestCommons.buildUserRequestDto;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eaglebank.dto.AuthRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

public class AuthITests extends BaseIntegrationTest {

    @Test
    void login_withNonExistingIdAndValidToken_shouldReturnNotFound()
            throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(EMAIL, PASSWORD))))
                .andExpect(status().isNotFound());
    }

    @Test
    void login_withNoEmail_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(null, PASSWORD))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_wrongPassword_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildUserRequestDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andReturn();

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequestDto(EMAIL, "wrongPass"))))
                .andExpect(status().isUnauthorized());
    }
}

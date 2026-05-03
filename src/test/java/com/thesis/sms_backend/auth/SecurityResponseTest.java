package com.thesis.sms_backend.auth;

import com.thesis.sms_backend.auth.internal.ApiAccessDeniedHandler;
import com.thesis.sms_backend.auth.internal.SecurityConfig;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest
@Import({SecurityConfig.class, ApiAccessDeniedHandler.class})
class SecurityResponseTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void allRequests_returnStandardized403() throws Exception {
        mockMvc.perform(get("/anything"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }
}

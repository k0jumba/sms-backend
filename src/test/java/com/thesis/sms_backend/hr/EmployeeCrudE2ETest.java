package com.thesis.sms_backend.hr;

import com.thesis.sms_backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class EmployeeCrudE2ETest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void employeeLifecycle_createReadUpdateDeleteVerifyGone() throws Exception {

        MvcResult createResult = mockMvc.perform(post("/api/hr/employees")
                        .contentType("application/json")
                        .content("""
                                {
                                    "firstName": "Jane",
                                    "lastName":  "Doe",
                                    "middleName": "A",
                                    "role":      "TEACHER",
                                    "active":    true,
                                    "email":     "jane.doe@example.com",
                                    "phone":     "+1000000001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.uuid").isNotEmpty())
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.middleName").value("A"))
                .andExpect(jsonPath("$.data.role").value("TEACHER"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.data.phone").value("+1000000001"))
                .andReturn();

        String uuid = com.jayway.jsonpath.JsonPath.read(
                createResult.getResponse().getContentAsString(), "$.data.uuid");

        mockMvc.perform(get("/api/hr/employees/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.uuid").value(uuid))
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.email").value("jane.doe@example.com"));


        mockMvc.perform(get("/api/hr/employees")
                        .param("page", "0")
                        .param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[?(@.uuid == '" + uuid + "')]").exists())
                .andExpect(jsonPath("$.meta.totalElements").isNumber());


        mockMvc.perform(patch("/api/hr/employees/{uuid}", uuid)
                        .contentType("application/json")
                        .content("""
                                {
                                    "firstName": "Janet",
                                    "role":      "MANAGER",
                                    "active":    false,
                                    "email":     "janet.doe@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.uuid").value(uuid))

                .andExpect(jsonPath("$.data.firstName").value("Janet"))
                .andExpect(jsonPath("$.data.role").value("MANAGER"))
                .andExpect(jsonPath("$.data.active").value(false))
                .andExpect(jsonPath("$.data.email").value("janet.doe@example.com"))

                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.middleName").value("A"))
                .andExpect(jsonPath("$.data.phone").value("+1000000001"));


        mockMvc.perform(delete("/api/hr/employees/{uuid}", uuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));


        mockMvc.perform(get("/api/hr/employees/{uuid}", uuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }
}
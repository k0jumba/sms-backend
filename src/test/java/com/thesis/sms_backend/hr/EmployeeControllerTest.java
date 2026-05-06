package com.thesis.sms_backend.hr;

import com.thesis.sms_backend.auth.internal.ApiAccessDeniedHandler;
import com.thesis.sms_backend.auth.internal.SecurityConfig;
import com.thesis.sms_backend.core.StrictJacksonConfig;
import com.thesis.sms_backend.core.GlobalExceptionHandler;
import com.thesis.sms_backend.core.PagedResult;
import com.thesis.sms_backend.core.UniqueConstraintViolationException;
import com.thesis.sms_backend.hr.internal.Employee;
import com.thesis.sms_backend.hr.internal.EmployeeController;
import com.thesis.sms_backend.hr.internal.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({EmployeeController.class, SecurityConfig.class, ApiAccessDeniedHandler.class})
@Import({GlobalExceptionHandler.class, StrictJacksonConfig.class})
@Slf4j
class EmployeeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EmployeeService employeeService;

    // ---------------------------------------------------------------------------
    // Fixtures
    // ---------------------------------------------------------------------------

    static final int DEFAULT_PAGE      = 0;
    static final int DEFAULT_PAGE_SIZE = 20;

    static Employee employee(String firstName, String email) {
        return Employee.builder()
                .firstName(firstName)
                .lastName("Doe")
                .role(Employee.Role.TEACHER)
                .active(true)
                .email(email)
                .phone("+1000000000")
                .build();
    }

    static PagedResult<Employee> pagedResult(List<Employee> content, int page, int pageSize, long total) {
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) total / pageSize);
        return new PagedResult<>(content, page, pageSize, total, totalPages);
    }

    // ===========================================================================
    // Routing constraints
    // ===========================================================================

    @Nested
    class RoutingConstraints {

        @ParameterizedTest(name = "{0} /api/hr/employees/non-existent/path -> 404")
        @ValueSource(strings = {"GET", "POST", "PUT", "PATCH", "DELETE", "HEAD"})
        void nonExistentPath_returns404RouteNotFound(String method) throws Exception {
            mockMvc.perform(request(HttpMethod.valueOf(method), "/api/hr/employees/non-existent/path"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.error.message").value("Route not found"));
        }

        @ParameterizedTest(name = "{0} /api/hr/employees -> 405")
        @ValueSource(strings = {"PUT", "PATCH", "DELETE"})
        void disallowedMethodOnCollectionEndpoint_returns405(String method) throws Exception {
            mockMvc.perform(request(HttpMethod.valueOf(method), "/api/hr/employees"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("METHOD_NOT_ALLOWED"));
        }

        @ParameterizedTest(name = "{0} /api/hr/employees/<uuid> -> 405")
        @ValueSource(strings = {"POST", "PUT"})
        void disallowedMethodOnItemEndpoint_returns405(String method) throws Exception {
            mockMvc.perform(request(HttpMethod.valueOf(method), "/api/hr/employees/00000000-0000-0000-0000-000000000001"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("METHOD_NOT_ALLOWED"));
        }
    }

    // ===========================================================================
    // GET /api/hr/employees
    // ===========================================================================

    @Nested
    class GetAll {

        // -----------------------------------------------------------------------
        // Validation
        // -----------------------------------------------------------------------

        @ParameterizedTest(name = "page={0} -> 422")
        @ValueSource(ints = {-1, -100})
        void invalidPage_returns422(int page) throws Exception {
            mockMvc.perform(get("/api/hr/employees")
                            .param("page", String.valueOf(page))
                            .param("pageSize", "20"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("UNPROCESSABLE_ENTITY"));
        }

        @ParameterizedTest(name = "pageSize={0} -> 422")
        @ValueSource(ints = {0, -1, 101, 1000})
        void invalidPageSize_returns422(int pageSize) throws Exception {
            mockMvc.perform(get("/api/hr/employees")
                            .param("page", "0")
                            .param("pageSize", String.valueOf(pageSize)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("UNPROCESSABLE_ENTITY"));
        }

        // -----------------------------------------------------------------------
        // Explicit pagination
        // -----------------------------------------------------------------------

        @Test
        void explicitPagination_fullPage_returns200WithCorrectDataAndMeta() throws Exception {
            List<Employee> employees = List.of(
                    employee("Alice", "alice@example.com"),
                    employee("Bob",   "bob@example.com")
            );
            when(employeeService.findAll(eq(1), eq(2)))
                    .thenReturn(pagedResult(employees, 1, 2, 10L));

            mockMvc.perform(get("/api/hr/employees")
                            .param("page",     "1")
                            .param("pageSize", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    // data
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].firstName").value("Alice"))
                    .andExpect(jsonPath("$.data[1].firstName").value("Bob"))
                    // meta
                    .andExpect(jsonPath("$.meta.page").value(1))
                    .andExpect(jsonPath("$.meta.pageSize").value(2))
                    .andExpect(jsonPath("$.meta.totalElements").value(10))
                    .andExpect(jsonPath("$.meta.totalPages").value(5));
        }

        @Test
        void explicitPagination_partialPage_returns200WithFewerItemsThanPageSize() throws Exception {
            List<Employee> employees = List.of(
                    employee("Carol", "carol@example.com")
            );
            // pageSize=2 requested but only 1 item exists on this page
            when(employeeService.findAll(eq(1), eq(2)))
                    .thenReturn(pagedResult(employees, 1, 2, 3L));

            mockMvc.perform(get("/api/hr/employees")
                            .param("page",     "1")
                            .param("pageSize", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.meta.page").value(1))
                    .andExpect(jsonPath("$.meta.pageSize").value(2))
                    .andExpect(jsonPath("$.meta.totalElements").value(3))
                    .andExpect(jsonPath("$.meta.totalPages").value(2));
        }

        // -----------------------------------------------------------------------
        // Default pagination (no query params)
        // -----------------------------------------------------------------------

        @Test
        void defaultPagination_fullPage_returns200WithDefaultPageSizeItemsAndMeta() throws Exception {
            List<Employee> employees = Collections.nCopies(
                    DEFAULT_PAGE_SIZE,
                    employee("Dave", "dave@example.com")
            );
            when(employeeService.findAll(eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(pagedResult(employees, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, 50L));

            mockMvc.perform(get("/api/hr/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.meta.page").value(DEFAULT_PAGE))
                    .andExpect(jsonPath("$.meta.pageSize").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.meta.totalElements").value(50))
                    .andExpect(jsonPath("$.meta.totalPages").value(3));
        }

        @Test
        void defaultPagination_partialPage_returns200WithFewerItemsThanDefaultPageSize() throws Exception {
            List<Employee> employees = List.of(
                    employee("Eve",   "eve@example.com"),
                    employee("Frank", "frank@example.com")
            );
            when(employeeService.findAll(eq(DEFAULT_PAGE), eq(DEFAULT_PAGE_SIZE)))
                    .thenReturn(pagedResult(employees, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, 2L));

            mockMvc.perform(get("/api/hr/employees"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.meta.page").value(DEFAULT_PAGE))
                    .andExpect(jsonPath("$.meta.pageSize").value(DEFAULT_PAGE_SIZE))
                    .andExpect(jsonPath("$.meta.totalElements").value(2))
                    .andExpect(jsonPath("$.meta.totalPages").value(1));
        }
    }

    // ===========================================================================
    // GET /api/hr/employees/{uuid}
    // ===========================================================================

    @Nested
    class GetById {

        final UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");

        @Test
        void malformedUuid_returns400WithDescriptiveMessage() throws Exception {
            mockMvc.perform(get("/api/hr/employees/not-a-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.error.message").value("Invalid value 'not-a-uuid' for parameter 'uuid'"));
        }

        @Test
        void unknownUuid_returns404ResourceNotFound() throws Exception {
            when(employeeService.getById(id)).thenThrow(new EntityNotFoundException());

            mockMvc.perform(get("/api/hr/employees/{uuid}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.error.message").value("Resource not found"));
        }

        @Test
        void existingUuid_returns200WithEmployee() throws Exception {
            Employee emp = employee("Alice", "alice@example.com");
            when(employeeService.getById(id)).thenReturn(emp);

            mockMvc.perform(get("/api/hr/employees/{uuid}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.firstName").value("Alice"))
                    .andExpect(jsonPath("$.data.email").value("alice@example.com"))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }
    }

    // ===========================================================================
    // POST /api/hr/employees
    // ===========================================================================

    @Nested
    class CreateEmployee {

        // -----------------------------------------------------------------------
        // Shared valid body
        // -----------------------------------------------------------------------

        final String VALID_BODY = """
                {
                    "firstName": "Alice",
                    "lastName":  "Doe",
                    "role":      "TEACHER",
                    "active":    true,
                    "email":     "alice@example.com",
                    "phone":     "+1000000000",
                    "middleName": "A"
                }
                """;

        // -----------------------------------------------------------------------
        // 422 — missing / null required fields
        // -----------------------------------------------------------------------

        @ParameterizedTest(name = "missing {0} -> 422")
        @ValueSource(strings = {"firstName", "lastName", "role", "active", "email", "phone"})
        void missingRequiredField_returns422(String field) throws Exception {
            String body = removeField(VALID_BODY, field);

            mockMvc.perform(post("/api/hr/employees")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("UNPROCESSABLE_ENTITY"))
                    .andExpect(jsonPath("$.error.message").value("Method argument not valid"))
                    .andExpect(jsonPath("$.error.fields[?(@.field == '" + field + "')]").exists());
        }

        @ParameterizedTest(name = "null {0} -> 422")
        @ValueSource(strings = {"firstName", "lastName", "role", "active", "email", "phone"})
        void nullRequiredField_returns422(String field) throws Exception {
            String body = setFieldNull(VALID_BODY, field);

            mockMvc.perform(post("/api/hr/employees")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("UNPROCESSABLE_ENTITY"))
                    .andExpect(jsonPath("$.error.message").value("Method argument not valid"))
                    .andExpect(jsonPath("$.error.fields[?(@.field == '" + field + "')]").exists());
        }

        @Test
        void invalidEmailFormat_returns422() throws Exception {
            String body = replaceFieldValue(VALID_BODY, "email", "\"not-an-email\"");

            mockMvc.perform(post("/api/hr/employees")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("UNPROCESSABLE_ENTITY"))
                    .andExpect(jsonPath("$.error.fields[?(@.field == 'email')]").exists());
        }

        // -----------------------------------------------------------------------
        // 400 — wrong types (strict Jackson coercion)
        // -----------------------------------------------------------------------

        @ParameterizedTest(name = "integer value for string field {0} -> 400")
        @ValueSource(strings = {"firstName", "lastName", "middleName", "email", "phone"})
        void integerValueForStringField_returns400(String field) throws Exception {
            String body = replaceFieldValue(VALID_BODY, field, "123");

            mockMvc.perform(post("/api/hr/employees")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.error.message").value("Malformed or unreadable request body"));
        }

        @Test
        void integerValueForBooleanField_returns400() throws Exception {
            String body = replaceFieldValue(VALID_BODY, "active", "1");

            mockMvc.perform(post("/api/hr/employees")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
        }

        @Test
        void invalidRoleValue_returns400() throws Exception {
            String body = replaceFieldValue(VALID_BODY, "role", "\"INVALID_ROLE\"");

            mockMvc.perform(post("/api/hr/employees")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.error.message").value("Malformed or unreadable request body"));
        }

        // -----------------------------------------------------------------------
        // 409 — unique constraint violations
        // -----------------------------------------------------------------------

        @Test
        void duplicateEmail_returns409() throws Exception {
            when(employeeService.create(any()))
                    .thenThrow(new UniqueConstraintViolationException("email", "alice@example.com"));

            mockMvc.perform(post("/api/hr/employees")
                            .contentType("application/json")
                            .content(VALID_BODY))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CONFLICT"))
                    .andExpect(jsonPath("$.error.fields[?(@.field == 'email')]").exists());
        }

        @Test
        void duplicatePhone_returns409() throws Exception {
            when(employeeService.create(any()))
                    .thenThrow(new UniqueConstraintViolationException("phone", "+1000000000"));

            mockMvc.perform(post("/api/hr/employees")
                            .contentType("application/json")
                            .content(VALID_BODY))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CONFLICT"))
                    .andExpect(jsonPath("$.error.fields[?(@.field == 'phone')]").exists());
        }

        // -----------------------------------------------------------------------
        // 201 — happy path
        // -----------------------------------------------------------------------

        @Test
        void validRequest_returns201WithCreatedEmployee() throws Exception {
            Employee created = employee("Alice", "alice@example.com");
            when(employeeService.create(any())).thenReturn(created);

            mockMvc.perform(post("/api/hr/employees")
                            .contentType("application/json")
                            .content(VALID_BODY))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.firstName").value("Alice"))
                    .andExpect(jsonPath("$.data.email").value("alice@example.com"))
                    .andExpect(jsonPath("$.error").doesNotExist());
        }

        // -----------------------------------------------------------------------
        // Body manipulation helpers
        // -----------------------------------------------------------------------

        /** Removes the line containing "field": ... from a JSON string. */
        private String removeField(String json, String field) {
            return json.replaceAll("(?m)^\\s*\"" + field + "\"\\s*:.*,?\\n", "");
        }

        /** Replaces the value of a field with JSON null. */
        private String setFieldNull(String json, String field) {
            return json.replaceAll(
                    "(\"" + field + "\"\\s*:\\s*)([^,\\n}]+)",
                    "$1null"
            );
        }

        /** Replaces the value of a field with an arbitrary raw JSON value. */
        private String replaceFieldValue(String json, String field, String rawValue) {
            return json.replaceAll(
                    "(\"" + field + "\"\\s*:\\s*)([^,\\n}]+)",
                    "$1" + rawValue
            );
        }
    }
}
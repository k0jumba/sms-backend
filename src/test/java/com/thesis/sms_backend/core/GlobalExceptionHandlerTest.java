package com.thesis.sms_backend.core;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private ApiResponse<?> bodyOf(ResponseEntity<? extends ApiResponse<?>> response) {
        ApiResponse<?> body = response.getBody();
        assertNotNull(body);
        return body;
    }

    private void assertError(ApiResponse<?> body, String expectedCode) {
        assertFalse(body.isSuccess());
        assertThat(body.getData()).isNull();
        assertThat(body.getError().getCode()).isEqualTo(expectedCode);
    }

    @Nested
    class HandleNoResourceFound {

        @Test
        void returns404WithNotFoundCode() throws Exception {
            NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/unknown");

            ResponseEntity<ApiResponse<?>> response = handler.handleNoResourceFound(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertError(bodyOf(response), "NOT_FOUND");
            assertThat(bodyOf(response).getError().getMessage()).isEqualTo("Route not found");
        }
    }

    @Nested
    class HandleTypeMismatch {

        @Test
        void returns400WithBadRequestCodeAndDescriptiveMessage() {
            MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
            when(ex.getValue()).thenReturn("not-a-uuid");
            when(ex.getName()).thenReturn("id");

            ResponseEntity<ApiResponse<?>> response = handler.handleTypeMismatch(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ApiResponse<?> body = bodyOf(response);
            assertError(body, "BAD_REQUEST");
            assertThat(body.getError().getMessage())
                    .contains("not-a-uuid")
                    .contains("id");
        }
    }

    @Nested
    class HandleMethodNotAllowed {

        @Test
        void returns405WithMethodNotAllowedCode() {
            HttpRequestMethodNotSupportedException ex =
                    new HttpRequestMethodNotSupportedException("DELETE");

            ResponseEntity<ApiResponse<?>> response = handler.handleMethodNotAllowed(ex);
            ApiResponse<?> body = bodyOf(response);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            assertError(bodyOf(response), "METHOD_NOT_ALLOWED");
            assertThat(body.getError().getMessage()).isEqualTo(ex.getMessage());
        }
    }

    @Nested
    class HandleEntityNotFound {

        @Test
        void returns404WithNotFoundCode() {
            ResponseEntity<ApiResponse<?>> response =
                    handler.handleEntityNotFound(new EntityNotFoundException());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertError(bodyOf(response), "NOT_FOUND");
            assertThat(bodyOf(response).getError().getMessage()).isEqualTo("Resource not found");
        }
    }

    @Nested
    class HandleNotReadable {

        @Test
        void returns400WithBadRequestCodeAndGenericMessage() {
            HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                    "bad body", new MockHttpInputMessage(new byte[0]));

            ResponseEntity<ApiResponse<?>> response = handler.handleNotReadable(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            ApiResponse<?> body = bodyOf(response);
            assertError(body, "BAD_REQUEST");
            assertThat(body.getError().getMessage()).isEqualTo("Malformed or unreadable request body");
        }
    }

    // ===========================================================================
    // handleConstraintViolation
    // ===========================================================================

    @Nested
    class HandleConstraintViolation {

        private ConstraintViolation<?> violation(String propertyPath, String message) {
            ConstraintViolation<?> cv = mock(ConstraintViolation.class);
            Path path = mock(Path.class);
            when(path.toString()).thenReturn(propertyPath);
            when(cv.getPropertyPath()).thenReturn(path);
            when(cv.getMessage()).thenReturn(message);
            return cv;
        }

        @Test
        void returns422WithFieldViolationsForNegativePageAndNonIntegerPageSize() {
            ConstraintViolationException ex = new ConstraintViolationException(Set.of(
                    violation("findAll.page", "must be greater than or equal to 0"),
                    violation("findAll.pageSize", "must be a positive integer")
            ));

            ResponseEntity<ApiResponse<?>> response = handler.handleConstraintViolation(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            ApiResponse<?> body = bodyOf(response);
            assertError(body, "UNPROCESSABLE_ENTITY");

            List<ApiError.FieldViolation> fields = body.getError().getFields();
            assertThat(fields).hasSize(2);

            assertThat(fields)
                    .extracting(ApiError.FieldViolation::getField, ApiError.FieldViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            tuple("page",     "must be greater than or equal to 0"),
                            tuple("pageSize", "must be a positive integer")
                    );
        }
    }

    // ===========================================================================
    // handleValidation
    // ===========================================================================

    @Nested
    class HandleValidation {

        @Test
        void returns422WithFieldViolationsForNotNullAndEmailFormatErrors() {
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                    new FieldError("request", "firstName", "must not be null"),
                    new FieldError("request", "email", "must be a well-formed email address")
            ));
            when(bindingResult.getGlobalErrors()).thenReturn(List.of());

            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);

            ResponseEntity<ApiResponse<?>> response = handler.handleValidation(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            ApiResponse<?> body = bodyOf(response);
            assertError(body, "UNPROCESSABLE_ENTITY");

            List<ApiError.FieldViolation> fields = body.getError().getFields();
            assertThat(fields).hasSize(2);
            assertThat(fields)
                    .extracting(ApiError.FieldViolation::getField, ApiError.FieldViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            tuple("firstName", "must not be null"),
                            tuple("email", "must be a well-formed email address")
                    );
        }

        @Test
        void includesGlobalErrorsWithNullFieldName() {
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getFieldErrors()).thenReturn(List.of());
            when(bindingResult.getGlobalErrors()).thenReturn(List.of(
                    new ObjectError("request", "start date must be before end date")
            ));

            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);

            List<ApiError.FieldViolation> fields =
                    bodyOf(handler.handleValidation(ex)).getError().getFields();

            assertThat(fields).hasSize(1);
            assertThat(fields.getFirst().getField()).isNull();
            assertThat(fields.getFirst().getMessage()).isEqualTo("start date must be before end date");
        }
    }

    @Nested
    class HandleUniqueConstraint {

        @Test
        void returns409WithConflictCodeAndFieldViolation() {
            UniqueConstraintViolationException ex =
                    new UniqueConstraintViolationException("email", "jane@example.com");

            ResponseEntity<ApiResponse<?>> response = handler.handleUniqueConstraint(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            ApiResponse<?> body = bodyOf(response);
            assertError(body, "CONFLICT");

            List<ApiError.FieldViolation> fields = body.getError().getFields();
            assertThat(fields).hasSize(1);
            assertThat(fields.getFirst().getField()).isEqualTo("email");
            assertThat(fields.getFirst().getMessage()).isEqualTo("Value already exists");
        }
    }

    @Nested
    class HandleGeneric {

        @Test
        void returns500WithInternalErrorCode() {
            ResponseEntity<ApiResponse<Void>> response =
                    handler.handleGeneric(new RuntimeException("fail"));

            ApiResponse<Void> body = response.getBody();

            Assertions.assertNotNull(body);
            assertFalse(body.isSuccess());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(body.getError().getCode()).isEqualTo("INTERNAL_ERROR");
        }
    }
}
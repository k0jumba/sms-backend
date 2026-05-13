package com.thesis.sms_backend.hr;

import com.thesis.sms_backend.core.PagedResult;
import com.thesis.sms_backend.core.Patch;
import com.thesis.sms_backend.core.UniqueConstraintViolationException;
import com.thesis.sms_backend.hr.internal.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private UUID employeeId;
    private Employee existingEmployee;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        existingEmployee = Employee.builder()
                .firstName("Jane")
                .lastName("Doe")
                .middleName(null)
                .email("jane@example.com")
                .phone("+1234567890")
                .role(Employee.Role.ADMIN)
                .active(true)
                .build();
    }

    @Nested
    class FindAll {

        @Test
        void returnsPagedResultBuiltFromRepositoryPage() {
            Page<Employee> page = new PageImpl<>(
                    List.of(existingEmployee),
                    PageRequest.of(0, 10),
                    1
            );
            when(employeeRepository.findAll(any(Pageable.class))).thenReturn(page);

            PagedResult<Employee> result = employeeService.findAll(0, 10);

            assertThat(result.content()).containsExactly(existingEmployee);
            assertThat(result.totalElements()).isEqualTo(1);
        }

        @Test
        void passesCorrectPageAndPageSizeToRepository() {
            Page<Employee> emptyPage = new PageImpl<>(List.of(), PageRequest.of(2, 5), 0);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            when(employeeRepository.findAll(pageableCaptor.capture())).thenReturn(emptyPage);

            employeeService.findAll(2, 5);

            Pageable captured = pageableCaptor.getValue();
            assertThat(captured.getPageNumber()).isEqualTo(2);
            assertThat(captured.getPageSize()).isEqualTo(5);
        }
    }

    @Nested
    class GetById {

        @Test
        void returnsEmployeeWhenFound() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));

            Employee result = employeeService.getById(employeeId);

            assertThat(result).isSameAs(existingEmployee);
        }

        @Test
        void throwsEntityNotFoundExceptionWhenNotFound() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.getById(employeeId))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    class Create {

        private CreateEmployeeRequest request;

        @BeforeEach
        void setUp() {
            request = new CreateEmployeeRequest(
                    "Jane",
                    "Doe",
                    null,
                    Employee.Role.ADMIN,
                    true,
                    "jane@example.com",
                    "+1234567890"
            );
        }

        @Test
        void throwsUniqueConstraintViolationExceptionWhenEmailIsTaken() {
            when(employeeRepository.existsByEmail(request.email())).thenReturn(true);

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(UniqueConstraintViolationException.class)
                    .satisfies(ex -> {
                        UniqueConstraintViolationException violation =
                                (UniqueConstraintViolationException) ex;
                        assertThat(violation.getField()).isEqualTo("email");
                    });

            verify(employeeRepository, never()).save(any());
        }

        @Test
        void throwsUniqueConstraintViolationExceptionWhenPhoneIsTaken() {
            when(employeeRepository.existsByEmail(request.email())).thenReturn(false);
            when(employeeRepository.existsByPhone(request.phone())).thenReturn(true);

            assertThatThrownBy(() -> employeeService.create(request))
                    .isInstanceOf(UniqueConstraintViolationException.class)
                    .satisfies(ex -> {
                        UniqueConstraintViolationException violation =
                                (UniqueConstraintViolationException) ex;
                        assertThat(violation.getField()).isEqualTo("phone");
                    });

            verify(employeeRepository, never()).save(any());
        }

        @Test
        void savesEmployeeWithCorrectFieldValuesAndReturnsIt() {
            when(employeeRepository.existsByEmail(request.email())).thenReturn(false);
            when(employeeRepository.existsByPhone(request.phone())).thenReturn(false);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

            Employee result = employeeService.create(request);

            ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(captor.capture());

            Employee saved = captor.getValue();
            assertThat(saved.getFirstName()).isEqualTo(request.firstName());
            assertThat(saved.getLastName()).isEqualTo(request.lastName());
            assertThat(saved.getMiddleName()).isEqualTo(request.middleName());
            assertThat(saved.getEmail()).isEqualTo(request.email());
            assertThat(saved.getPhone()).isEqualTo(request.phone());
            assertThat(saved.getRole()).isEqualTo(request.role());
            assertThat(saved.isActive()).isEqualTo(request.active());

            assertThat(result).isSameAs(saved);
        }
    }

    @Nested
    class Update {

        private UpdateEmployeeRequest fullUpdateRequest;
        private UpdateEmployeeRequest emptyUpdateRequest;

        @BeforeEach
        void setUp() {
            fullUpdateRequest = UpdateEmployeeRequest.builder()
                    .firstName(Patch.of("John"))
                    .lastName(Patch.of("Smith"))
                    .middleName(Patch.of("B"))
                    .email(Patch.of("john@example.com"))
                    .phone(Patch.of("+9876543210"))
                    .role(Patch.of(Employee.Role.MANAGER))
                    .active(Patch.of(false))
                    .build();

            emptyUpdateRequest = UpdateEmployeeRequest.builder()
                    .firstName(Patch.absent())
                    .lastName(Patch.absent())
                    .middleName(Patch.absent())
                    .email(Patch.absent())
                    .phone(Patch.absent())
                    .role(Patch.absent())
                    .active(Patch.absent())
                    .build();
        }

        @Test
        void throwsEntityNotFoundExceptionWhenEmployeeNotFound() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.update(employeeId, fullUpdateRequest))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(employeeRepository, never()).save(any());
        }

        @Test
        void throwsUniqueConstraintViolationExceptionWhenNewEmailIsTaken() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
            when(employeeRepository.existsByEmail(fullUpdateRequest.getEmail().getValue()))
                    .thenReturn(true);

            assertThatThrownBy(() -> employeeService.update(employeeId, fullUpdateRequest))
                    .isInstanceOf(UniqueConstraintViolationException.class)
                    .satisfies(ex -> assertThat(
                            ((UniqueConstraintViolationException) ex).getField()
                    ).isEqualTo("email"));

            verify(employeeRepository, never()).save(any());
        }

        @Test
        void throwsUniqueConstraintViolationExceptionWhenNewPhoneIsTaken() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
            when(employeeRepository.existsByEmail(fullUpdateRequest.getEmail().getValue()))
                    .thenReturn(false);
            when(employeeRepository.existsByPhone(fullUpdateRequest.getPhone().getValue()))
                    .thenReturn(true);

            assertThatThrownBy(() -> employeeService.update(employeeId, fullUpdateRequest))
                    .isInstanceOf(UniqueConstraintViolationException.class)
                    .satisfies(ex -> assertThat(
                            ((UniqueConstraintViolationException) ex).getField()
                    ).isEqualTo("phone"));

            verify(employeeRepository, never()).save(any());
        }

        @Test
        void appliesOnlyPresentFieldsAndSaves() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
            when(employeeRepository.existsByEmail(fullUpdateRequest.getEmail().getValue()))
                    .thenReturn(false);
            when(employeeRepository.existsByPhone(fullUpdateRequest.getPhone().getValue()))
                    .thenReturn(false);
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

            Employee result = employeeService.update(employeeId, fullUpdateRequest);

            assertThat(existingEmployee.getFirstName()).isEqualTo("John");
            assertThat(existingEmployee.getLastName()).isEqualTo("Smith");
            assertThat(existingEmployee.getMiddleName()).isEqualTo("B");
            assertThat(existingEmployee.getEmail()).isEqualTo("john@example.com");
            assertThat(existingEmployee.getPhone()).isEqualTo("+9876543210");
            assertThat(existingEmployee.getRole()).isEqualTo(Employee.Role.MANAGER);
            assertThat(existingEmployee.isActive()).isFalse();

            verify(employeeRepository).save(existingEmployee);
            assertThat(result).isSameAs(existingEmployee);
        }

        @Test
        void skipsUniquenessChecksAndLeavesFieldsUnchangedWhenAllFieldsAbsent() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));
            when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

            employeeService.update(employeeId, emptyUpdateRequest);

            verify(employeeRepository, never()).existsByEmail(any());
            verify(employeeRepository, never()).existsByPhone(any());

            assertThat(existingEmployee.getFirstName()).isEqualTo("Jane");
            assertThat(existingEmployee.getEmail()).isEqualTo("jane@example.com");
            assertThat(existingEmployee.getPhone()).isEqualTo("+1234567890");

            verify(employeeRepository).save(existingEmployee);
        }
    }

    @Nested
    class Delete {

        @Test
        void throwsEntityNotFoundExceptionWhenEmployeeNotFound() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> employeeService.delete(employeeId))
                    .isInstanceOf(EntityNotFoundException.class);

            verify(employeeRepository, never()).delete(any());
        }

        @Test
        void callsRepositoryDeleteWithTheCorrectEmployee() {
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(existingEmployee));

            employeeService.delete(employeeId);

            verify(employeeRepository).delete(existingEmployee);
        }
    }
}
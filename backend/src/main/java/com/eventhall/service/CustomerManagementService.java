package com.eventhall.service;

import com.eventhall.dto.*;
import com.eventhall.entity.UserAccount;
import com.eventhall.enums.UserRole;
import com.eventhall.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Admin-only service for managing customer accounts.
 *
 * Rules enforced here:
 * - Only CUSTOMER-role users are managed; admin users are not accessible via this service.
 * - Email must be unique across all users.
 * - customerIdentityNumber must be unique if provided.
 * - Passwords are BCrypt-hashed; plaintext is never stored or returned.
 */
@Service
@Transactional
public class CustomerManagementService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerManagementService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // -----------------------------------------------------------------------
    // Read
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<CustomerResponse> listCustomers() {
        return userAccountRepository
                .findAllByRoleOrderByCreatedAtDesc(UserRole.CUSTOMER)
                .stream()
                .map(CustomerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long id) {
        return CustomerResponse.from(requireCustomer(id));
    }

    // -----------------------------------------------------------------------
    // Create
    // -----------------------------------------------------------------------

    public CustomerResponse createCustomer(CreateCustomerRequest req) {
        // Email uniqueness (across all users)
        if (userAccountRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "כתובת דוא\"ל כבר קיימת במערכת");
        }

        // Identity number uniqueness (if provided)
        if (req.customerIdentityNumber() != null
                && !req.customerIdentityNumber().isBlank()
                && userAccountRepository.existsByCustomerIdentityNumber(req.customerIdentityNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "מספר תעודת זהות כבר קיים במערכת");
        }

        UserAccount customer = UserAccount.builder()
                .fullName(req.fullName())
                .email(req.email().toLowerCase())
                .customerIdentityNumber(
                        req.customerIdentityNumber() != null && !req.customerIdentityNumber().isBlank()
                                ? req.customerIdentityNumber() : null)
                .phoneNumber(req.phoneNumber())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(UserRole.CUSTOMER)
                .active(true)
                .basePackagePrice(req.basePackagePrice())
                .build();

        return CustomerResponse.from(userAccountRepository.save(customer));
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest req) {
        UserAccount customer = requireCustomer(id);

        // Identity number uniqueness — exclude the current user from the check
        if (req.customerIdentityNumber() != null
                && !req.customerIdentityNumber().isBlank()
                && userAccountRepository.existsByCustomerIdentityNumberAndIdNot(
                        req.customerIdentityNumber(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "מספר תעודת זהות כבר קיים במערכת");
        }

        customer.setFullName(req.fullName());
        customer.setCustomerIdentityNumber(
                req.customerIdentityNumber() != null && !req.customerIdentityNumber().isBlank()
                        ? req.customerIdentityNumber() : null);
        customer.setPhoneNumber(req.phoneNumber());
        customer.setBasePackagePrice(req.basePackagePrice());

        return CustomerResponse.from(userAccountRepository.save(customer));
    }

    // -----------------------------------------------------------------------
    // Password
    // -----------------------------------------------------------------------

    public void changePassword(Long id, ChangeCustomerPasswordRequest req) {
        UserAccount customer = requireCustomer(id);
        customer.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userAccountRepository.save(customer);
    }

    // -----------------------------------------------------------------------
    // Enable / disable
    // -----------------------------------------------------------------------

    public CustomerResponse setActive(Long id, boolean active) {
        UserAccount customer = requireCustomer(id);
        customer.setActive(active);
        return CustomerResponse.from(userAccountRepository.save(customer));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Loads a UserAccount by id, verifying it exists and has the CUSTOMER role.
     * Throws 404 if not found; 400 if found but not a customer.
     */
    private UserAccount requireCustomer(Long id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "לקוח לא נמצא"));
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "משתמש זה אינו לקוח");
        }
        return user;
    }
}

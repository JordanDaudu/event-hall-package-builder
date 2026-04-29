package com.eventhall.service;

import com.eventhall.entity.UserAccount;
import com.eventhall.repository.UserAccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Loads {@link UserAccount} records and adapts them into Spring Security's
 * {@link UserDetails} model. The principal's username is the email.
 *
 * - Inactive accounts are loaded with {@code enabled=false} so Spring Security
 *   rejects them at authentication time.
 * - Roles are exposed with the standard {@code ROLE_} prefix so
 *   {@code hasRole("ADMIN")} works as expected in route guards.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public UserDetailsServiceImpl(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount account = userAccountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return User.builder()
                .username(account.getEmail())
                .password(account.getPasswordHash())
                .disabled(!account.isActive())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())))
                .build();
    }
}

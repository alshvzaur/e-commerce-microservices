package com.zaur.user_service.service;

import com.zaur.user_service.model.Role;
import com.zaur.user_service.model.User;
import com.zaur.user_service.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository userRepository;
//
//    @Mock
//    UserService userService;

    @InjectMocks
    UserService userService;

    @InjectMocks
    MyUserDetailService myUserDetailService;

    @Test
    void shouldFindUserByUsername() {
        String username = "test";

        User user = User.builder().username(username).build();

        when(userRepository.getUserByUsername(username)).thenReturn(Optional.of(user));

        User resultUser = userService.findByUsername(username);

        assertEquals(user, resultUser);

    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        String username = "test";

        when(userRepository.getUserByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.findByUsername(username));
    }

    @Test
    void shouldLoadUserDetailsByUsername() {
        User user = User.builder()
                .username("test")
                .password("test")
                .role(Role.USER)
                .build();

        when(userService.findByUsername("test")).thenReturn(user);

        UserDetails userDetails = myUserDetailService.loadUserByUsername("test");

        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());

        assertEquals("ROLE_" + user.getRole().name(), userDetails.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );

    }

    @Test
    void shouldSaveUser(){
        User user = User.builder()
                .username("test")
                .password("test")
                .role(Role.USER)
                .build();

        when(userRepository.save(user)).thenReturn(user);

        User resultUser = userService.saveUser(user);
        assertEquals(user, resultUser);
    }
}

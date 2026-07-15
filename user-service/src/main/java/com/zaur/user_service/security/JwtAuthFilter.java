package com.zaur.user_service.security;


import com.zaur.user_service.service.MyUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    JwtUtil jwtUtil;
    MyUserDetailService myUserDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Достань заголовок "Authorization" из request
        // 2. Если null или не начинается с "Bearer " — пропусти запрос дальше (filterChain.doFilter) и выйди
        // 3. Вытащи токен (строка после "Bearer ", то есть substring(7))
        // 4. Извлеки username из токена через jwtUtil
        // 5. Если username != null И SecurityContextHolder ещё не содержит аутентификацию:
        //    - загрузи UserDetails через userDetailService
        //    - проверь токен через jwtUtil.isValid(...)
        //    - если валиден — создай UsernamePasswordAuthenticationToken и положи в SecurityContext
        // 6. В конце всегда вызови filterChain.doFilter(request, response)


        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = myUserDetailService.loadUserByUsername(username);
                if(jwtUtil.isValid(token, username)){
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}

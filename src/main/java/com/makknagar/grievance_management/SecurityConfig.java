
package com.makknagar.grievance_management;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public InMemoryUserDetailsManager adminUser(
            PasswordEncoder passwordEncoder) {

        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("DemoAdmin123!"))
                .roles("ADMIN")
                .build();

        UserDetails waterOfficer = User.builder()
                .username("water_officer")
                .password(passwordEncoder.encode("Water123!"))
                .roles("WATER")
                .build();

        UserDetails roadsOfficer = User.builder()
                .username("roads_officer")
                .password(passwordEncoder.encode("Roads123!"))
                .roles("ROADS")
                .build();

        UserDetails electricityOfficer = User.builder()
                .username("electricity_officer")
                .password(passwordEncoder.encode("Electric123!"))
                .roles("ELECTRICITY")
                .build();

        UserDetails sanitationOfficer = User.builder()
                .username("sanitation_officer")
                .password(passwordEncoder.encode("Sanitation123!"))
                .roles("SANITATION")
                .build();

        return new InMemoryUserDetailsManager(
                admin,
                waterOfficer,
                roadsOfficer,
                electricityOfficer,
                sanitationOfficer
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler loginSuccessHandler() {

        return (request, response, authentication) -> {

            if (hasRole(authentication, "ADMIN")) {
                response.sendRedirect("/admin.html");
            } else if (hasRole(authentication, "WATER")) {
                response.sendRedirect("/water-dashboard.html");
            } else if (hasRole(authentication, "ROADS")) {
                response.sendRedirect("/roads-dashboard.html");
            } else if (hasRole(authentication, "ELECTRICITY")) {
                response.sendRedirect("/electricity-dashboard.html");
            } else if (hasRole(authentication, "SANITATION")) {
                response.sendRedirect("/sanitation-dashboard.html");
            } else {
                response.sendRedirect("/admin-login.html");
            }
        };
    }

    private boolean hasRole(
            Authentication authentication, String role) {

        return authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationSuccessHandler loginSuccessHandler)
            throws Exception {

        http
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                    "/api/citizens/**",
                    "/api/complaints/**",
                    "/api/admin/complaints/*/status",
                    "/api/department/complaints/*/status",
                    "/login",
                    "/logout"
            ))

            .authorizeHttpRequests(auth -> auth

                // Admin access
                .requestMatchers(
                    "/admin.html",
                    "/api/admin/**"
                ).hasRole("ADMIN")

                // Department PDF reports
                .requestMatchers(
                    "/api/department/reports/**"
                ).hasAnyRole(
                    "WATER",
                    "ROADS",
                    "ELECTRICITY",
                    "SANITATION"
                )

                // Water department
                .requestMatchers(
                    "/water-dashboard.html",
                    "/api/department/water/**"
                ).hasRole("WATER")

                // Roads department
                .requestMatchers(
                    "/roads-dashboard.html",
                    "/api/department/roads/**"
                ).hasRole("ROADS")

                // Electricity department
                .requestMatchers(
                    "/electricity-dashboard.html",
                    "/api/department/electricity/**"
                ).hasRole("ELECTRICITY")

                // Sanitation department
                .requestMatchers(
                    "/sanitation-dashboard.html",
                    "/api/department/sanitation/**"
                ).hasRole("SANITATION")

                // Department complaint status updates
                .requestMatchers(
                    "/api/department/complaints/**"
                ).hasAnyRole(
                    "WATER",
                    "ROADS",
                    "ELECTRICITY",
                    "SANITATION"
                )

                // Public pages and citizen APIs
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/register.html",
                    "/complaint.html",
                    "/track.html",
                    "/admin-login.html",
                    "/api/citizens/**",
                    "/api/complaints/**",
                    "/login"
                ).permitAll()

                .anyRequest().permitAll()
            )

            .formLogin(form -> form
                .loginPage("/admin-login.html")
                .loginProcessingUrl("/login")
                .successHandler(loginSuccessHandler)
                .failureUrl("/admin-login.html?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutSuccessUrl("/admin-login.html?logout")
            );

        return http.build();
    }
}

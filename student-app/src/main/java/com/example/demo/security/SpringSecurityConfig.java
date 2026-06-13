package com.example.demo.security;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/access-denied").permitAll()
                .requestMatchers("/manageStudents").hasRole("PROFESSOR")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userAuthoritiesMapper(grantedAuthoritiesMapper()))
            )
            .logout(logout -> logout
                .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
            )
            .exceptionHandling(exception -> exception.accessDeniedPage("/access-denied"));

        return http.build();
    }

    /**
     * On logout, also end the Keycloak SSO session (not just this app's local
     * session) so the user is signed out of professor-app/student-app together.
     */
    private LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler successHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        successHandler.setPostLogoutRedirectUri("{baseUrl}");
        return successHandler;
    }

    /**
     * Keycloak puts realm roles under the "realm_access.roles" claim, which Spring
     * Security does not map to authorities by default. This maps each realm role
     * (e.g. "PROFESSOR") to a "ROLE_PROFESSOR" authority for use with hasRole(...).
     */
    private GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mapped = new HashSet<>(authorities);

            for (GrantedAuthority authority : authorities) {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    Map<String, Object> claims = oidcUserAuthority.getIdToken().getClaims();
                    Object realmAccess = claims.get("realm_access");
                    if (realmAccess instanceof Map<?, ?> realmAccessMap) {
                        Object roles = realmAccessMap.get("roles");
                        if (roles instanceof Iterable<?> roleNames) {
                            for (Object roleName : roleNames) {
                                mapped.add(new SimpleGrantedAuthority("ROLE_" + roleName.toString().toUpperCase()));
                            }
                        }
                    }
                }
            }

            return mapped;
        };
    }
}

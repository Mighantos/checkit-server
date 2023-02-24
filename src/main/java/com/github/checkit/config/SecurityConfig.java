package com.github.checkit.config;

import com.github.checkit.config.properties.KeycloakConfigProperties;
import com.github.checkit.security.KeycloakJwtGrantedAuthoritiesConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final KeycloakConfigProperties keycloakConfigProperties;

    public SecurityConfig(KeycloakConfigProperties keycloakConfigProperties) {
        this.keycloakConfigProperties = keycloakConfigProperties;
    }

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("keycloak")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientId(keycloakConfigProperties.getClientId())
                .clientSecret(keycloakConfigProperties.getSecret())
                .issuerUri(keycloakConfigProperties.getIssuerUrl())
                .tokenUri(keycloakConfigProperties.getTokenUrl())
                .userInfoUri(keycloakConfigProperties.getUserInfoUrl())
                .redirectUri(keycloakConfigProperties.getAuthorizationUrl())
                .authorizationUri(keycloakConfigProperties.getAuthorizationUrl())
                .build();
        return new InMemoryClientRegistrationRepository(clientRegistration);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(keycloakConfigProperties.getJwksUrl()).build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests().requestMatchers("/**").permitAll();
        http.oauth2Login(Customizer.withDefaults());
        http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(KeycloakAuthenticationConverter());
        http.cors().and().cors().disable();
        return http.build();
    }

    protected JwtAuthenticationConverter KeycloakAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtGrantedAuthoritiesConverter(keycloakConfigProperties));
        return converter;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        // We're allowing all methods from all origins so that the application API is usable also by
        // other clients
        // than just the UI.
        // This behavior can be restricted later.
        final CorsConfiguration corsConfiguration =
                new CorsConfiguration().applyPermitDefaultValues();
        corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
        corsConfiguration.setAllowedOrigins(Collections.singletonList("*"));
        corsConfiguration.addExposedHeader(HttpHeaders.AUTHORIZATION);
        corsConfiguration.addExposedHeader(HttpHeaders.LOCATION);
        corsConfiguration.addExposedHeader(HttpHeaders.CONTENT_DISPOSITION);
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}

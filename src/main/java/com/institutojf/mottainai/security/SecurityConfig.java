package com.institutojf.mottainai.security;

import com.institutojf.mottainai.model.AppUser;
import com.institutojf.mottainai.repository.AppUserRepository;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({JwtProperties.class, FirebaseProperties.class})
public class SecurityConfig {
    private static final String FIREBASE_JWK_SET_URI =
            "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";

    private final JwtProperties jwtProperties;
    private final FirebaseProperties firebaseProperties;
    private final AppUserRepository appUserRepository;
    private final List<String> allowedOrigins;

    public SecurityConfig(JwtProperties jwtProperties, AppUserRepository appUserRepository) {
        this(jwtProperties, new FirebaseProperties(""), appUserRepository, "");
    }

    public SecurityConfig(JwtProperties jwtProperties, FirebaseProperties firebaseProperties, AppUserRepository appUserRepository) {
        this(jwtProperties, firebaseProperties, appUserRepository, "");
    }

    @Autowired
    public SecurityConfig(JwtProperties jwtProperties, FirebaseProperties firebaseProperties, AppUserRepository appUserRepository, @Value("${security.cors.allowed-origins:}") String allowedOrigins) {
        this.jwtProperties = jwtProperties;
        this.firebaseProperties = firebaseProperties;
        this.appUserRepository = appUserRepository;
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain clientSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/v1/client/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(firebaseJwtDecoder())));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain staffSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password",
                                "/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/actuator/health"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Location"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(jwtSecretKey()));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()),
                tokenVersionValidator()
        ));
        return decoder;
    }

    private JwtDecoder firebaseJwtDecoder() {
        String firebaseIssuer = "https://securetoken.google.com/" + firebaseProperties.projectId();
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(FIREBASE_JWK_SET_URI).build();
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> jwt.getAudience().contains(firebaseProperties.projectId())
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid Firebase audience", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(firebaseIssuer), audienceValidator
        ));
        return decoder;
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }

    /**
     * Compara a versão do JWT com a versão atual do usuário
     * Quando a senha muda, tokens antigos deixam de funcionar
     */
    private OAuth2TokenValidator<Jwt> tokenVersionValidator() {
        return jwt -> appUserRepository.findByEmailIgnoreCaseAndActiveTrueAndDeletedAtIsNull(jwt.getSubject())
                .filter(user -> hasCurrentTokenVersion(user, jwt))
                .map(user -> OAuth2TokenValidatorResult.success())
                .orElseGet(() -> OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("invalid_token", "Token is no longer valid", null)
                ));
    }

    private boolean hasCurrentTokenVersion(AppUser user, Jwt jwt) {
        Object tokenVersion = jwt.getClaim("tokenVersion");
        if (!(tokenVersion instanceof Number version) || user.getTokenVersion() == null
                || user.getTokenVersion() != version.intValue()) {
            return false;
        }
        if (user.getEmployee() == null) {
            return true;
        }
        return Boolean.TRUE.equals(user.getEmployee().getActive())
                && user.getEmployee().getDeletedAt() == null
                && user.getEmployee().getRole() != null
                && Boolean.TRUE.equals(user.getEmployee().getRole().getActive())
                && user.getEmployee().getRole().getDeletedAt() == null;
    }

    private SecretKey jwtSecretKey() {
        try {
            byte[] secret = Base64.getDecoder().decode(jwtProperties.secret());
            if (secret.length < 32) {
                throw new IllegalStateException("JWT secret must contain at least 256 bits");
            }
            return new SecretKeySpec(secret, "HmacSHA256");
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT secret must be Base64 encoded", exception);
        }
    }
}

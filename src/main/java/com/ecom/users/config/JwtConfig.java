package com.ecom.users.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

    public JwtConfig(RsakeysConfig rsakeysConfig) {
        this.rsakeysConfig = rsakeysConfig;
    }

    @Value("${SAS_JWK_URI}")
    private String jwtUri;

    private RsakeysConfig rsakeysConfig;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Qualifier("userJwtDecoder")
    public JwtDecoder UserjwtDecoder() {
        //on récupère la key public en local pour décoder le JWT Users
        return NimbusJwtDecoder.withPublicKey(rsakeysConfig.publicKey()).build();
    }

    @Bean
    @Qualifier("resourceJwtDecoder")
    public JwtDecoder resourceJwtDecoder() {
        //on récupère la key public pour décoder les JWT inter-service auprès du service Sécurty
        return NimbusJwtDecoder
                .withJwkSetUri(jwtUri)
                .build();
    }
}

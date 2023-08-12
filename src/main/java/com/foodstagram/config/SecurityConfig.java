package com.foodstagram.config;

import com.foodstagram.config.exception.MyAccessDeniedHandler;
import com.foodstagram.config.exception.MyAuthenticationEntryPoint;
import com.foodstagram.config.handler.MyLogoutHandler;
import com.foodstagram.config.jwt.*;
import com.foodstagram.config.oauth.OAuth2UserSuccessHandler;
import com.foodstagram.config.oauth.PrincipalOauth2UserService;
import com.foodstagram.config.redis.RedisService;
import com.foodstagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final PrincipalOauth2UserService principalOauth2UserService;
    private final OAuth2UserSuccessHandler oAuth2UserSuccessHandler;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final MessageSource ms;
    private final JwtTokenService jwtTokenService;
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final MyAuthenticationEntryPoint myAuthenticationEntryPoint; // 권한이 없는 경우
    private final MyAccessDeniedHandler myAccessDeniedHandler; // 로그인 하지 않은 경우
    private final MyLogoutHandler myLogoutHandler;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/loginForm", "/user/join", "/user/loginIdCheck", "/user/emailCheck"
                        , "/css/**", "/image/**", "/js/**", "/picture/**"
                );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .formLogin(form -> form.disable())
            .oauth2Login(oauth -> oauth
                    .userInfoEndpoint(userInfo -> userInfo.userService(principalOauth2UserService))
                    .successHandler(oAuth2UserSuccessHandler) // Oauth2 로그인 시 토큰 발급
            )
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 비활성화
            .httpBasic(basic -> basic.disable())
            .addFilter(new JwtAuthenticationFilter(authenticationManager(authenticationConfiguration), ms, jwtTokenService, redisService)) // 로그인 시도 및 JWT 토큰 발행
            .addFilter(new JwtAuthorizationFilter(authenticationManager(authenticationConfiguration), ms, jwtTokenService, userRepository)) // 인증이나 권한 요청 시 JWT 토큰 유효한지 확인
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/user/**", "/food/**").authenticated() // 인증해야 들어갈 수 있는 주소
                    .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                    .anyRequest().permitAll()
            )
            .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer
                    .authenticationEntryPoint(myAuthenticationEntryPoint) // 로그인 안한 경우
                    .accessDeniedHandler(myAccessDeniedHandler)) // 권한이 없는 경우
            .logout(logout -> logout
                    .invalidateHttpSession(true)
                    .deleteCookies(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, JwtProperties.REFRESH_TOKEN_COOKIE_NAME)
                    .addLogoutHandler(myLogoutHandler)
                    .logoutSuccessUrl("/")
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true); // 서버가 응답을 할 때 json을 자바스크립트에서 처리할 수 있게 함
        configuration.setAllowedOrigins(Arrays.asList("*")); // 모든 ip 에서 응답 허용
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 header 에 응답 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST")); // 모든 method 에 응답 허용
        configuration.setExposedHeaders(Arrays.asList("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}

package shop.mtcoding.tddbank._core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import shop.mtcoding.tddbank._core.errors.exception.Exception401;
import shop.mtcoding.tddbank._core.errors.exception.Exception403;
import shop.mtcoding.tddbank._core.util.FilterResponseUtils;

@Configuration  // component에 등록됨
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    public class CustomSecurityFilterManager extends AbstractHttpConfigurer<CustomSecurityFilterManager, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            builder.addFilter(new JwtAuthenticationFilter(authenticationManager));
            super.configure(builder);
        }
    }


    @Bean // 컴포넌트 스캔시에 @Bean이 붙은 메서드가 있으면 실행시켜서 return되는 값을 IoC에 등록하는 깃발 (기존에 작동하던 시큐리티 필터체인의 작동이 멈춤)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 1. CSRF 해제
        http.csrf().disable(); // postman 접근해야 함!! - CSR 할때!!

        // 2. iframe 거부
        http.headers().frameOptions().sameOrigin();

        // 3. cors 재설정
        http.cors().configurationSource(configurationSource());

        // 4. jSessionId가 응답이 될 때 세션영 역에서 사라진다 (이게 stateless)
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 5. form 로긴 해제 (UsernamePasswordAuthenticationFilter 비활성화)
        http.formLogin().disable();

        // 6. 로그인 인증창이 뜨지 않게 비활성화 (로그인처리 UserController에서)
        http.httpBasic().disable();

        // 7. 커스텀 필터 적용 (시큐리티 필터 교환)
        http.apply(new CustomSecurityFilterManager());

        // 8. 인증 실패 처리
        http.exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
            FilterResponseUtils.unAuthorized(response, new Exception401("인증되지 않았습니다."));
        });

        // 9. 권한 실패 처리
        http.exceptionHandling().accessDeniedHandler((request, response, accessDeniedException) -> {
            FilterResponseUtils.forbidden(response, new Exception403("권한이 없습니다."));
        });

        // 11. 인증, 권한 필터 설정
        http.authorizeRequests(
                authorize -> authorize.antMatchers("/account/**").authenticated()   // 인증 체크
                        .antMatchers("/admin/**").access("hasRole('ADMIN')")    // 인증+권한 체크
                        .anyRequest().permitAll()
        );
        return http.build();
    }

    public CorsConfigurationSource configurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*"); // GET, POST, PUT, DELETE (Javascript 요청 허용)
        // localhost:8080 백엔드, localhost:3000 프론트엔드
        configuration.addAllowedOriginPattern("*"); // 모든 IP 주소 허용 (프론트 앤드 IP만 허용 react)
        configuration.setAllowCredentials(true); // 클라이언트에서 쿠키 요청 허용
        configuration.addExposedHeader("Authorization"); // 옛날에는 디폴트 였다. 지금은 아닙니다. (jwt는 Authorization 헤더를 통해서 들어옴)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // /login, /board, /product/ 모든 요청이 들어올때 Cors 설정대로 동작
        return source;
    }
}

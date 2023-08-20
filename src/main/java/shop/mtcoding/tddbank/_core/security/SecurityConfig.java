package shop.mtcoding.tddbank._core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration  // component에 등록됨
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return new BCryptPasswordEncoder();
    }

    @Bean // 컴포넌트 스캔시에 @Bean이 붙은 메서드가 있으면 실행시켜서 return되는 값을 IoC에 등록하는 깃발 (기존에 작동하던 시큐리티 필터체인의 작동이 멈춤)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 1. CSRF 해제
        http.csrf().disable();

        http.formLogin()
                .loginProcessingUrl("/login"); // POST /long -> x-www-form-urlencoded (필터에 기본적으로 만들어져있는 로그인 로직)

        http.authorizeHttpRequests(authorize -> authorize.antMatchers("/").authenticated().anyRequest().permitAll());

        return http.build();
    }
}

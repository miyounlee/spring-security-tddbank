package shop.mtcoding.tddbank._core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import shop.mtcoding.tddbank.user.UserRequest;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    // Filter가 컴포넌트 스캔 대상이 아니고 new 해서 던질거기 때문에 Manager를 인자로 받아옴
    public CustomUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.authenticationManager = authenticationManager;
    }

    // /login POST 요청일 때 동작
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        ObjectMapper om = new ObjectMapper();

        try {
            UserRequest.LoginDTO loginDTO = om.readValue(request.getInputStream(), UserRequest.LoginDTO.class); // json 객체를 dto로 받음
            UsernamePasswordAuthenticationToken token =
                    UsernamePasswordAuthenticationToken.unauthenticated(loginDTO.getUsername(),
                    loginDTO.getPassword());
            Authentication authentication = authenticationManager.authenticate(token);  // UserDetailsService의 loadUserByUsername 이 호출됨
            return authentication;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // UserDetailsSevice가 UserDetails를 잘 리턴하면 실행됨
        System.out.println("로그인 성공함!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        super.successfulAuthentication(request, response, chain, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        // 인증서비스가 실행되다가 익셉션이 터지면 실행
        System.out.println("로그인 중에 실패함.......................................");
        super.unsuccessfulAuthentication(request, response, failed);
    }
}

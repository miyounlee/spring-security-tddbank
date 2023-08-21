package shop.mtcoding.tddbank.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shop.mtcoding.tddbank._core.errors.exception.Exception401;
import shop.mtcoding.tddbank._core.security.CustomUserDetails;
import shop.mtcoding.tddbank._core.security.JwtTokenProvider;
import shop.mtcoding.tddbank._core.util.ApiUtils;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;  // 원래 서비스에서

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody UserRequset.JoinDTO joinDTO) {   // json으로 입력받으려면 @RequestBody 필요 (기본은 x-www-form-urlencoded)
        // 1. 유효성 검사

        // 2. 회원가입 (원래는 서비스 요청) - 시큐리티는 password 인코딩이 무조건 되어야 한다.
        joinDTO.setPassword(passwordEncoder.encode(joinDTO.getPassword()));
        userRepository.save(joinDTO.toEntity());

        // 3. 응답

        return ResponseEntity.ok().body("ok");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequset.LoginDTO loginDTO) {

        try {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                    = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
            Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);    // UserDetailsService 호출됨 -> authentication객체가 SecurityContextHolder에 들어감
            CustomUserDetails myUserDetails = (CustomUserDetails) authentication.getPrincipal();
            System.out.println("myUserDetails : " + myUserDetails.getUser().getFullName());

            // JWT 토큰 만들기
            String jwt = JwtTokenProvider.create(myUserDetails.getUser());
            return ResponseEntity.ok().header("Authorization", jwt).body(ApiUtils.success(null));

        } catch (Exception e) {
            throw new Exception401("인증되지 않았습니다.");
        }
    }
}

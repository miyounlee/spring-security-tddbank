package shop.mtcoding.tddbank.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shop.mtcoding.tddbank._core.errors.exception.Exception401;
import shop.mtcoding.tddbank._core.security.CustomUserDetails;
import shop.mtcoding.tddbank._core.security.JwtTokenProvider;
import shop.mtcoding.tddbank._core.util.ApiUtils;

import javax.validation.Valid;

// 컨트롤러의 책임 - 요청 값 잘 받기, 유효성 검사 잘하기, 서비스 호출 잘하기, 응답 잘하기, 인증 체크(시큐리티가 해줌)
@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;  // 원래 서비스에서

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody @Valid UserRequset.JoinDTO joinDTO, Errors errors) {   // json으로 입력받으려면 @RequestBody 필요 (기본은 x-www-form-urlencoded)

        UserResponse.JoinDTO responseDTO = userService.회원가입(joinDTO);

        return ResponseEntity.ok().body(ApiUtils.success(responseDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserRequset.LoginDTO loginDTO, Errors errors) {

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

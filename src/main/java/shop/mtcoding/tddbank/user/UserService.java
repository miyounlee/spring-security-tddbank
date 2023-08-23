package shop.mtcoding.tddbank.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 서비스의 책임 - 비지니스 로직 관리, 트랜잭션 관리, DTO를 만듦
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse.JoinDTO 회원가입(UserRequset.JoinDTO joinDTO) {

        // 시큐리티는 password 인코딩이 무조건 되어야 한다.
        joinDTO.setPassword(passwordEncoder.encode(joinDTO.getPassword()));
        User userPS = userRepository.save(joinDTO.toEntity());

        return new UserResponse.JoinDTO(userPS);
    } // 끝날때 commit, 영속화된 엔티티가 변경되면 flush() 더티체킹
}

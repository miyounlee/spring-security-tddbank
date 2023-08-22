package shop.mtcoding.tddbank.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ActiveProfiles("test") // 개발과 테스트는 분리시켜야 함!!
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)    // 가짜 스프링 환경
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void join_test() throws Exception {
        // given
        UserRequset.JoinDTO joinDTO = new UserRequset.JoinDTO();
        joinDTO.setUsername("love");
        joinDTO.setEmail("love@gmail.com");
        joinDTO.setPassword("1234");
        joinDTO.setFullName("러브");

        ObjectMapper om = new ObjectMapper();
        String requestBody = om.writeValueAsString(joinDTO);    // 직렬화 (dto -> json)
        System.out.println("테스트 : " + requestBody); // 변환 눈으로 확인

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post("/join").content(requestBody).contentType(MediaType.APPLICATION_JSON));   // builder로 endExpect 쓰지말고 when, then 분리하기
        String responseBody = resultActions.andReturn().getResponse().getContentAsString();
        System.out.println("테스트 : " + responseBody);

        // then
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.response.id").value(1));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.response.username").value("love"));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.response.email").value("love@gmail.com"));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.response.fullName").value("러브"));
    }
}

package predawn.global.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RequestFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void cleanUp() {
        redisTemplate
            .getConnectionFactory()
            .getConnection()
            .serverCommands()
            .flushAll();
    }

    @DisplayName("요청 제한을 넘어서면 429 too many requests 응답이 온다")
    @Test
    void doFilterInternal_TooManyRequests() throws Exception {
        for (int i = 0; i < 15; i++) {
            mockMvc.perform(post("/login"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/login"))
                .andExpect(status().isTooManyRequests());
    }

    @DisplayName("요청 제한을 넘어서지 않으면 정상 응답한다")
    @Test
    void doFilterInternal() throws Exception {
        for (int i = 0; i < 14; i++) {
            mockMvc.perform(post("/login"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/login"))
                .andExpect(status().isOk());
    }
}
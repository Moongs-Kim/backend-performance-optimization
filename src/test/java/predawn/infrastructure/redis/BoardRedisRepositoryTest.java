package predawn.infrastructure.redis;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BoardRedisRepositoryTest {

    @Autowired
    private BoardRedisRepository boardRedisRepository;

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

    @DisplayName("존재하지 않는 키로 등록하면 저장된다")
    @Test
    void saveViewIfNotExists() {
        //Given
        Long boardId = 1L;
        Long memberId = 1L;

        //When
        Boolean isSaved = boardRedisRepository.saveViewIfNotExists(boardId, memberId);

        //Then
        assertThat(isSaved).isTrue();
    }

    @DisplayName("존재하는 키로 등록하면 저장되지 않는다")
    @Test
    void saveViewIfNotExists_Fail() {
        //Given
        Long boardId = 1L;
        Long memberId = 1L;

        Boolean firstSaved = boardRedisRepository.saveViewIfNotExists(boardId, memberId);

        //When
        Boolean secondSaved = boardRedisRepository.saveViewIfNotExists(boardId, memberId);

        //Then
        assertThat(firstSaved).isTrue();
        assertThat(secondSaved).isFalse();
    }
}
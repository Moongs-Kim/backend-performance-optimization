package predawn.support.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import predawn.domain.board.enums.CategoryName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@Profile("load")
@RequiredArgsConstructor
public class DataInitializer {

    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 1000;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        insertMembers();
        insertCategories();
        insertBoards();
        insertLikes();

        createIndexes();

        log.info("Data Initialization Finish");
    }

    private void insertMembers() {
        String sql =
                "INSERT INTO member" +
                " (login_id, password, name, birth_date, gender, email, address)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = 1; i <= 100_000; i++) {

            batchArgs.add(new Object[]{
                    "user" + i,
                    "1234",
                    "user" + i,
                    LocalDate.now(),
                    "MALE",
                    "user" + i + "@test.com",
                    "주소"
            });

            if (i % BATCH_SIZE == 0) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    private void insertCategories() {
        String sql = "INSERT INTO category (name) VALUES (?)";

        CategoryName[] categoryNames = CategoryName.values();

        for (int i = 0; i < categoryNames.length; i++) {
            jdbcTemplate.update(sql, categoryNames[i].name());
        }
    }

    private void insertBoards() {
        String sql =
                "INSERT INTO board" +
                " (member_id, title, content, board_open, category_id, view_count, created_date, last_modified_date)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();

        LocalDateTime baseTime = LocalDateTime.now().minusDays(100_000);

        Random random = new Random();

        for (int i = 1; i <= 100_000; i++) {

            batchArgs.add(new Object[]{
                    i,
                    "게시글 제목" + i,
                    "게시글 내용" + i,
                    "ALL",
                    1L,
                    random.nextInt(10000),
                    baseTime.plusDays(i),
                    baseTime.plusDays(i)
            });

            if (i % BATCH_SIZE == 0) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    private void insertLikes() {
        String sql = "INSERT INTO likes (member_id, board_id) VALUES (?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();

        Random random = new Random();

        Set<String> uniqueSet = new HashSet<>();

        for (int i = 1; i < 20_000; i++) {

            long memberId = random.nextInt(100_000) + 1;
            long boardId;

            if (i < 2000) {
                boardId = 100_000 - random.nextInt(100);
            } else if (i < 6000) {
                boardId = 100_000 - random.nextInt(1000);
            } else {
                boardId = 100_000 - random.nextInt(10_000);
            }

            String key = memberId + "-" + boardId;

            if (!uniqueSet.add(key)) {
                i--;
                continue;
            }

            batchArgs.add(new Object[]{memberId, boardId});

            if (i % BATCH_SIZE == 0) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }

    }

    private void createIndexes() {
        String deletedAtCreatedDAteIndexSql = "CREATE INDEX idx_board_deleted_at_created_date_desc ON board (deleted_at, created_date DESC)";
        String deletedAtViewCountIndexSql = "CREATE INDEX idx_board_deleted_at_view_count_desc ON board (deleted_at, view_count DESC)";

        jdbcTemplate.execute(deletedAtCreatedDAteIndexSql);
        jdbcTemplate.execute(deletedAtViewCountIndexSql);
    }
}

package predawn.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import predawn.application.file.service.FileCleanupService;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupScheduler {

    private final FileCleanupService fileCleanupService;

    @Scheduled(cron = "0 0 0/1 * * *")
    @SchedulerLock(
            name = "boardStoredFileCleanup",
            lockAtMostFor = "30m",
            lockAtLeastFor = "5m"
    )
    public void boardFileCleanupScheduler() {
        log.info("boardFileCleanupScheduler Start");
        try {
            fileCleanupService.boardFileCleanup();
        } catch (Exception e) {
            log.error("boardFileCleanupScheduler Error Message: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 0/1 * * *")
    @SchedulerLock(
            name = "memberStoredFileCleanup",
            lockAtMostFor = "30m",
            lockAtLeastFor = "5m"
    )
    public void memberFileCleanupScheduler() {
        log.info("memberFileCleanupScheduler Start");
        try {
            fileCleanupService.memberFileCleanup();
        } catch (Exception e) {
            log.error("memberFileCleanupScheduler Error Message: {}", e.getMessage(), e);
        }
    }
}

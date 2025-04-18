package capstone.bookitty.domain.bookSimilarity.batchSchedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class BookSimilarityScheduler {

    private final JobLauncher jobLauncher;
    private final Job bookSimilarityJob;

    @Scheduled(cron = "0 0 4 * * ?", zone = "Asia/Seoul") // 매일 새벽 4시
    public void runCalSimilarityJob() throws Exception {
        log.info("[Scheduler] 전체 유사도 계산 배치 시작");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("runDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                .toJobParameters();

        jobLauncher.run(bookSimilarityJob, jobParameters);

        log.info("[Scheduler] 전체 유사도 계산 배치 종료");
    }
}

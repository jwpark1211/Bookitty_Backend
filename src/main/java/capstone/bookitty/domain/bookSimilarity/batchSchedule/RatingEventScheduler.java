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
public class RatingEventScheduler {

    private final JobLauncher jobLauncher;
    private final Job ratingEventSimilarityJob;

    @Scheduled(fixedDelay = 600000) // 10분마다 실행 (600,000ms)
    public void runRatingEventJob() throws Exception {
        log.info("[Scheduler] 이벤트 기반 유사도 계산 배치 시작");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("runDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                .toJobParameters();

        jobLauncher.run(ratingEventSimilarityJob, jobParameters);

        log.info("[Scheduler] 이벤트 기반 유사도 계산 배치 종료");
    }
}

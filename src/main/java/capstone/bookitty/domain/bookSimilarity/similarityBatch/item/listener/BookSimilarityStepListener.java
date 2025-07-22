package capstone.bookitty.domain.bookSimilarity.similarityBatch.item.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookSimilarityStepListener implements StepExecutionListener {

    private long startTime;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        startTime = System.currentTimeMillis();
        log.info("📊 ==================== STEP 시작 ====================");
        log.info("🔧 Step 이름: {}", stepExecution.getStepName());
        log.info("⏰ 시작 시간: {}", stepExecution.getStartTime());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;

        long totalItems = stepExecution.getReadCount();
        long processedItems = stepExecution.getWriteCount();
        long skippedItems = stepExecution.getSkipCount();
        long failedItems = stepExecution.getReadSkipCount() + stepExecution.getWriteSkipCount();

        // 성능 지표 계산
        double totalSeconds = totalDuration / 1000.0;

        log.info("📈 ==================== STEP 완료 ====================");
        log.info("⏱️  총 실행 시간: " + totalSeconds + "초 " + totalSeconds / 60 + "분");
        log.info("📚 총 읽은 아이템: {}개", totalItems);
        log.info("✅ 성공 처리된 아이템: {}개", processedItems);
        log.info("⏭️  건너뛴 아이템: {}개", skippedItems);
        log.info("❌ 실패한 아이템: {}개", failedItems);
        log.info("📊 처리 성공률:", (processedItems * 100.0) / Math.max(1, totalItems));


        log.info("📊 =====================================================");

        return stepExecution.getExitStatus();
    }

    /*@Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("--- Step 완료: {} ---", stepExecution.getStepName());
        log.info("읽은 아이템 수: {}", stepExecution.getReadCount());
        log.info("처리된 아이템 수: {}", stepExecution.getWriteCount());
        log.info("건너뛴 아이템 수: {}", stepExecution.getSkipCount());
        log.info("실패한 아이템 수: {}", stepExecution.getReadSkipCount() + stepExecution.getWriteSkipCount());

        return stepExecution.getExitStatus();
    }*/

}

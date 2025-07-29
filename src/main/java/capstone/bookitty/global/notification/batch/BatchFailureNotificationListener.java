package capstone.bookitty.global.notification.batch;

import capstone.bookitty.global.notification.slack.SlackNotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchFailureNotificationListener implements JobExecutionListener {

    private final SlackNotificationClient slackNotificationClient;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus().isUnsuccessful()) {
            // Check if it's a critical batch failure
            if (isCriticalBatchFailure(jobExecution)) {
                String jobName = jobExecution.getJobInstance().getJobName();
                Exception exception = getJobException(jobExecution);
                
                log.warn("Critical batch failure detected for job: {}", jobName);
                slackNotificationClient.sendBatchFailureNotification(jobName, null, exception);
            }
        }
    }

    private boolean isCriticalBatchFailure(JobExecution jobExecution) {
        // Critical batch failures that require immediate attention
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            if (stepExecution.getFailureExceptions() != null) {
                for (Throwable throwable : stepExecution.getFailureExceptions()) {
                    if (isCriticalBatchException(throwable)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isCriticalBatchException(Throwable throwable) {
        return throwable instanceof org.springframework.dao.DataAccessException ||
               throwable instanceof OutOfMemoryError ||
               throwable instanceof org.springframework.transaction.TransactionException ||
               throwable instanceof java.sql.SQLException ||
               throwable instanceof org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
    }

    private Exception getJobException(JobExecution jobExecution) {
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            if (!stepExecution.getFailureExceptions().isEmpty()) {
                Throwable throwable = stepExecution.getFailureExceptions().get(0);
                return throwable instanceof Exception ? (Exception) throwable : 
                       new RuntimeException("Batch execution failed", throwable);
            }
        }
        return new RuntimeException("Unknown batch failure: " + jobExecution.getExitStatus().getExitDescription());
    }
}
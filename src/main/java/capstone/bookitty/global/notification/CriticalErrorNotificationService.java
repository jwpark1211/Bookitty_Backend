package capstone.bookitty.global.notification;

import capstone.bookitty.global.notification.slack.SlackNotificationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CriticalErrorNotificationService {

    private final SlackNotificationClient slackNotificationClient;

    public void handleException(Exception e, String layer, String className, String methodName, long executionTime) {
        if (isCriticalError(e)) {
            String errorMessage = String.format("[%s Layer] %s.%s 실행 중 심각한 예외 발생 (실행시간: %dms)",
                    layer, className, methodName, executionTime);

            log.warn("Critical error detected, sending Slack notification: {}", e.getMessage());
            slackNotificationClient.sendErrorNotification(errorMessage, className, methodName, e);
        }
    }

    private boolean isCriticalError(Exception e) {
        return e instanceof org.springframework.dao.DataAccessException ||
                e instanceof NullPointerException ||
                e instanceof IllegalStateException ||
                e instanceof SecurityException ||
                e instanceof org.springframework.transaction.TransactionException;
    }
}
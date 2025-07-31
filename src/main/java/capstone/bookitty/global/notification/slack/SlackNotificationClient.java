package capstone.bookitty.global.notification.slack;

import capstone.bookitty.global.notification.slack.dto.SlackMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlackNotificationClient {

    private final RestTemplate restTemplate;

    @Value("${slack.webhook.url:}")
    private String webhookUrl;

    @Value("${slack.notification.enabled:false}")
    private boolean notificationEnabled;

    public void sendErrorNotification(String errorMessage, String className, String methodName, Exception exception) {
        if (!notificationEnabled || webhookUrl.isEmpty()) {
            log.debug("Slack notification is disabled or webhook URL is not configured");
            return;
        }

        try {
            String message = buildErrorMessage(errorMessage, className, methodName, exception);
            SlackMessage slackMessage = SlackMessage.error(message);
            sendMessage(slackMessage);
        } catch (Exception e) {
            log.error("Failed to send Slack notification", e);
        }
    }

    public void sendBatchFailureNotification(String jobName, String stepName, Exception exception) {
        if (!notificationEnabled || webhookUrl.isEmpty()) {
            log.debug("Slack notification is disabled or webhook URL is not configured");
            return;
        }

        try {
            String message = buildBatchFailureMessage(jobName, stepName, exception);
            SlackMessage slackMessage = SlackMessage.batch(message);
            sendMessage(slackMessage);
        } catch (Exception e) {
            log.error("Failed to send Slack batch notification", e);
        }
    }

    public void sendRedisFailureNotification(String errorMessage) {
        if (!notificationEnabled || webhookUrl.isEmpty()) {
            log.debug("Slack notification is disabled or webhook URL is not configured");
            return;
        }

        try {
            String message = buildRedisFailureMessage(errorMessage);
            SlackMessage slackMessage = SlackMessage.error(message);
            sendMessage(slackMessage);
        } catch (Exception e) {
            log.error("Failed to send Redis failure notification", e);
        }
    }

    private void sendMessage(SlackMessage message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SlackMessage> request = new HttpEntity<>(message, headers);
        restTemplate.postForEntity(webhookUrl, request, String.class);

        log.info("Slack notification sent successfully");
    }

    private String buildErrorMessage(String errorMessage, String className, String methodName, Exception exception) {
        return String.format(
                "```\n" +
                        "⏰ Time: %s\n" +
                        "🏷️ Class: %s\n" +
                        "🔧 Method: %s\n" +
                        "❌ Error: %s\n" +
                        "🔍 Exception: %s\n" +
                        "```",
                java.time.LocalDateTime.now(),
                className,
                methodName,
                errorMessage,
                exception.getMessage()
        );
    }

    private String buildBatchFailureMessage(String jobName, String stepName, Exception exception) {
        return String.format(
                "```\n" +
                        "⏰ Time: %s\n" +
                        "🏷️ Job: %s\n" +
                        "📋 Step: %s\n" +
                        "❌ Failure: %s\n" +
                        "```",
                java.time.LocalDateTime.now(),
                jobName,
                stepName != null ? stepName : "N/A",
                exception.getMessage()
        );
    }

    private String buildRedisFailureMessage(String errorMessage) {
        return String.format(
                "```\n" +
                        "⏰ Time: %s\n" +
                        "🔴 Redis: Connection Failed\n" +
                        "❌ Error: %s\n" +
                        "📋 Impact: Cache disabled, using API directly\n" +
                        "```",
                java.time.LocalDateTime.now(),
                errorMessage
        );
    }
}
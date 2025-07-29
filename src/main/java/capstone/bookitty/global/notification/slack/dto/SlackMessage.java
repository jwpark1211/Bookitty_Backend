package capstone.bookitty.global.notification.slack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SlackMessage(
        String text,
        String username,
        String channel,
        @JsonProperty("icon_emoji") String iconEmoji
) {
    public static SlackMessage error(String message) {
        return new SlackMessage(
                "🚨 *ERROR ALERT* 🚨\n" + message,
                "BookItty Error Bot",
                null,
                ":rotating_light:"
        );
    }

    public static SlackMessage batch(String message) {
        return new SlackMessage(
                "📊 *BATCH FAILURE* 📊\n" + message,
                "BookItty Batch Bot",
                null,
                ":chart_with_downwards_trend:"
        );
    }
}
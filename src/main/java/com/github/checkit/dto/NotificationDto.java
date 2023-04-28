package com.github.checkit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.checkit.model.Notification;
import com.github.checkit.util.Utils;
import java.net.URI;
import java.time.Instant;
import lombok.Getter;

@Getter
public class NotificationDto {

    private final URI uri;
    private final String title;
    private final String content;
    private final String about;
    private final Instant created;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Instant readAt;

    /**
     * Constructor.
     */
    public NotificationDto(Notification notification, String languageTag) {
        this.uri = notification.getUri();
        this.title = Utils.resolveMultilingual(notification.getTitle(), languageTag);
        this.content = Utils.resolveMultilingual(notification.getContent(), languageTag);
        this.about = notification.getAbout();
        this.created = notification.getCreated();
        this.readAt = notification.getReadAt();
    }
}

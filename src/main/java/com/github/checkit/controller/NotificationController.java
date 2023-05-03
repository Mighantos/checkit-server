package com.github.checkit.controller;

import com.github.checkit.dto.NotificationDto;
import com.github.checkit.service.NotificationService;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(NotificationController.MAPPING)
public class NotificationController extends BaseController {

    public static final String MAPPING = "/notifications";

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationDto> getAllForCurrent(@RequestParam(required = false, defaultValue = "0") int pageNumber,
                                                  @RequestParam String languageTag) {
        return notificationService.getAllForCurrent(pageNumber, languageTag);
    }

    @GetMapping("/unread/count")
    public int getUnreadCountForCurrent() {
        return notificationService.getUnreadCountForCurrent();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/unread/seen")
    public void markAllSeen() {
        notificationService.markAllSeen();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/seen")
    public void markSeen(@RequestBody URI notificationUri) {
        notificationService.markSeen(notificationUri);
    }
}

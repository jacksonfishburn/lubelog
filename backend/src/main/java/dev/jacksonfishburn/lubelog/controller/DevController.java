package dev.jacksonfishburn.lubelog.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.jacksonfishburn.lubelog.service.ReminderService;
import lombok.RequiredArgsConstructor;

@Profile("dev")
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevController {

    private final ReminderService reminderService;

    @PostMapping("/trigger-reminders")
    public ResponseEntity<String> triggerReminders() {
        reminderService.sendDueReminders();
        return ResponseEntity.ok("Reminders triggered");
    }
}

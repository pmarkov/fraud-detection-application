package com.student.fraudsters.logging;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SseLogger implements Logger {

    private final SseEmitter sseEmitter;

    public SseLogger(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void log(String msg) {
        LocalDateTime now = LocalDateTime.now();
        String prefix = dtf.format(now) + " INFO: ";
        try {
            sseEmitter.send(msg);
            System.out.println(prefix + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void debug(String msg) {
        LocalDateTime now = LocalDateTime.now();
        String prefix = dtf.format(now) + " DEBUG: ";
        System.out.println(prefix + msg);
    }

}

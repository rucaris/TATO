package com.tato.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DbDebugConfig {
    private final JdbcTemplate jdbc;

    @EventListener(ApplicationReadyEvent.class)
    public void printDbInfo() {
        String db = jdbc.queryForObject("SELECT DATABASE()", String.class);
        String ver = jdbc.queryForObject("SELECT VERSION()", String.class);
        String user = jdbc.queryForObject("SELECT USER()", String.class);
        log.info("[DB] DATABASE()   = {}", db);
        log.info("[DB] VERSION()    = {}", ver);
        log.info("[DB] USER()       = {}", user);
    }
}

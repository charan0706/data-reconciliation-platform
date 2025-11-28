package com.reconciliation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Application class for Data Reconciliation Platform.
 * Enterprise-grade solution for automated data reconciliation and quality validation.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
public class DataReconciliationApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataReconciliationApplication.class, args);
    }
}


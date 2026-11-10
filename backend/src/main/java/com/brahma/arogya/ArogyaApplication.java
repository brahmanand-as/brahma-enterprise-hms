package com.brahma.arogya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArogyaApplication {

  public static void main(String[] args) {
    java.util.TimeZone.setDefault(
        java.util.TimeZone.getTimeZone(
            java.time.ZoneId.of(System.getenv().getOrDefault("HMS_TIMEZONE", "Asia/Kolkata"))));
    SpringApplication.run(ArogyaApplication.class, args);
  }
}

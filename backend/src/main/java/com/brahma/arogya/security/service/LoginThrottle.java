package com.brahma.arogya.security.service;

import java.util.concurrent.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class LoginThrottle {

  private record Entry(int count, long until) {}

  private final ConcurrentHashMap<String, Entry> attempts = new ConcurrentHashMap<>();

  public void check(String key) {
    long now = System.currentTimeMillis();
    Entry e = attempts.get(key);
    if (e != null && e.until > now && e.count >= 5)
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS,
          "Too many failed sign-in attempts. Try again in 15 minutes.");
    if (attempts.size() > 10000) attempts.entrySet().removeIf(x -> x.getValue().until < now);
  }

  public void failed(String key) {
    long now = System.currentTimeMillis();
    attempts.compute(
        key,
        (k, e) ->
            e == null || e.until < now
                ? new Entry(1, now + 900000)
                : new Entry(e.count + 1, e.until));
  }

  public void clear(String key) {
    attempts.remove(key);
  }
}

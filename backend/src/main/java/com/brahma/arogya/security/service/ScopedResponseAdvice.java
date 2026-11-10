package com.brahma.arogya.security.service;

import java.util.*;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ScopedResponseAdvice implements ResponseBodyAdvice<Object> {

  private final CareScope care;

  public ScopedResponseAdvice(CareScope care) {
    this.care = care;
  }

  public boolean supports(MethodParameter p, Class<? extends HttpMessageConverter<?>> c) {
    return true;
  }

  public Object beforeBodyWrite(
      Object body,
      MethodParameter p,
      MediaType t,
      Class<? extends HttpMessageConverter<?>> c,
      ServerHttpRequest req,
      ServerHttpResponse res) {
    body = normalize(body);
    if (!care.restricted()) return body;
    if (body instanceof List<?> rows) return rows.stream().filter(care::visible).toList();
    if (!care.visible(body))
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Record is outside your assigned care team");
    return body;
  }

  public static Object normalize(Object value) {
    if (value instanceof java.sql.Date d) return d.toLocalDate().toString();
    if (value instanceof java.sql.Time t) return t.toLocalTime().toString();
    if (value instanceof java.sql.Timestamp t) return t.toLocalDateTime().toString();
    if (value instanceof Map<?, ?> map) {
      Map<Object, Object> copy = new LinkedHashMap<>();
      map.forEach((k, v) -> copy.put(k, normalize(v)));
      return copy;
    }
    if (value instanceof List<?> list)
      return list.stream().map(ScopedResponseAdvice::normalize).toList();
    return value;
  }
}

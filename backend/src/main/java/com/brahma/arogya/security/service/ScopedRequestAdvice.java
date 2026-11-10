package com.brahma.arogya.security.service;

import java.lang.reflect.*;
import org.springframework.core.MethodParameter;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@ControllerAdvice
public class ScopedRequestAdvice extends RequestBodyAdviceAdapter {

  private final CareScope care;

  public ScopedRequestAdvice(CareScope care) {
    this.care = care;
  }

  public boolean supports(MethodParameter p, Type t, Class<? extends HttpMessageConverter<?>> c) {
    return true;
  }

  public Object afterBodyRead(
      Object body,
      HttpInputMessage m,
      MethodParameter p,
      Type t,
      Class<? extends HttpMessageConverter<?>> c) {
    if (care.restricted()) {
      for (String field : new String[] {"patientId", "encounterId", "admissionId"})
        try {
          Object value = body.getClass().getMethod(field).invoke(body);
          if (value instanceof Number n) {
            if (field.equals("patientId")) care.require(n.longValue());
            if (field.equals("encounterId")) care.encounter(n.longValue());
            if (field.equals("admissionId")) care.admission(n.longValue());
          }
        } catch (NoSuchMethodException ignored) {
        } catch (ReflectiveOperationException e) {
          throw new IllegalStateException(e);
        }
    }
    return body;
  }
}

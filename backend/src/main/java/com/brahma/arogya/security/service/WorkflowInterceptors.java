package com.brahma.arogya.security.service;

import com.brahma.arogya.security.repository.WorkflowInterceptorsJdbcRepository;
import jakarta.servlet.http.*;
import java.util.regex.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WorkflowInterceptors implements WebMvcConfigurer {

  private final WorkflowInterceptorsJdbcRepository db;

  private final CareScope care;

  public WorkflowInterceptors(WorkflowInterceptorsJdbcRepository db, CareScope care) {
    this.db = db;
    this.care = care;
  }

  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(
            new HandlerInterceptor() {

              public boolean preHandle(
                  HttpServletRequest req, HttpServletResponse res, Object handler) {
                if (care.restricted()) {
                  String path = req.getRequestURI();
                  var own =
                      Pattern.compile(
                              "/api/(?:ward/medications|orders/prescriptions|inpatient/medication-orders)/(\\d+)(?:/.*)?")
                          .matcher(path);
                  if (own.matches()) {
                    long id = Long.parseLong(own.group(1));
                    String table =
                        path.contains("orders/prescriptions")
                            ? "prescriptions"
                            : path.contains("inpatient/medication-orders")
                                ? "medication_orders"
                                : "medication_administrations";
                    if (table.equals("medication_administrations"))
                      care.admission(db.readMedicationAdministrationsForPreHandle(id));
                    else care.require(db.readGeneratedIdForPreHandle(table, id));
                  }
                  var m =
                      Pattern.compile("/api/(?:workflow/)?encounters/(\\d+)(?:/.*)?").matcher(path);
                  if (m.matches()) care.encounter(Long.parseLong(m.group(1)));
                  m = Pattern.compile("/api/(?:ward/)?admissions/(\\d+)(?:/.*)?").matcher(path);
                  if (m.matches()) care.admission(Long.parseLong(m.group(1)));
                }
                return true;
              }

              public void afterCompletion(
                  HttpServletRequest req, HttpServletResponse res, Object handler, Exception e) {
                var a = SecurityContextHolder.getContext().getAuthentication();
                if (a != null
                    && !"anonymousUser".equals(a.getName())
                    && !req.getMethod().equals("GET")
                    && req.getRequestURI().startsWith("/api/")) {
                  try {
                    db.insertAuditLogsForAfterCompletion(
                        a.getName(),
                        req.getMethod(),
                        "HTTP_WORKFLOW",
                        req.getRequestURI(),
                        "HTTP " + res.getStatus());
                  } catch (Exception ignored) {
                    /* Domain ledgers remain transactional; audit outage is separately observable. */
                  }
                }
              }
            })
        .addPathPatterns("/api/**");
  }
}

package com.brahma.arogya.architecture;

import static org.junit.jupiter.api.Assertions.*;

import com.github.javaparser.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import org.junit.jupiter.api.Test;

class ApiCompatibilityTest {
  @Test
  void allExistingUrlsHttpMethodsAndMethodPermissionGuardsArePreserved() throws Exception {
    StaticJavaParser.getParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
    var expected =
        new TreeSet<>(Files.readAllLines(Path.of("src/test/resources/contracts/api-routes.txt")));
    assertEquals(expected, Contracts.routes(Path.of("src/main/java")));
  }

  @Test
  void allAppliedFlywayMigrationsRemainByteIdentical() throws Exception {
    var rows = Files.readAllLines(Path.of("src/test/resources/contracts/migrations.sha256"));
    assertEquals(21, rows.size());
    for (String row : rows) {
      var parts = row.split(" ", 2);
      var bytes = Files.readAllBytes(Path.of("src/main/resources/db/migration", parts[1]));
      assertEquals(
          parts[0],
          HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)),
          parts[1]);
    }
  }

  @Test
  void all526RepositorySqlStatementsArePreserved() throws Exception {
    StaticJavaParser.getParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
    assertEquals(
        Files.readAllLines(Path.of("src/test/resources/contracts/sql.txt")),
        SqlContracts.sql(Path.of("src/main/java")));
  }

  @Test
  void urlPermissionRulesAreUnchanged() throws Exception {
    StaticJavaParser.getParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
    assertEquals(
        Files.readString(Path.of("src/test/resources/contracts/access-policy.txt")),
        StaticJavaParser.parse(
                Path.of("src/main/java/com/brahma/arogya/security/service/AccessPolicy.java"))
            .getType(0)
            .toString());
  }
}

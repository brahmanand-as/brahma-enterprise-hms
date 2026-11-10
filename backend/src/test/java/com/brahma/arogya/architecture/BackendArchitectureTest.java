package com.brahma.arogya.architecture;

import static org.junit.jupiter.api.Assertions.*;

import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class BackendArchitectureTest {
  private static final Path ROOT = Path.of("src/main/java/com/brahma/arogya");

  private List<Path> files() throws Exception {
    try (var paths = Files.walk(ROOT)) {
      return paths.filter(p -> p.toString().endsWith(".java")).toList();
    }
  }

  private CompilationUnit parse(Path p) throws Exception {
    return new JavaParser(
            new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21))
        .parse(p)
        .getResult()
        .orElseThrow();
  }

  @Test
  void rootPackageContainsOnlyApplicationEntryPoint() throws Exception {
    try (var paths = Files.list(ROOT)) {
      assertEquals(
          List.of("ArogyaApplication.java"),
          paths
              .filter(p -> p.toString().endsWith(".java"))
              .map(p -> p.getFileName().toString())
              .sorted()
              .toList());
    }
  }

  @Test
  void controllersHaveOnlyServiceDependenciesAndNoPersistenceOrSql() throws Exception {
    int controllers = 0;
    for (var p : files())
      if (p.getParent().getFileName().toString().equals("controller")) {
        var c = parse(p);
        controllers++;
        var cl = c.getType(0).asClassOrInterfaceDeclaration();
        for (var f : cl.getFields()) {
          assertTrue(f.isFinal(), p.toString());
          assertTrue(f.getElementType().asString().endsWith("Service"), p + " depends on " + f);
        }
        assertFalse(c.toString().contains("JdbcTemplate"), p.toString());
        assertTrue(
            cl.getMethods().stream()
                .allMatch(
                    m -> m.getBody().isPresent() && m.getBody().get().getStatements().size() == 1),
            p.toString());
        assertFalse(
            c.getImports().stream()
                .anyMatch(
                    i ->
                        i.getNameAsString().contains(".repository.")
                            || i.getNameAsString().contains(".entity.")),
            p.toString());
      }
    assertEquals(44, controllers);
  }

  @Test
  void sqlAndJdbcTemplateAreConfinedToRepositories() throws Exception {
    for (var p : files())
      if (!p.getParent().getFileName().toString().equals("repository")) {
        var c = parse(p);
        assertFalse(c.toString().contains("JdbcTemplate"), p.toString());
        for (var s : c.findAll(StringLiteralExpr.class))
          assertFalse(
              s.asString()
                  .matches(
                      "(?s).*\\b(SELECT\\s+.+\\s+FROM|INSERT\\s+INTO|UPDATE\\s+\\w+\\s+SET|DELETE\\s+FROM)\\b.*"),
              p + ": " + s);
      }
  }

  @Test
  void businessServicesNeverDependOnControllers() throws Exception {
    for (var p : files())
      if (p.getParent().getFileName().toString().equals("service")) {
        var c = parse(p);
        assertFalse(
            c.getImports().stream().anyMatch(i -> i.getNameAsString().contains(".controller.")),
            p.toString());
      }
  }

  @Test
  void noFieldInjectionRemains() throws Exception {
    for (var p : files())
      for (var f : parse(p).findAll(FieldDeclaration.class))
        assertTrue(
            f.getAnnotations().stream().noneMatch(a -> a.getNameAsString().endsWith("Autowired")),
            p.toString());
  }

  @Test
  void everyRequestBodyUsesValidatedDto() throws Exception {
    for (var p : files())
      if (p.getParent().getFileName().toString().equals("controller"))
        for (var param : parse(p).findAll(Parameter.class))
          if (param.getAnnotations().stream()
              .anyMatch(a -> a.getNameAsString().equals("RequestBody"))) {
            assertTrue(
                param.getAnnotations().stream()
                    .anyMatch(a -> a.getNameAsString().endsWith("Valid")),
                p.toString());
            assertTrue(param.getTypeAsString().endsWith("Request"), p + ": " + param);
          }
  }

  @Test
  void jdbcOperationsExposeConcreteSignaturesInsteadOfAnUnboundedSqlAdapter() throws Exception {
    for (var path : files())
      if (path.toString().endsWith("JdbcRepository.java")) {
        for (var method : parse(path).getType(0).getMethods()) {
          assertTrue(method.getTypeParameters().isEmpty(), path + ": " + method.getName());
          assertTrue(
              method.getParameters().stream().noneMatch(Parameter::isVarArgs),
              path + ": " + method.getName());
        }
      }
  }
}

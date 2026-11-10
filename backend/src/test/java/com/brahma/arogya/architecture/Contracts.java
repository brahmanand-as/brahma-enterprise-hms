package com.brahma.arogya.architecture;

import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import java.nio.file.*;
import java.util.*;

public class Contracts {
  static List<String> paths(com.github.javaparser.ast.expr.AnnotationExpr a) {
    if (a.isMarkerAnnotationExpr()) return List.of("");
    Expression v =
        a.isSingleMemberAnnotationExpr()
            ? a.asSingleMemberAnnotationExpr().getMemberValue()
            : a.asNormalAnnotationExpr().getPairs().stream()
                .filter(
                    p -> p.getNameAsString().equals("value") || p.getNameAsString().equals("path"))
                .map(p -> p.getValue())
                .findFirst()
                .orElse(new StringLiteralExpr(""));
    return v.findAll(StringLiteralExpr.class).stream().map(StringLiteralExpr::asString).toList();
  }

  public static Set<String> routes(Path root) throws Exception {
    Set<String> rows = new TreeSet<>();
    for (var p : Files.walk(root).filter(p -> p.toString().endsWith(".java")).toList()) {
      var c = StaticJavaParser.parse(p);
      var t = c.getType(0);
      if (t.getAnnotations().stream().noneMatch(a -> a.getNameAsString().equals("RestController")))
        continue;
      var base =
          t.getAnnotations().stream()
              .filter(a -> a.getNameAsString().equals("RequestMapping"))
              .findFirst();
      var prefixes = base.isPresent() ? paths(base.get()) : List.of("");
      for (var m : t.getMethods())
        for (var a : m.getAnnotations())
          if (a.getNameAsString().matches("(Get|Post|Put|Patch|Delete|Request)Mapping")) {
            for (String b : prefixes)
              for (String route : paths(a)) {
                String security =
                    m.getAnnotations().stream()
                        .filter(x -> x.getNameAsString().endsWith("PreAuthorize"))
                        .map(Object::toString)
                        .reduce("", (x, y) -> x + y);
                rows.add(
                    a.getNameAsString().replace("Mapping", "").toUpperCase()
                        + " "
                        + b
                        + route
                        + " "
                        + security);
              }
          }
    }
    return rows;
  }

  public static void main(String[] a) throws Exception {
    StaticJavaParser.getParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
    Files.write(Path.of(a[1]), routes(Path.of(a[0])));
  }
}

package com.brahma.arogya.architecture;

import com.github.javaparser.*;
import com.github.javaparser.ast.expr.*;
import java.nio.file.*;
import java.util.*;

class SqlContracts {
  public static List<String> sql(Path root) throws Exception {
    List<String> rows = new ArrayList<>();
    for (var p : Files.walk(root).filter(p -> p.toString().endsWith(".java")).toList())
      for (var call : StaticJavaParser.parse(p).findAll(MethodCallExpr.class))
        if (!call.getArguments().isEmpty()
            && (call.getScope().map(Object::toString).orElse("").matches("db|jdbc")
                || call.getNameAsString().equals("row"))) {
          var arg = call.getArgument(0);
          String text =
              arg.findAll(StringLiteralExpr.class).stream()
                  .map(StringLiteralExpr::asString)
                  .reduce("", String::concat);
          if (text.matches("(?s)^(SELECT |INSERT |UPDATE |DELETE ).*")) rows.add(text);
        }
    Collections.sort(rows);
    return rows;
  }

  public static void main(String[] a) throws Exception {
    StaticJavaParser.getParserConfiguration()
        .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
    Files.write(Path.of(a[1]), sql(Path.of(a[0])));
  }
}

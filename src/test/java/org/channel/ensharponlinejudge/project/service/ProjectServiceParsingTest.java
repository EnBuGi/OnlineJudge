package org.channel.ensharponlinejudge.project.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;

class ProjectServiceParsingTest {

  @Test
  @DisplayName("테스트 메서드 파싱시 () 접미사가 추가되는지 검증")
  void testMethodNameParsingWithParentheses() {
    // Given
    String code =
        "import org.junit.jupiter.api.Test;\n"
            + "class MyTest {\n"
            + "    @Test\n"
            + "    void myTestMethod() {}\n"
            + "    \n"
            + "    void regularMethod() {}\n"
            + "}";
    CompilationUnit cu = StaticJavaParser.parse(code);
    List<String> methodNames = new ArrayList<>();

    // When (ProjectService.java 의 로직 모사)
    cu.findAll(MethodDeclaration.class)
        .forEach(
            method -> {
              boolean isTest =
                  method.getAnnotations().stream()
                      .anyMatch(
                          ann -> {
                            String name = ann.getNameAsString();
                            return name.equals("Test") || name.equals("ParameterizedTest");
                          });
              if (isTest) {
                methodNames.add(method.getNameAsString() + "()");
              }
            });

    // Then
    assertThat(methodNames).containsExactly("myTestMethod()");
  }
}

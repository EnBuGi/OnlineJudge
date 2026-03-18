package org.channel.ensharponlinejudge.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfiguration {

  @Value("${security.paths-to-permit.get:}")
  private String[] getPermitUrls;

  @Value("${security.paths-to-permit.post:}")
  private String[] postPermitUrls;

  @Bean
  public OpenAPI openAPI() {
    final String securitySchemeName = "bearerAuth";
    SecurityScheme securityScheme =
        new SecurityScheme()
            .name(securitySchemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");

    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme))
        .info(
            new Info()
                .title("🎯 EnSharp Online Judge API")
                .description(
                    """
                        <div style='font-size: 14px;'>
                          <h3>💡 온라인 저지 시스템 API 문서</h3>
                          <p>코딩 문제를 풀고, 제출하고, 채점하는 온라인 저지 플랫폼의 RESTful API입니다.</p>
                          <ul>
                            <li>📝 문제 관리 및 조회</li>
                            <li>✅ 코드 제출 및 채점</li>
                            <li>👥 사용자 인증 및 관리</li>
                            <li>📊 통계 및 랭킹</li>
                          </ul>
                          <p><strong>개발 팀:</strong> EnSharp Development Team</p>
                        </div>
                        """)
                .version("v1.0.0")
                .contact(new Contact().name("En#")));
  }

  @Bean
  public OpenApiCustomizer securityOpenApiCustomizer() {
    Set<String> getPermitSet = new HashSet<>(Arrays.asList(getPermitUrls));
    Set<String> postPermitSet = new HashSet<>(Arrays.asList(postPermitUrls));

    return openApi ->
        openApi
            .getPaths()
            .forEach(
                (path, pathItem) -> {
                  // "get" 허용 경로 (GET 메서드만)
                  if (pathItem.getGet() != null && getPermitSet.contains(path)) {
                    pathItem.getGet().setSecurity(new ArrayList<>());
                  }
                  // "post" 허용 경로 (POST 메서드만)
                  if (pathItem.getPost() != null && postPermitSet.contains(path)) {
                    pathItem.getPost().setSecurity(new ArrayList<>());
                  }
                });
  }
}

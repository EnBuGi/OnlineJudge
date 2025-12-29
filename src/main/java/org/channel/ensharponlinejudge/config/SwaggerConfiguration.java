package org.channel.ensharponlinejudge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfiguration {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
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
}

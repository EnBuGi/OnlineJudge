package org.channel.ensharponlinejudge.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestRedisConfig.class)
public abstract class ApiTestBase {

  @LocalServerPort private int port;

  @Autowired private DatabaseCleaner databaseCleaner;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    databaseCleaner.execute();
  }
}

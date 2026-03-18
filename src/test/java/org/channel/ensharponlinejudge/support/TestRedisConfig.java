package org.channel.ensharponlinejudge.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestRedisConfig {

  public static final GenericContainer<?> REDIS_CONTAINER =
      new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine")).withExposedPorts(6379);

  static {
    REDIS_CONTAINER.start();
    System.setProperty("spring.data.redis.host", REDIS_CONTAINER.getHost());
    System.setProperty(
        "spring.data.redis.port", String.valueOf(REDIS_CONTAINER.getMappedPort(6379)));
  }
}

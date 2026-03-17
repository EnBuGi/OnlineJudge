package org.channel.ensharponlinejudge.support;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@TestConfiguration
public class TestRedisConfig {

  private final Map<String, String> redisData = new HashMap<>();
  private final Map<String, Long> expirationData = new HashMap<>();

  @Bean
  @Primary
  public RedisConnectionFactory redisConnectionFactory() {
    RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
    RedisConnection connection = mock(RedisConnection.class);
    RedisServerCommands serverCommands = mock(RedisServerCommands.class);

    when(factory.getConnection()).thenReturn(connection);
    when(connection.serverCommands()).thenReturn(serverCommands);

    doAnswer(
            invocation -> {
              redisData.clear();
              expirationData.clear();
              return null;
            })
        .when(serverCommands)
        .flushAll();

    return factory;
  }

  @Bean
  @Primary
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
    StringRedisTemplate template = mock(StringRedisTemplate.class);
    ValueOperations<String, String> valueOperations = mock(ValueOperations.class);

    when(template.opsForValue()).thenReturn(valueOperations);
    when(template.getConnectionFactory()).thenReturn(connectionFactory);

    // Mock set with expiration
    doAnswer(
            invocation -> {
              String key = invocation.getArgument(0);
              String value = invocation.getArgument(1);
              long timeout = invocation.getArgument(2);
              TimeUnit unit = invocation.getArgument(3);
              redisData.put(key, value);
              expirationData.put(key, System.currentTimeMillis() + unit.toMillis(timeout));
              return null;
            })
        .when(valueOperations)
        .set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

    // Mock get with expiration check
    when(valueOperations.get(anyString()))
        .thenAnswer(
            invocation -> {
              String key = invocation.getArgument(0);
              Long expiration = expirationData.get(key);
              if (expiration != null && System.currentTimeMillis() > expiration) {
                redisData.remove(key);
                expirationData.remove(key);
                return null;
              }
              return redisData.get(key);
            });

    // Mock delete
    when(template.delete(anyString()))
        .thenAnswer(
            invocation -> {
              String key = invocation.getArgument(0);
              expirationData.remove(key);
              return redisData.remove(key) != null;
            });

    return template;
  }
}

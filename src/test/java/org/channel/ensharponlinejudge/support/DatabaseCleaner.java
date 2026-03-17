package org.channel.ensharponlinejudge.support;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseCleaner {

  @PersistenceContext private EntityManager entityManager;

  @Autowired(required = false)
  private RedisTemplate<String, Object> redisTemplate;

  private List<String> tableNames;

  @PostConstruct
  public void findTableNames() {
    tableNames =
        entityManager.getMetamodel().getEntities().stream()
            .filter(entity -> entity.getJavaType().getAnnotation(Entity.class) != null)
            .map(
                entity -> {
                  Table table = entity.getJavaType().getAnnotation(Table.class);
                  if (table != null && !table.name().isEmpty()) {
                    return table.name();
                  }
                  return convertToSnakeCase(entity.getName());
                })
            .collect(Collectors.toList());
  }

  private String convertToSnakeCase(String name) {
    StringBuilder result = new StringBuilder();
    for (char c : name.toCharArray()) {
      if (Character.isUpperCase(c)) {
        if (result.length() > 0) {
          result.append("_");
        }
        result.append(Character.toLowerCase(c));
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  @Transactional
  public void execute() {
    entityManager.flush();
    entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();

    for (String tableName : tableNames) {
      entityManager.createNativeQuery("TRUNCATE TABLE " + tableName).executeUpdate();
    }

    entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();

    if (redisTemplate != null) {
      redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }
  }
}

package org.channel.ensharponlinejudge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling
@EnableTransactionManagement
@SpringBootApplication
public class EnSharpOnlineJudgeApplication {

  public static void main(String[] args) {
    SpringApplication.run(EnSharpOnlineJudgeApplication.class, args);
  }
}

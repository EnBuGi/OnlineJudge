package org.channel.ensharponlinejudge.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "oci.object-storage")
public class OciProperties {
  private String tenantId;
  private String userId;
  private String fingerprint;
  private String privateKeyPath;
  private String region;
  private String namespace;
  private String testCodeBucketName;
  private int parExpirationHours = 1;
}

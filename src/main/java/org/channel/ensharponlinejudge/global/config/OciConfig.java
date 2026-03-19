package org.channel.ensharponlinejudge.global.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OciProperties.class)
public class OciConfig {

  private final OciProperties ociProperties;

  @Bean
  public ObjectStorageClient objectStorageClient() throws IOException {
    byte[] privateKeyBytes = Files.readAllBytes(Paths.get(ociProperties.getPrivateKeyPath()));

    SimpleAuthenticationDetailsProvider provider =
        SimpleAuthenticationDetailsProvider.builder()
            .tenantId(ociProperties.getTenantId())
            .userId(ociProperties.getUserId())
            .fingerprint(ociProperties.getFingerprint())
            .privateKeySupplier(() -> new java.io.ByteArrayInputStream(privateKeyBytes))
            .region(Region.fromRegionId(ociProperties.getRegion()))
            .build();

    return ObjectStorageClient.builder().build(provider);
  }
}

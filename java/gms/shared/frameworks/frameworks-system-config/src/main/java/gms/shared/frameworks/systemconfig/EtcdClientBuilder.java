package gms.shared.frameworks.systemconfig;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class EtcdClientBuilder {

  private final ClientBuilder clientBuilder;

  public EtcdClientBuilder(String endpoints, String user, String pw) {
    this.clientBuilder = Client.builder()
      .endpoints(endpoints)
      .retryDelay(50)
      .retryMaxDelay(1000)
      .retryChronoUnit(ChronoUnit.MILLIS)
      .retryMaxDuration(Duration.parse("PT10M"))
      .keepaliveTime(Duration.ofNanos(Long.MAX_VALUE));

    if (null != user && null != pw) {
      clientBuilder.user(ByteSequence.from(user, StandardCharsets.US_ASCII))
        .password(ByteSequence.from(pw, StandardCharsets.US_ASCII));
    }
  }

  public ClientBuilder clientBuilder() {
    return clientBuilder;
  }

  /**
   * Builds a {@link java.io.Closeable} etcd Client. This client should always be closed, as Keep-Alive functionality
   * is disabled due to a known issue with netty epoll.
   *
   * @return
   */
  public Client buildClient() {
    return clientBuilder.build();
  }


}

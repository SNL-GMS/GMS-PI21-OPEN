package gms.shared.frameworks.systemconfig;

import com.google.protobuf.ByteString;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.api.KeyValue;
import io.etcd.jetcd.api.RangeResponse;
import io.etcd.jetcd.kv.GetResponse;
import net.jodah.failsafe.RetryPolicy;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EtcdSystemConfigRepositoryTest {

  @Test
  void testBuilder() {

    var configRepo = EtcdSystemConfigRepository.builder()
      .setEndpoints("http://testEndpoint")
      .setCredentials("testUser", "testPW")
      .build();

    assertThat(configRepo).isNotNull();
  }


  @Test
  void testGet() {
    EtcdClientBuilder mockBuilder = mock(EtcdClientBuilder.class);
    Client mockClient = mock(Client.class);
    KV mockKV = mock(KV.class);

    var configRepo = new EtcdSystemConfigRepository(mockBuilder, failFast());

    var inputKey = "foo";
    var expectedValue = "bar";
    var keyValue = KeyValue.newBuilder()
      .setKey(ByteString.copyFrom(inputKey, StandardCharsets.US_ASCII))
      .setValue(ByteString.copyFrom(expectedValue, StandardCharsets.US_ASCII))
      .build();

    var response = new GetResponse(RangeResponse.newBuilder().addKvs(keyValue).build(), ByteSequence.EMPTY);

    given(mockBuilder.buildClient()).willReturn(mockClient);
    given(mockClient.getKVClient()).willReturn(mockKV);
    given(mockKV.get(ByteSequence.from(inputKey, StandardCharsets.UTF_8))).willReturn(
      CompletableFuture.completedFuture(response));

    assertThat(configRepo.get(inputKey)).contains(expectedValue);
    verify(mockClient).close();
  }

  private RetryPolicy<Optional<String>> failFast() {
    return new RetryPolicy<Optional<String>>()
      .handle(ExecutionException.class, TimeoutException.class)
      .withMaxAttempts(1);
  }

}
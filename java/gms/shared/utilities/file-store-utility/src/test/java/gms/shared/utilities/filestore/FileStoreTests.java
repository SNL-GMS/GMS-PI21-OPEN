package gms.shared.utilities.filestore;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.utilities.filestore.FileStoreTestFixture.DeserializedTestClass1;
import gms.shared.utilities.filestore.FileStoreTestFixture.DeserializedTestClass2;
import gms.shared.utilities.filestore.FileStoreTestFixture.TestFileTransformer1;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class FileStoreTests {

  @Mock
  private MinioClient minioClient;

  @ParameterizedTest
  @MethodSource("findByFileDescriptor_objectMapper_TestSource")
  void testFindByFileDescriptor_objectMapper(
    FileDescriptor fileDescriptor,
    String serializedMockObject,
    DeserializedTestClass1 expected
  ) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    Mockito.when(minioClient.getObject(
      GetObjectArgs.builder()
        .bucket(fileDescriptor.getBucket())
        .object(fileDescriptor.getKey())
        .build()
    )).thenReturn(new GetObjectResponse(
      null,
      fileDescriptor.getBucket(),
      null,
      fileDescriptor.getKey(),
      new ByteArrayInputStream(serializedMockObject.getBytes())
    ));

    var fileStore = new FileStore(minioClient);

    var actual = fileStore.findByFileDescriptor(fileDescriptor, DeserializedTestClass1.class);

    Assertions.assertEquals(expected, actual);
  }

  private static Stream<Arguments> findByFileDescriptor_objectMapper_TestSource() throws JsonProcessingException {
    var objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    return Stream.of(
      Arguments.arguments(
        FileDescriptor.create("my-bucket", "my-key"),
        objectMapper.writeValueAsString(new DeserializedTestClass1(
          "myString",
          1.123,
          Map.of(
            "key1", 1,
            "key2", 2
          )
        )),
        new DeserializedTestClass1(
          "myString",
          1.123,
          Map.of(
            "key1", 1,
            "key2", 2
          )
        )
      )
    );
  }

  @ParameterizedTest
  @MethodSource("findByFileDescriptor_fileTransformer_TestSource")
  void testFindByFileDescriptor_fileTransformer(
    FileDescriptor fileDescriptor,
    FileTransformer<DeserializedTestClass2> fileTransformer,
    String serializedMockObject,
    DeserializedTestClass2 expected
  ) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    Mockito.when(minioClient.getObject(
      GetObjectArgs.builder()
        .bucket(fileDescriptor.getBucket())
        .object(fileDescriptor.getKey())
        .build()
    )).thenReturn(new GetObjectResponse(
      null,
      fileDescriptor.getBucket(),
      null,
      fileDescriptor.getKey(),
      new ByteArrayInputStream(serializedMockObject.getBytes())
    ));

    var fileStore = new FileStore(minioClient);

    var actual = fileStore.findByFileDescriptor(fileDescriptor, fileTransformer);

    Assertions.assertEquals(expected, actual);
  }

  private static Stream<Arguments> findByFileDescriptor_fileTransformer_TestSource()
    throws JsonProcessingException {
    var objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    return Stream.of(
      Arguments.arguments(
        FileDescriptor.create("my-bucket", "my-key"),
        new TestFileTransformer1(),
        objectMapper.writeValueAsString(new DeserializedTestClass1(
          "myString",
          1.123,
          Map.of(
            "integer1", 1,
            "integer2", 2
          )
        )),
        new DeserializedTestClass2(
          "myString",
          1.123,
          1,
          2
        )
      )
    );
  }

  @ParameterizedTest
  @MethodSource("findByKeyPrefix_objectMapper_TestSource")
  void testFindByKeyPrefix_objectMapper(
    FileDescriptor fileDescriptor,
    Map<String, String> serializedMockObjectMap,
    Map<FileDescriptor, DeserializedTestClass1> expectedMap
  )
    throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    var prefix = fileDescriptor.getKey();
    if (!prefix.endsWith("/")) {
      prefix = prefix + "/";
    }

    var mockItems = generateMockItems(serializedMockObjectMap.keySet()).stream()
      .map(Result::new)
      .collect(Collectors.toSet());

    Mockito.when(minioClient.listObjects(
      ListObjectsArgs.builder()
        .bucket(fileDescriptor.getBucket())
        .prefix(prefix)
        .build()
    )).thenAnswer(invocation -> {
      ListObjectsArgs args = invocation.getArgument(0);

      Assertions.assertEquals(fileDescriptor.getBucket(), args.bucket());

      return mockItems;
    });

    Mockito.when(minioClient.getObject(
      any(GetObjectArgs.class)
    )).thenAnswer(invocation -> {

      GetObjectArgs args = invocation.getArgument(0);

      Assertions.assertEquals(fileDescriptor.getBucket(), args.bucket());
      Assertions.assertTrue(serializedMockObjectMap.keySet().contains(args.object()));

      return new GetObjectResponse(
        null,
        fileDescriptor.getBucket(),
        null,
        fileDescriptor.getKey(),
        new ByteArrayInputStream(serializedMockObjectMap.get(args.object()).getBytes())
      );

    });

    var fileStore = new FileStore(minioClient);

    var actualMap = fileStore.findByKeyPrefix(fileDescriptor, DeserializedTestClass1.class);

    Assertions.assertEquals(expectedMap, actualMap);

  }

  private static Stream<Arguments> findByKeyPrefix_objectMapper_TestSource()
    throws JsonProcessingException {

    var objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    return Stream.of(
      Arguments.arguments(
        FileDescriptor.create(
          "my-bucket",
          "my-general-key/"
        ),
        Map.of(
          "my-general-key/my-specific-key1",
          objectMapper.writeValueAsString(new DeserializedTestClass1(
            "myString",
            1.123,
            Map.of(
              "integer1", 1,
              "integer2", 2
            )
          )),
          "my-general-key/my-specific-key2",
          objectMapper.writeValueAsString(new DeserializedTestClass1(
            "myOtherString",
            1.123,
            Map.of(
              "integer1", 2,
              "integer2", 4
            )
          ))
        ),
        Map.of(
          FileDescriptor.create(
            "my-bucket",
            "my-general-key/my-specific-key1"
          ),
          new DeserializedTestClass1(
            "myString",
            1.123,
            Map.of(
              "integer1", 1,
              "integer2", 2
            )
          ),
          FileDescriptor.create(
            "my-bucket",
            "my-general-key/my-specific-key2"
          ),
          new DeserializedTestClass1(
            "myOtherString",
            1.123,
            Map.of(
              "integer1", 2,
              "integer2", 4
            )
          )
        )
      ),

      Arguments.arguments(
        FileDescriptor.create(
          "my-bucket",
          "my-general-key"
        ),
        Map.of(
          "my-general-key/my-specific-key1",
          objectMapper.writeValueAsString(new DeserializedTestClass1(
            "myString",
            1.123,
            Map.of(
              "integer1", 1,
              "integer2", 2
            )
          )),
          "my-general-key/my-specific-key2",
          objectMapper.writeValueAsString(new DeserializedTestClass1(
            "myOtherString",
            1.123,
            Map.of(
              "integer1", 2,
              "integer2", 4
            )
          ))
        ),
        Map.of(
          FileDescriptor.create(
            "my-bucket",
            "my-general-key/my-specific-key1"
          ),
          new DeserializedTestClass1(
            "myString",
            1.123,
            Map.of(
              "integer1", 1,
              "integer2", 2
            )
          ),
          FileDescriptor.create(
            "my-bucket",
            "my-general-key/my-specific-key2"
          ),
          new DeserializedTestClass1(
            "myOtherString",
            1.123,
            Map.of(
              "integer1", 2,
              "integer2", 4
            )
          )
        )
      )
    );
  }

  @ParameterizedTest
  @MethodSource("findByKeyPrefix_fileTransformer_TestSource")
  void testFindByKeyPrefix_fileTransformer(
    FileDescriptor fileDescriptor,
    FileTransformer<DeserializedTestClass2> fileTransformer,
    Map<String, String> serializedMockObjectMap,
    Map<FileDescriptor, DeserializedTestClass2> expectedMap
  )
    throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    var prefix = fileDescriptor.getKey();
    if (!prefix.endsWith("/")) {
      prefix = prefix + "/";
    }

    var mockItems = generateMockItems(serializedMockObjectMap.keySet()).stream()
      .map(Result::new)
      .collect(Collectors.toSet());

    Mockito.when(minioClient.listObjects(
      ListObjectsArgs.builder()
        .bucket(fileDescriptor.getBucket())
        .prefix(prefix)
        .build()
    )).thenAnswer(invocation -> {
      ListObjectsArgs args = invocation.getArgument(0);

      Assertions.assertEquals(fileDescriptor.getBucket(), args.bucket());

      return mockItems;
    });

    Mockito.when(minioClient.getObject(
      any(GetObjectArgs.class)
    )).thenAnswer(invocation -> {

      GetObjectArgs args = invocation.getArgument(0);

      Assertions.assertEquals(fileDescriptor.getBucket(), args.bucket());
      Assertions.assertTrue(serializedMockObjectMap.keySet().contains(args.object()));

      return new GetObjectResponse(
        null,
        fileDescriptor.getBucket(),
        null,
        fileDescriptor.getKey(),
        new ByteArrayInputStream(serializedMockObjectMap.get(args.object()).getBytes())
      );

    });

    var fileStore = new FileStore(minioClient);

    var actualMap = fileStore.findByKeyPrefix(fileDescriptor, fileTransformer);

    Assertions.assertEquals(expectedMap, actualMap);

  }

  private static Stream<Arguments> findByKeyPrefix_fileTransformer_TestSource()
    throws JsonProcessingException {

    var objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    return Stream.of(
      Arguments.arguments(
        FileDescriptor.create(
          "my-bucket",
          "my-general-key/"
        ),
        new TestFileTransformer1(),
        Map.of(
          "my-general-key/my-specific-key1",
          objectMapper.writeValueAsString(new DeserializedTestClass1(
            "myString",
            1.123,
            Map.of(
              "integer1", 1,
              "integer2", 2
            )
          )),
          "my-general-key/my-specific-key2",
          objectMapper.writeValueAsString(new DeserializedTestClass1(
            "myOtherString",
            1.123,
            Map.of(
              "integer1", 2,
              "integer2", 4
            )
          ))
        ),
        Map.of(
          FileDescriptor.create(
            "my-bucket",
            "my-general-key/my-specific-key1"
          ),
          new DeserializedTestClass2(
            "myString",
            1.123,
            1,
            2
          ),
          FileDescriptor.create(
            "my-bucket",
            "my-general-key/my-specific-key2"
          ),
          new DeserializedTestClass2(
            "myOtherString",
            1.123,
            2,
            4
          )
        )
      ),

      Arguments.arguments(
        FileDescriptor.create(
          "my-bucket",
          "my-general-key"
        ),
        new TestFileTransformer1(),
        Map.of(
          "my-general-key/my-specific-key1",
          objectMapper.writeValueAsString(new DeserializedTestClass1(
            "myString",
            1.123,
            Map.of(
              "integer1", 1,
              "integer2", 2
            )
          )),
          "my-general-key/my-specific-key2",
          objectMapper.writeValueAsString(new DeserializedTestClass1(
            "myOtherString",
            1.123,
            Map.of(
              "integer1", 2,
              "integer2", 4
            )
          ))
        ),
        Map.of(
          FileDescriptor.create(
            "my-bucket",
            "my-general-key/my-specific-key1"
          ),
          new DeserializedTestClass2(
            "myString",
            1.123,
            1,
            2
          ),
          FileDescriptor.create(
            "my-bucket",
            "my-general-key/my-specific-key2"
          ),
          new DeserializedTestClass2(
            "myOtherString",
            1.123,
            2,
            4
          )
        )
      )
    );
  }

  private static Set<Item> generateMockItems(Set<String> objectKeys) {

    return objectKeys.stream()
      .map(objectKey -> {

        var item = Mockito.mock(Item.class);
        Mockito.when(item.objectName()).thenReturn(objectKey);
        return item;
      }).collect(Collectors.toSet());
  }

  @Test
  void testConstructorValidation() {
    var exception = Assertions.assertThrows(
      NullPointerException.class,
      () -> new FileStore(null)
    );

    Assertions.assertEquals("minioClient must not be null!", exception.getMessage());
  }

  @Test
  void testFindByFileDescriptorValidation() {

    var dummyFileDescriptor = FileDescriptor.create("dummy", "dummy");

    var fileStore = new FileStore(minioClient);

    var exception = Assertions.assertThrows(
      NullPointerException.class,
      () -> fileStore.findByFileDescriptor(null, String.class)
    );

    Assertions.assertEquals("fileDescriptor must not be null!", exception.getMessage());

    exception = Assertions.assertThrows(
      NullPointerException.class,
      () -> fileStore.findByFileDescriptor(null, new TestFileTransformer1())
    );

    Assertions.assertEquals("fileDescriptor must not be null!", exception.getMessage());


    exception = Assertions.assertThrows(
      NullPointerException.class,
      () -> fileStore.findByFileDescriptor(dummyFileDescriptor, (Class<Object>) null)
    );

    Assertions.assertEquals("type must not be null!", exception.getMessage());

    exception = Assertions.assertThrows(
      NullPointerException.class,
      () -> fileStore.findByFileDescriptor(dummyFileDescriptor, (FileTransformer<Object>) null)
    );

    Assertions.assertEquals("fileTransformer must not be null!", exception.getMessage());
  }


  @Test
  void testFindByKeyPrefixValidation() {

    var dummyFileDescriptor = FileDescriptor.create("dummy", "dummy");

    var fileStore = new FileStore(minioClient);

    var exception = Assertions.assertThrows(
      NullPointerException.class,
      () -> fileStore.findByKeyPrefix(null, String.class)
    );

    Assertions.assertEquals("fileDescriptor must not be null!", exception.getMessage());

    exception = Assertions.assertThrows(
      NullPointerException.class,
      () -> fileStore.findByKeyPrefix(null, new TestFileTransformer1())
    );

    Assertions.assertEquals("fileDescriptor must not be null!", exception.getMessage());

    exception = Assertions.assertThrows(
      NullPointerException.class,
      () -> fileStore.findByKeyPrefix(dummyFileDescriptor, (Class<Object>) null)
    );

    Assertions.assertEquals("type must not be null!", exception.getMessage());

    exception = Assertions.assertThrows(
      NullPointerException.class,
      () -> fileStore.findByKeyPrefix(dummyFileDescriptor, (FileTransformer<Object>) null)
    );

    Assertions.assertEquals("fileTransformer must not be null!", exception.getMessage());
  }

  @Test
  void testExceptionFromFindByFileDescriptorWrapped()
    throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    Mockito.when(minioClient.getObject(
      any()
    )).thenThrow(ServerException.class);

    var fileStore = new FileStore(minioClient);

    var fileDescriptor = FileDescriptor.create(
      "my-bucket",
      "my-general-key"
    );

    Assertions.assertThrows(
      FileStoreRuntimeException.class,
      () -> fileStore.findByFileDescriptor(fileDescriptor, new TestFileTransformer1())
    );
  }

  @Test
  void testExceptionFromFindByKeyPrefixWrapped()
    throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

    var problematicResult = (Result<Item>) Mockito.mock(Result.class);

    Mockito.when(problematicResult.get()).thenThrow(ServerException.class);

    Mockito.when(minioClient.listObjects(
      any()
    )).thenReturn(Set.of(problematicResult));

    var fileStore = new FileStore(minioClient);

    var fileDescriptor = FileDescriptor.create(
      "my-bucket",
      "my-general-key"
    );

    var dummyTransformer = new TestFileTransformer1();

    Assertions.assertThrows(
      FileStoreRuntimeException.class,
      () -> fileStore.findByKeyPrefix(fileDescriptor, dummyTransformer)
    );
  }
}

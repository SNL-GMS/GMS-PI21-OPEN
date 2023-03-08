package gms.shared.utilities.filestore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility for interacting with Minio.
 */
public class FileStore {

  //
  // Common validation error messages
  //
  private static final String NULL_FILE_DESCRIPTOR_MESSAGE = "fileDescriptor must not be null!";
  private static final String NULL_TYPE_MESSAGE = "type must not be null!";
  private static final String NULL_FILE_TRANSFORMER_MESSAGE = "fileTransformer must not be null!";

  private final MinioClient minioClient;
  private final ObjectMapper objectMapper;

  /**
   * Construct an instance of FileStore
   *
   * @param minioClient - the Minio client to use.
   */
  public FileStore(MinioClient minioClient) {
    Objects.requireNonNull(minioClient, "minioClient must not be null!");

    this.minioClient = minioClient;
    this.objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  }

  /**
   * Get a file given a FileDescriptor and type to deserialize to. The file is loaded from Minio
   * and deserialized to the type.
   *
   * @param fileDescriptor Describes the file to laod
   * @param type Type to deserialize to represented as a Class instance
   * @param <T> Actual type to deserialize to.
   * @return The deserialized object.
   */
  public <T> T findByFileDescriptor(FileDescriptor fileDescriptor, Class<T> type) {

    Objects.requireNonNull(fileDescriptor, NULL_FILE_DESCRIPTOR_MESSAGE);
    Objects.requireNonNull(type, NULL_TYPE_MESSAGE);

    return this.findByFileDescriptor(fileDescriptor, getObjectMapperTransformer(fileDescriptor, type));
  }

  /**
   * Get a file given a FileDescriptor and file transformer. The file is loaded from Minio
   * and deserialized via the fileTransformer.
   *
   * @param fileDescriptor Describes the file to load
   * @param fileTransformer the FileTransformer instance used to deserialize.
   * @param <T> Actual type to deserialize to.
   * @return The deserialized object.
   */
  public <T> T findByFileDescriptor(FileDescriptor fileDescriptor, FileTransformer<T> fileTransformer) {

    Objects.requireNonNull(fileDescriptor, NULL_FILE_DESCRIPTOR_MESSAGE);
    Objects.requireNonNull(fileTransformer, NULL_FILE_TRANSFORMER_MESSAGE);

    try {
      var response = minioClient.getObject(
        GetObjectArgs.builder()
          .bucket(fileDescriptor.getBucket())
          .object(fileDescriptor.getKey())
          .build()
      );

      return fileTransformer.transform(response);

    } catch (ErrorResponseException
             | InsufficientDataException
             | InternalException
             | InvalidKeyException
             | InvalidResponseException
             | IOException
             | NoSuchAlgorithmException
             | ServerException
             | XmlParserException e) {

      throw new FileStoreRuntimeException(fileDescriptor, e);
    }
  }

  /**
   * Given a FileDescriptor, find all files in the bucket specified in the FileDescriptor that start
   * with the prefix in the key field of the FileDescriptor.
   *
   * @param fileDescriptor FileDescriptor instance to use
   * @param type Type to deserialize to represented as a Class instance
   * @param <T> Actual type to deserialize to
   * @return A map that maps specific FileDescriptors to the deserialized objects that were found.
   */
  public <T> Map<FileDescriptor, T> findByKeyPrefix(FileDescriptor fileDescriptor, Class<T> type) {

    Objects.requireNonNull(fileDescriptor, NULL_FILE_DESCRIPTOR_MESSAGE);
    Objects.requireNonNull(type, NULL_TYPE_MESSAGE);

    return this.findByKeyPrefix(fileDescriptor, getObjectMapperTransformer(fileDescriptor, type));
  }

  /**
   * Given a FileDescriptor, find all files in the bucket specified in the FileDescriptor that start
   * with the prefix in the key field of the FileDescriptor.
   *
   * @param fileDescriptor FileDescriptor instance to use
   * @param fileTransformer the FileTransformer instance used to deserialize.
   * @param <T> Actual type to deserialize to
   * @return A map that maps specific FileDescriptors to the deserialized objects that were found.
   */
  public <T> Map<FileDescriptor, T> findByKeyPrefix(FileDescriptor fileDescriptor, FileTransformer<T> fileTransformer) {

    Objects.requireNonNull(fileDescriptor, NULL_FILE_DESCRIPTOR_MESSAGE);
    Objects.requireNonNull(fileTransformer, NULL_FILE_TRANSFORMER_MESSAGE);

    var prefix = fileDescriptor.getKey();
    if (!prefix.endsWith("/")) {
      prefix = prefix + "/";
    }

    var objectMetadatas = minioClient.listObjects(
      ListObjectsArgs.builder()
        .bucket(fileDescriptor.getBucket())
        .prefix(prefix)
        .build()
    );

    return Streams.stream(objectMetadatas)
      .map(result -> {
        try {
          return result.get();
        } catch (ErrorResponseException
                 | InsufficientDataException
                 | InternalException
                 | InvalidKeyException
                 | InvalidResponseException
                 | IOException
                 | NoSuchAlgorithmException
                 | ServerException
                 | XmlParserException e) {
          throw new FileStoreRuntimeException(fileDescriptor, e);
        }
      })
      .map(item -> FileDescriptor.create(fileDescriptor.getBucket(), item.objectName()))
      .map(itemFileDiscriptor -> Map.entry(
        itemFileDiscriptor,
        findByFileDescriptor(itemFileDiscriptor, fileTransformer)
      ))
      .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Helper method which returns a FileTransformer that simply uses the object mapper to deserialize.
   * Reduces some highly repetitive code.
   *
   * @param fileDescriptor file descriptor - its only use is to provide info if there is an exception.
   * @param type type to deserialize to, represented as a Java Class instance.
   * @param <T> Actual type to deserialize to.
   * @return A FileTransformer that use the objectMapper for this FileStore instance.
   */
  private <T> FileTransformer<T> getObjectMapperTransformer(FileDescriptor fileDescriptor, Class<T> type) {
    return rawDataStream -> {
      try {
        return objectMapper.readValue(rawDataStream, type);
      } catch (IOException e) {
        throw new FileStoreRuntimeException(fileDescriptor, e);
      }
    };
  }

}

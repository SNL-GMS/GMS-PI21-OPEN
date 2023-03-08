package gms.shared.utilities.filestore;

import com.google.auto.value.AutoValue;

/**
 * Describes a Minio object via a bucket and a key.
 */
@AutoValue
public abstract class FileDescriptor {

  public abstract String getBucket();

  public abstract String getKey();

  public static FileDescriptor create(String bucket, String key) {
    return new AutoValue_FileDescriptor(bucket, key);
  }

}

package gms.shared.utilities.filestore;

/**
 * Wraps exceptions around a custom runtime exception.
 */
public class FileStoreRuntimeException extends RuntimeException {

  public FileStoreRuntimeException(FileDescriptor fileDescriptor, Throwable cause) {
    super("FileStore encountered an exception with FileDescriptor " + fileDescriptor, cause);
  }

}

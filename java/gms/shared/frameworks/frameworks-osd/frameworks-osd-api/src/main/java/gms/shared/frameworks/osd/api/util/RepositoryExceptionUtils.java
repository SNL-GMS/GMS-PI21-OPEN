package gms.shared.frameworks.osd.api.util;

import gms.shared.frameworks.coi.exceptions.DataExistsException;
import gms.shared.frameworks.coi.exceptions.RepositoryException;
import gms.shared.frameworks.coi.exceptions.StorageUnavailableException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.exception.JDBCConnectionException;

public class RepositoryExceptionUtils {

  private RepositoryExceptionUtils() {
    // prevent instantiation
  }

  public static RepositoryException wrap(Exception e) {
    if (isStorageUnavailableException(e)) {
      return new StorageUnavailableException(e);
    } else if (isDataExistsException(e)) {
      return new DataExistsException(e);
    }
    return new RepositoryException(e);
  }

  public static RepositoryException wrapWithContext(String context, Exception e) {
    if (isStorageUnavailableException(e)) {
      return new StorageUnavailableException(context, e);
    } else if (isDataExistsException(e)) {
      return new DataExistsException(context, e);
    }
    return new RepositoryException(context, e);
  }

  public static boolean isStorageUnavailableException(Exception e) {
    return containsCause(e, JDBCConnectionException.class);
  }

  public static boolean isDataExistsException(Exception e) {
    return e.getClass() == DataExistsException.class;
  }

  public static boolean containsCause(Exception e, Class<? extends Throwable> clazz) {
    return ExceptionUtils.indexOfThrowable(e, clazz) >= 0;
  }

}

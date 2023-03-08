package gms.shared.frameworks.utilities;

import com.google.auto.value.AutoValue;
import gms.shared.frameworks.common.ContentType;

import java.lang.reflect.Method;

/**
 * Represents a {@link Method} annotated by {@link javax.ws.rs.Path}.  Contains attributes for all
 * known information about the method and annotations relevant to service and/or client generation.
 */
@AutoValue
public abstract class PathMethod {

  /**
   * Obtains the full relative path, (including base path but excluding scheme, host, and port), for this object
   *
   * @return String, not null and starts with '/'
   */
  public abstract String getRelativePath();

  /**
   * Obtains the {@link Method} annotated by the other information provided by {@link PathMethod}
   *
   * @return {@link Method}, not null
   */
  public abstract Method getMethod();

  /**
   * Gets the content type the method takes as input.
   *
   * @return {@link ContentType}, not null
   */
  public abstract ContentType getInputFormat();

  /**
   * Gets the content type the method returns as output.
   *
   * @return {@link ContentType}, not null
   */
  public abstract ContentType getOutputFormat();

  public static PathMethod from(String relativePath, Method method,
    ContentType inputFormat, ContentType outputFormat) {
    return new AutoValue_PathMethod(prependSlashIfNotPresent(relativePath),
      method, inputFormat, outputFormat);
  }

  private static String prependSlashIfNotPresent(String s) {
    return s.startsWith("/") ? s : "/" + s;
  }
}

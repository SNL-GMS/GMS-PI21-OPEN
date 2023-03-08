package gms.shared.utilities.filestore;

import java.io.IOException;
import java.io.InputStream;

/**
 * Specifies a FileTransformer, which, given an InputStream, reads it and constructs a Java object
 * from it.
 *
 * @param <T> The type of object to construct.
 */
public interface FileTransformer<T> {

  T transform(InputStream rawDataStream) throws IOException;

}

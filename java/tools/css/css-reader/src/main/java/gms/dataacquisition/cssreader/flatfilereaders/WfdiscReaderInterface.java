package gms.dataacquisition.cssreader.flatfilereaders;

import com.github.ffpojo.exception.FFPojoException;
import gms.dataacquisition.cssreader.data.WfdiscRecord;

import java.io.IOException;
import java.util.Collection;

/**
 * Interface for reading WfdiscRecord's from a Source, such as a String that points to a flat file
 * directory or a database connection.
 */
@FunctionalInterface
public interface WfdiscReaderInterface {

  /**
   * Read the wfdisc file located at the path provided into WfdiscRecord's.
   *
   * @param s the path to the wfdisc file
   * @return WfdiscRecord's parsed from the given file
   * @throws IOException if errors occur in reading the file, parsing, etc.
   */
  Collection<WfdiscRecord> read(String s) throws IOException, FFPojoException;
}

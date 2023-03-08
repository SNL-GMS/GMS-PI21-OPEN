package gms.shared.stationdefinition.api.util;

import com.google.common.collect.ImmutableList;

/**
 * Interface method for station definition requests and caching
 */
public interface Request {
  ImmutableList<String> getNames();
}

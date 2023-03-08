package gms.shared.featureprediction.plugin.lookuptable.traveltime;

import com.google.auto.value.AutoValue;
import gms.shared.utilities.filestore.FileDescriptor;

/**
 * Defines processing configuration for TravelTimeLookupTableDefinition
 */
@AutoValue
public abstract class TravelTimeLookupTableDefinition {

  public static Builder builder() {
    return new AutoValue_TravelTimeLookupTableDefinition.Builder();
  }

  /**
   * Gets the string defining the bucket to find the minio data for this table
   *
   * @return A string defining the bucket to find the minio data for this table
   */
  public abstract FileDescriptor getFileDescriptor();

  /**
   * Builder for constructing TravelTimeLookupTableDefinition instances
   */
  @AutoValue.Builder
  public abstract static class Builder {

    /**
     * Returns a Builder with the bucket to find the minio data for this table field set
     *
     * @param fileDescriptor The TravelTimeLookupTableDefinition bucket value to set
     * @return A Builder with the FileDescriptor field set
     */
    public abstract Builder setFileDescriptor(FileDescriptor fileDescriptor);

    /**
     * Creates a new TravelTimeLookupTableDefinition instance with attributes initialized by the
     * Builder
     *
     * @return A new TravelTimeLookupTableDefinition instance with attributes initialized by the
     * Builder
     */
    public abstract TravelTimeLookupTableDefinition build();

  }
}

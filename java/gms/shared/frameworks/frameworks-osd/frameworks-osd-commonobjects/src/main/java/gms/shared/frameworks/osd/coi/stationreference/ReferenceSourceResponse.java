package gms.shared.frameworks.osd.coi.stationreference;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;

import java.util.List;

@AutoValue
@JsonSerialize(as = ReferenceSourceResponse.class)
@JsonDeserialize(builder = AutoValue_ReferenceSourceResponse.Builder.class)
public abstract class ReferenceSourceResponse {

  public abstract byte[] getSourceResponseData();

  public abstract Units getSourceResponseUnits();

  public abstract ResponseTypes getSourceResponseTypes();

  public abstract List<InformationSource> getInformationSources();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ReferenceSourceResponse.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setSourceResponseData(byte[] sourceResponseData);

    Builder setSourceResponseUnits(Units sourceResponseUnits);

    Builder setSourceResponseTypes(ResponseTypes sourceResponseTypes);

    Builder setInformationSources(List<InformationSource> informationSources);

    ReferenceSourceResponse build();
  }
}
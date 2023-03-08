package gms.dataacquisition.stationreceiver.cd11.common.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

@AutoValue
public abstract class Cd11DataConsumerParametersTemplatesFile {

  public abstract ImmutableList<Cd11DataConsumerParametersTemplate> getCd11DataConsumerParametersTemplates();

  @JsonCreator
  public static Cd11DataConsumerParametersTemplatesFile from(
    @JsonProperty("stations") ImmutableList<Cd11DataConsumerParametersTemplate> templates) {
    return new AutoValue_Cd11DataConsumerParametersTemplatesFile(templates);
  }
}

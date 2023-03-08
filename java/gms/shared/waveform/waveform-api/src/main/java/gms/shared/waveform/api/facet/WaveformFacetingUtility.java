package gms.shared.waveform.api.facet;

import com.google.common.base.Preconditions;
import gms.shared.stationdefinition.api.StationDefinitionAccessorInterface;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.facets.FacetingDefinition;
import gms.shared.stationdefinition.facet.FacetingTypes;
import gms.shared.stationdefinition.facet.StationDefinitionFacetingUtility;
import gms.shared.waveform.api.WaveformRepositoryInterface;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Timeseries;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;

@Component
public class WaveformFacetingUtility {

  //@TODO: this will be needed in the future
  private final WaveformRepositoryInterface waveformAccessor;
  private final StationDefinitionFacetingUtility stationDefinitionFacetingUtility;

  public WaveformFacetingUtility(
    WaveformRepositoryInterface waveformAccessor,
    @Qualifier("bridgedAccessor") StationDefinitionAccessorInterface stationDefinitionAccessorImpl) {
    this.waveformAccessor = waveformAccessor;
    this.stationDefinitionFacetingUtility = StationDefinitionFacetingUtility.create(stationDefinitionAccessorImpl);
  }

  public ChannelSegment<? extends Timeseries> populateFacets(ChannelSegment<? extends Timeseries> initialChannelSegment,
    FacetingDefinition facetingDefinition) {

    Objects.requireNonNull(initialChannelSegment);
    Objects.requireNonNull(facetingDefinition);
    checkState(facetingDefinition.getClassType().equals(FacetingTypes.CHANNEL_SEGMENT_TYPE.getValue()));
    checkState(facetingDefinition.isPopulated(),
      FacetingTypes.CHANNEL_SEGMENT_TYPE.getValue() + " only supports populated = true at this time");

    FacetingDefinition channelFacetingDefinition = facetingDefinition.getFacetingDefinitionByName(
      FacetingTypes.ID_CHANNEL_KEY.getValue());

    //validate facetingDefinition types passed in
    if (facetingDefinition.getFacetingDefinitions().size() > 0) {
      Preconditions.checkState(facetingDefinition.getFacetingDefinitions().size() == 1,
        "Only valid faceting definition is: " + FacetingTypes.CHANNEL_SEGMENT_TYPE.getValue());
      Preconditions.checkState(channelFacetingDefinition != null,
        "Only valid faceting definition is: " + FacetingTypes.CHANNEL_SEGMENT_TYPE.getValue());
      Preconditions.checkState(channelFacetingDefinition.getClassType().equals(FacetingTypes.CHANNEL_TYPE.getValue()),
        "Only valid faceting definition ClassType is: " + FacetingTypes.CHANNEL_TYPE.getValue());
    }

    //delegate channelFaceting to StationDefinitionFacetingUtility
    if (channelFacetingDefinition != null) {
      Channel facetedChannel = stationDefinitionFacetingUtility.populateFacets(
        initialChannelSegment.getId().getChannel(),
        channelFacetingDefinition,
        initialChannelSegment.getId().getCreationTime());
      return ChannelSegment.from(
        facetedChannel,
        initialChannelSegment.getUnits(),
        initialChannelSegment.getTimeseries(),
        initialChannelSegment.getId().getCreationTime());
    }

    return initialChannelSegment;
  }


}

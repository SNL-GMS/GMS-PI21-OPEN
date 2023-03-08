package gms.shared.event.accessor.facet;

import gms.shared.signaldetection.api.SignalDetectionAccessorInterface;
import gms.shared.signaldetection.api.facet.SignalDetectionFacetingUtility;
import gms.shared.stationdefinition.api.StationDefinitionAccessorInterface;
import gms.shared.stationdefinition.facet.StationDefinitionFacetingUtility;
import gms.shared.waveform.api.WaveformAccessorInterface;
import gms.shared.waveform.api.facet.WaveformFacetingUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides configured beans for autowiring in the {@link EventFacetingUtility}
 */
@Configuration
public class FacetingUtilityConfiguration {

  @Autowired
  @Bean
  public StationDefinitionFacetingUtility stationDefinitionFacetingUtility(
    @Qualifier("bridgedAccessor") StationDefinitionAccessorInterface stationDefinitionAccessorImpl) {
    return StationDefinitionFacetingUtility.create(stationDefinitionAccessorImpl);
  }

  @Autowired
  @Bean
  public SignalDetectionFacetingUtility signalDetectionFacetingUtility(
    @Qualifier("bridgedSignalDetectionAccessor") SignalDetectionAccessorInterface signalDetectionAccessor,
    WaveformAccessorInterface waveformAccessor,
    @Qualifier("bridgedAccessor") StationDefinitionAccessorInterface stationDefinitionAccessorImpl,
    StationDefinitionFacetingUtility stationDefinitionFacetingUtility) {

    return SignalDetectionFacetingUtility.create(signalDetectionAccessor,
      new WaveformFacetingUtility(waveformAccessor, stationDefinitionAccessorImpl),
      stationDefinitionFacetingUtility);
  }


}

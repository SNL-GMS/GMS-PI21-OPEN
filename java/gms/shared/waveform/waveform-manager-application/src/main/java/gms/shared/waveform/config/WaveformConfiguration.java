package gms.shared.waveform.config;

import gms.shared.frameworks.cache.utils.CacheInfo;
import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.stationdefinition.cache.configuration.CacheAccessorConfiguration;
import gms.shared.waveform.coi.ChannelSegmentDescriptor;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

@Configuration
@ComponentScan(basePackages = {
  "gms.shared.spring",
  "gms.shared.stationdefinition",
  "gms.shared.emf.stationdefinition"},
  excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "gms\\.shared\\.spring\\.persistence\\..*"))
@Import(CacheAccessorConfiguration.class)
public class WaveformConfiguration {
  public static final CacheInfo CHANNEL_SEGMENT_DESCRIPTOR_WFID_CACHE =
    new CacheInfo("channel-segment-descriptor-wfid-cache",
      CacheMode.REPLICATED, CacheAtomicityMode.ATOMIC, true, Optional.empty());

  private static final Logger logger = LoggerFactory.getLogger(WaveformConfiguration.class);

  @Bean
  public IgniteCache<ChannelSegmentDescriptor, Long> channelSegmentDescriptorWfidCache(
    @Autowired SystemConfig systemConfig) {

    try {
      IgniteConnectionManager.initialize(systemConfig, List.of(CHANNEL_SEGMENT_DESCRIPTOR_WFID_CACHE));
    } catch (IllegalStateException ex) {
      logger.warn("Channel Segment cache already initialized");
    }
    return IgniteConnectionManager.getOrCreateCache(CHANNEL_SEGMENT_DESCRIPTOR_WFID_CACHE);
  }
}

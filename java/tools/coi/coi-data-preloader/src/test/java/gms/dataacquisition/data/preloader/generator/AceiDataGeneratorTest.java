package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.shared.frameworks.injector.AceiIdModifier;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.AceiUpdates;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

abstract class AceiDataGeneratorTest<G extends AceiDataGenerator<T>, T extends AcquiredChannelEnvironmentIssue<?>> extends
  CoiDataGeneratorTest<G, T, AceiIdModifier> {

  @Captor
  protected ArgumentCaptor<AceiUpdates> updatesCaptor;

  @Override
  protected abstract G getDataGenerator(GenerationSpec generationSpec,
    OsdRepositoryInterface sohRepository);

  @Override
  protected List<String> getSeedNames() {
    return stationGroups.stream()
      .flatMap(g -> g.getStations().stream())
      .flatMap(g -> g.getChannels().stream())
      .map(Channel::getName)
      .distinct()
      .collect(Collectors.toList());
  }

  @Override
  protected List<T> getRecordsToSend() {
    return new ArrayList<>();
  }

  protected long getNumberOfChannels() {
    return stationGroups.stream()
      .flatMap(g -> g.getStations().stream())
      .flatMap(s -> s.getChannels().stream())
      .map(Channel::getName)
      .distinct()
      .count();
  }

  @Override
  protected int getWantedNumberOfItemsGenerated() {
    return (int) Math.ceil((((double) generationDuration.toNanos()) / generationFrequency.toNanos())
      * getNumberOfChannels());
  }
}
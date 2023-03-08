package gms.dataacquisition.stationreceiver.cd11.parser.util;

import gms.shared.frameworks.osd.api.channel.ChannelRepositoryInterface;
import gms.shared.frameworks.osd.coi.channel.Channel;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

public class MockUtility {

  public static Channel mockChannel(String name) {
    Channel channel = mock(Channel.class);
    willReturn(name).given(channel).getName();
    return channel;
  }

  public static void configureMockRepository(ChannelRepositoryInterface repository,
    Channel... mockChannels) {

    List<String> mockChannelNames = Arrays.stream(mockChannels).map(Channel::getName)
      .collect(toList());

    given(repository.retrieveChannels(mockChannelNames))
      .willReturn(Arrays.asList(mockChannels));
  }
}

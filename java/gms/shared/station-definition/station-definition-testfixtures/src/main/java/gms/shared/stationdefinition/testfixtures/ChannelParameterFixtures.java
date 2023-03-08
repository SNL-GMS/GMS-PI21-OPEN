package gms.shared.stationdefinition.testfixtures;

import gms.shared.stationdefinition.coi.channel.Channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fixtures for creating lists of channels and channel to site param map
 */
public class ChannelParameterFixtures {
  private List<Channel> channelList;
  private Map<String, Map<CssDaoAndCoiParameters.SITE_ARGS, Double>> channelNametoParametersMap;

  public ChannelParameterFixtures() {
    channelList = new ArrayList<>();
    channelNametoParametersMap = new HashMap<>();
  }

  public void addChannels(List<Channel> newChannels) {
    this.channelList.addAll(newChannels);
  }

  public void addParamMap(String channelName, Map<CssDaoAndCoiParameters.SITE_ARGS, Double> siteParamMap) {
    this.channelNametoParametersMap.put(channelName, siteParamMap);
  }

  public List<Channel> getChannelList() {
    return channelList;
  }

  public Map<String, Map<CssDaoAndCoiParameters.SITE_ARGS, Double>> getChannelNametoParametersMap() {
    return channelNametoParametersMap;
  }
}

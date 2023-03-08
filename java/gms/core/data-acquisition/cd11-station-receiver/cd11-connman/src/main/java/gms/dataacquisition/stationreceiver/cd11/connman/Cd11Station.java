package gms.dataacquisition.stationreceiver.cd11.connman;


import java.net.InetAddress;

class Cd11Station {
  public final InetAddress expectedDataProviderIpAddress;
  public final InetAddress dataConsumerIpAddress;
  public final int dataConsumerPort;

  Cd11Station(
    InetAddress expectedDataProviderIpAddress,
    InetAddress dataConsumerIpAddress,
    int dataConsumerPort) {
    this.expectedDataProviderIpAddress = expectedDataProviderIpAddress;
    this.dataConsumerIpAddress = dataConsumerIpAddress;
    this.dataConsumerPort = dataConsumerPort;
  }
}

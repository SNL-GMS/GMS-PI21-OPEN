package gms.dataacquisition.stationreceiver.cd11.common.frames;

public interface Cd11Payload {
  byte[] toBytes();

  default int getByteCount() {
    return toBytes().length;
  }
}

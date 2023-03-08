package gms.dataacquisition.stationreceiver.cd11.common.frames;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FrameTrailerTestUtility {

  public static Cd11Trailer createTrailerWithoutAuthentication(Cd11Header header,
    byte[] payload) {

    var outStream = new ByteArrayOutputStream();
    try {
      outStream.write(header.toBytes());
      outStream.write(payload);
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to read segments into single byte array", e);
    }

    return Cd11Trailer.fromBytes(0, outStream.toByteArray());
  }
}

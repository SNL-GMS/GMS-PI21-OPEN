package gms.dataacquisition.stationreceiver.cd11.common.frames;


import com.google.auto.value.AutoValue;

import java.util.Optional;

/**
 * PartialFrame is used as an intermediate step between a fully parsed frame and the bytebuffer
 * received on the network
 */
@AutoValue
public abstract class PartialFrame {
  public abstract Optional<Cd11Header> getHeader();

  public abstract Optional<Cd11Payload> getBody();

  public abstract Optional<Cd11Trailer> getTrailer();

  public static Builder builder() {
    return new AutoValue_PartialFrame.Builder();
  }

  @AutoValue.Builder
  public interface Builder {
    Builder setHeader(Cd11Header header);

    Builder setBody(Cd11Payload body);

    Builder setTrailer(Cd11Trailer trailer);

    PartialFrame build();
  }


}
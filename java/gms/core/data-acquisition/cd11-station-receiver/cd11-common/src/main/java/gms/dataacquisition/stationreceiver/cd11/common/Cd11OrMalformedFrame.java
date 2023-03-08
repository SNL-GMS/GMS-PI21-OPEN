package gms.dataacquisition.stationreceiver.cd11.common;

import com.google.auto.value.AutoOneOf;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.MalformedFrame;

@AutoOneOf(Cd11OrMalformedFrame.Kind.class)
public abstract class Cd11OrMalformedFrame {
  public enum Kind {CD11, MALFORMED}

  public abstract Kind getKind();

  public abstract Cd11Frame cd11();

  public abstract MalformedFrame malformed();

  public static Cd11OrMalformedFrame ofCd11(Cd11Frame cd11Frame) {
    return AutoOneOf_Cd11OrMalformedFrame.cd11(cd11Frame);
  }

  public static Cd11OrMalformedFrame ofMalformed(MalformedFrame malformedFrame) {
    return AutoOneOf_Cd11OrMalformedFrame.malformed(malformedFrame);
  }


}

package gms.shared.frameworks.osd.dto.soh;

import com.google.auto.value.AutoValue;

public abstract class DoubleOrInteger {

  private DoubleOrInteger() {

  }

  public static DoubleOrInteger ofDouble(double val) {
    return new AutoValue_DoubleOrInteger_DoubleValue(val);
  }

  public static DoubleOrInteger ofInteger(int val) {
    return new AutoValue_DoubleOrInteger_IntegerValue(val);
  }

  @AutoValue
  public abstract static class DoubleValue extends DoubleOrInteger {

    public abstract double getValue();
  }

  @AutoValue
  public abstract static class IntegerValue extends DoubleOrInteger {

    public abstract int getValue();
  }

}

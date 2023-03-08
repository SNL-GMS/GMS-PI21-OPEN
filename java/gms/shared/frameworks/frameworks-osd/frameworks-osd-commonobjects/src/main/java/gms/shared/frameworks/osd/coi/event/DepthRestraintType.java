package gms.shared.frameworks.osd.coi.event;

/**
 * Define an enumeration of depth restraint types for location solutions.
 *
 * @deprecated As of PI 17.5, this COI has been declared OBE, and will be completely unused.
 * All usage of this COI outside the Frameworks area should be avoided
 */
@Deprecated(since = "17.5", forRemoval = true)
public enum DepthRestraintType {
  UNRESTRAINED,
  FIXED_AT_DEPTH {
    @Override
    public double getValue(double originalValue, double fixedValue) {
      return fixedValue;
    }
  },
  FIXED_AT_SURFACE {
    @Override
    public double getValue(double originalValue, double fixedValue) {
      return 0.0;
    }
  };

  public double getValue(double originalValue, double fixedValue) {
    return originalValue;
  }
}

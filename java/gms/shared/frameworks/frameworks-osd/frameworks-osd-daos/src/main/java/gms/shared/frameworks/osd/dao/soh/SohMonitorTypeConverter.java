package gms.shared.frameworks.osd.dao.soh;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.dao.util.EnumToOrdinalShortConverter;

import javax.persistence.Converter;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@Converter(autoApply = true)
public class SohMonitorTypeConverter extends EnumToOrdinalShortConverter<SohMonitorType> {

  private static final Map<SohMonitorType, Short> toColumn;

  static {
    toColumn = new EnumMap<>(SohMonitorType.class);

    Arrays.asList(SohMonitorType.values())
      .forEach(x -> toColumn.put(x, x.getDbId()));
  }

  /**
   * Creates a converter that maps a SohMonitorType enum to a string representation of those enums
   */
  public SohMonitorTypeConverter() {
    super(toColumn);
  }
}

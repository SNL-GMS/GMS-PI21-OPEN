package gms.shared.event.coi;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;

public enum MagnitudeType {
  MB("mb"),
  MB_CODA("mb_coda"),
  MB_MB("mb_mb"),
  MB_MLE("mb_mle"),
  MB_PG("mb_pg"),
  MB_REL_T("mb_rel_t"),
  ML("ml"),
  MS("ms"),
  MS_MLE("ms_mle"),
  MS_VMAX("ms_max"),
  MW_CODA("mw_coda");

  private static final ImmutableMap<String, MagnitudeType> flagsByType;

  static {
    flagsByType = Arrays.stream(values())
      .collect(ImmutableMap.toImmutableMap(MagnitudeType::getType, Functions.identity()));
  }

  private final String type;

  MagnitudeType(String type) {
    this.type = type;
  }

  public static MagnitudeType fromString(String type) {
    return flagsByType.get(type);
  }

  public String getType() {
    return type;
  }

}
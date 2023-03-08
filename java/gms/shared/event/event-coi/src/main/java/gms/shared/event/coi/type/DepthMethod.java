package gms.shared.event.coi.type;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;

public enum DepthMethod {
  A("a"),
  F("f"),
  D("d"),
  R("r"),
  G("g"),
  UNKNOWN("-");


  private static final ImmutableMap<String, DepthMethod> LABELS_BY_MODEL;

  static {
    LABELS_BY_MODEL = Arrays.stream(values())
      .collect(ImmutableMap.toImmutableMap(DepthMethod::getLabel, Functions.identity()));
  }

  public final String label;


  DepthMethod(String label) {
    this.label = label;
  }

  public static DepthMethod valueOfLabel(String label) {
    Preconditions.checkNotNull(label, "label cannot be null");
    Preconditions.checkArgument(!label.isBlank(), "label cannot be blank");
    return LABELS_BY_MODEL.get(label);
  }

  public String getLabel() {
    return label;
  }

}


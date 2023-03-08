package gms.shared.event.coi.featureprediction.type;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import gms.shared.event.coi.featureprediction.value.ArrivalTimeFeaturePredictionValue;
import gms.shared.event.coi.featureprediction.value.FeaturePredictionValue;
import gms.shared.event.coi.featureprediction.value.NumericFeaturePredictionValue;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Class for an instantiable object that represents the type of feature prediction. Each "type"
 * is associated with a FeaturePredictionValue class; together, they fully define what each type of
 * feature prediction looks like.
 *
 * @param <T> The FeaturePredictionValue class that should be coupled with this type.
 */
@JsonDeserialize(using = FeaturePredictionType.Deserializer.class)
public abstract class FeaturePredictionType<T extends FeaturePredictionValue<?, ?, ?>> {

  public enum TypeNames {
    ARRIVAL_TIME,
    SLOWNESS,
    EMERGENCE_ANGLE,
    RECEIVER_TO_SOURCE_AZIMUTH,
    SOURCE_TO_RECEIVER_AZIMUTH,
    SOURCE_TO_RECEIVER_DISTANCE,
  }

  private final TypeNames name;

  private FeaturePredictionType(
    TypeNames name
  ) {
    this.name = name;
  }

  @JsonValue
  public TypeNames getName() {
    return name;
  }

  @Override
  public String toString() {
    return "FeaturePredictionType{" +
      "name='" + name + '\'' +
      '}';
  }

  public abstract Class<T> getTypeValueClass();

  //
  // Constants for the supported feature prediction types.
  //

  public static final FeaturePredictionType<ArrivalTimeFeaturePredictionValue> ARRIVAL_TIME_PREDICTION_TYPE =
    new FeaturePredictionType<>(TypeNames.ARRIVAL_TIME) {
      @Override
      public Class<ArrivalTimeFeaturePredictionValue> getTypeValueClass() {
        return ArrivalTimeFeaturePredictionValue.class;
      }
    };

  public static final FeaturePredictionType<NumericFeaturePredictionValue> SLOWNESS_PREDICTION_TYPE =
    new FeaturePredictionType<>(TypeNames.SLOWNESS) {
      @Override
      public Class<NumericFeaturePredictionValue> getTypeValueClass() {
        return NumericFeaturePredictionValue.class;
      }
    };

  public static final FeaturePredictionType<NumericFeaturePredictionValue> EMERGENCE_ANGLE_PREDICTION_TYPE =
    new FeaturePredictionType<>(TypeNames.EMERGENCE_ANGLE) {
      @Override
      public Class<NumericFeaturePredictionValue> getTypeValueClass() {
        return NumericFeaturePredictionValue.class;
      }
    };

  public static final FeaturePredictionType<NumericFeaturePredictionValue> RECEIVER_TO_SOURCE_AZIMUTH_PREDICTION_TYPE =
    new FeaturePredictionType<>(TypeNames.RECEIVER_TO_SOURCE_AZIMUTH) {
      @Override
      public Class<NumericFeaturePredictionValue> getTypeValueClass() {
        return NumericFeaturePredictionValue.class;
      }
    };

  public static final FeaturePredictionType<NumericFeaturePredictionValue> SOURCE_TO_RECEIVER_AZIMUTH_PREDICTION_TYPE =
    new FeaturePredictionType<>(TypeNames.SOURCE_TO_RECEIVER_AZIMUTH) {
      @Override
      public Class<NumericFeaturePredictionValue> getTypeValueClass() {
        return NumericFeaturePredictionValue.class;
      }
    };

  public static final FeaturePredictionType<NumericFeaturePredictionValue> SOURCE_TO_RECEIVER_DISTANCE_PREDICTION_TYPE =
    new FeaturePredictionType<>(TypeNames.SOURCE_TO_RECEIVER_DISTANCE) {
      @Override
      public Class<NumericFeaturePredictionValue> getTypeValueClass() {
        return NumericFeaturePredictionValue.class;
      }
    };

  /**
   * Custom deserializer used by Jackson. The serialized JSON should contain a single field with
   * a single string that is the name of one of the types, and that string will be returned
   * be JsonParser::getValueAsString.
   */
  static class Deserializer extends StdDeserializer<FeaturePredictionType<?>> {

    private static final Map<TypeNames, FeaturePredictionType<?>> nameTypeMap = Map.of(
      TypeNames.ARRIVAL_TIME, ARRIVAL_TIME_PREDICTION_TYPE,
      TypeNames.SLOWNESS, SLOWNESS_PREDICTION_TYPE,
      TypeNames.EMERGENCE_ANGLE, EMERGENCE_ANGLE_PREDICTION_TYPE,
      TypeNames.RECEIVER_TO_SOURCE_AZIMUTH, RECEIVER_TO_SOURCE_AZIMUTH_PREDICTION_TYPE,
      TypeNames.SOURCE_TO_RECEIVER_AZIMUTH, SOURCE_TO_RECEIVER_AZIMUTH_PREDICTION_TYPE,
      TypeNames.SOURCE_TO_RECEIVER_DISTANCE, SOURCE_TO_RECEIVER_DISTANCE_PREDICTION_TYPE
    );

    public Deserializer() {
      this(null);
    }

    public Deserializer(Class<?> vc) {
      super(vc);
    }

    @Override
    public FeaturePredictionType<?> deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
      return Optional.ofNullable(nameTypeMap.get(TypeNames.valueOf(p.getValueAsString())))
        .orElseThrow();
    }
  }
}

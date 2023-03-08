package gms.shared.featureprediction.plugin.correction.ellipticity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.signaldetection.coi.types.PhaseType;
import java.util.List;

@AutoValue
@JsonSerialize(as = DziewonskiGilbertLookupTableView.class)
@JsonDeserialize(builder = AutoValue_DziewonskiGilbertLookupTableView.Builder.class)
public abstract class DziewonskiGilbertLookupTableView {
  
  public abstract String getModel();
  
  public abstract PhaseType getPhase();
  
  public abstract String getDepthUnits();
  
  public abstract String getDistanceUnits();
  
  public abstract List<Double> getDepths();
  
  public abstract List<Double> getDistances();
  
  public abstract List<List<Double>> getTau0();
  
  public abstract List<List<Double>> getTau1();
  
  public abstract List<List<Double>> getTau2();
  
  public static Builder builder() {
    return new AutoValue_DziewonskiGilbertLookupTableView.Builder();
  }
  
  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {
    
    public abstract Builder setModel(String model);
    
    public abstract Builder setPhase(PhaseType phase);
    
    public abstract Builder setDepthUnits(String depthUnits);
    
    public abstract Builder setDistanceUnits(String distanceUnits);
    
    public abstract Builder setDepths(List<Double> depths);
    
    public abstract Builder setDistances(List<Double> distances);
    
    public abstract Builder setTau0(List<List<Double>> tau0);
    
    public abstract Builder setTau1(List<List<Double>> tau1);
    
    public abstract Builder setTau2(List<List<Double>> tau2);
    
    public abstract DziewonskiGilbertLookupTableView build();
  }
}

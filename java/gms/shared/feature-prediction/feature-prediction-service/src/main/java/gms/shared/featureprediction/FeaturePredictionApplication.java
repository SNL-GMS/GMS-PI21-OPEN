package gms.shared.featureprediction;


import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(scanBasePackages = {
  "gms.shared.featureprediction", "gms.shared.plugin",
  "gms.shared.spring", "gms.shared.signaldetection",
  "gms.shared.utilities.filestore",
  "gms.shared.featureprediction.utilities"})
public class FeaturePredictionApplication {

  public static void main(String[] args) {
    new SpringApplicationBuilder(FeaturePredictionApplication.class)
      .run(args);
  }

}


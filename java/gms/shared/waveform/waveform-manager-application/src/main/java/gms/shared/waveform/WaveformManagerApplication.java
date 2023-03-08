package gms.shared.waveform;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EntityScan(basePackages = "gms.shared.stationdefinition.dao")
public class WaveformManagerApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(WaveformManagerApplication.class)
      .run(args);
  }
}



package gms.shared.workflow;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
@EntityScan(basePackages = "gms.shared.workflow.dao")
public class WorkflowApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(WorkflowApplication.class)
      .run(args);
  }
}

package gms.shared.plugin.injected;

import org.springframework.stereotype.Component;

@Component
public class BazImplOne implements IBaz {

  @Override
  public String printName() {
    return "BazImplOne";
  }

  @Override
  public String getName() {
    return "BazImplOne";
  }

  @Override
  public void initialize() {

  }
}

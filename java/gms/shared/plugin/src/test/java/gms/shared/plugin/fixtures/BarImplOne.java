package gms.shared.plugin.fixtures;

import org.springframework.stereotype.Component;

@Component
public class BarImplOne implements IBar {

  public static final String NAME = "bar_impl_1";

  public static final long VALUE = 1;

  public BarImplOne() {
  }

  @Override
  public long getBarValue() {
    return VALUE;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void initialize() {

  }
}

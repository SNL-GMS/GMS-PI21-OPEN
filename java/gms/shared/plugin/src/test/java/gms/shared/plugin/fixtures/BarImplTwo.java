package gms.shared.plugin.fixtures;

import org.springframework.stereotype.Component;

@Component
public class BarImplTwo implements IBar {

  public static final String NAME = "bar_impl_2";

  public static final long VALUE = 2;

  public BarImplTwo() {
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

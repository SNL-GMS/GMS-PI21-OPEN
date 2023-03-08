package gms.shared.plugin.fixtures;

import org.springframework.stereotype.Component;

@Component
public class FooImplTwo implements IFoo {

  public static final String NAME = "foo_impl_2";

  public static final long VALUE = 4;

  public FooImplTwo() {
  }

  @Override
  public long getFooValue() {
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
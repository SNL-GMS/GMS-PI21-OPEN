package gms.shared.plugin.fixtures;

import org.springframework.stereotype.Component;

@Component
public class FooImplOne implements IFoo {

  public static final String NAME = "foo_impl_1";

  public static final long VALUE = 3;

  public FooImplOne() {
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
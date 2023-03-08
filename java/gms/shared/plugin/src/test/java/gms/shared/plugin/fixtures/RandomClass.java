package gms.shared.plugin.fixtures;

import org.springframework.stereotype.Component;

@Component
public class RandomClass {

  public String identify() {
    return "Random class, shouldn't be in map";
  }

}

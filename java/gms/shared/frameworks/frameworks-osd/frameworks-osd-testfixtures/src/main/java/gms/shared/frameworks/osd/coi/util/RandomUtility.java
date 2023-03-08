package gms.shared.frameworks.osd.coi.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class RandomUtility {

  private static final SecureRandom random = new SecureRandom();

  private RandomUtility() {
  }

  public static String randomUpperCase(long length) {
    return Stream.generate(RandomUtility::randomUpperCase).limit(length)
      .collect(Collector.of(
        StringBuilder::new,
        StringBuilder::append,
        StringBuilder::append,
        StringBuilder::toString
      ));
  }

  private static char randomUpperCase() {
    return (char) (random.nextInt(25) + 65);
  }

  public static <T extends Enum<?>> T randomEnum(Class<T> enumClass) {
    T[] enumConstants = enumClass.getEnumConstants();
    return enumConstants[randomInt(enumConstants.length)];
  }

  public static int randomInt(int bound) {
    return random.nextInt(bound);
  }

  public static double randomDouble(double bound) {
    return random.nextDouble() * bound;
  }

  public static Instant randomInstant(Instant bound) {
    return Instant.ofEpochSecond(randomInt((int) bound.getEpochSecond()));
  }
}

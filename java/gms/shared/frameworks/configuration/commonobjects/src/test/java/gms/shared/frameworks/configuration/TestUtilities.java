package gms.shared.frameworks.configuration;

import gms.shared.frameworks.configuration.Operator.Type;
import gms.shared.frameworks.configuration.constraints.NumericScalarConstraint;
import org.junit.jupiter.api.function.Executable;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtilities {

  private static NumericScalarConstraint snrIs10 = NumericScalarConstraint
    .from("sta", Operator.from(Type.EQ, false), 5.0, 100);

  private static ConfigurationOption configOptSnrIs10 = ConfigurationOption
    .from("SNR-10", List.of(snrIs10), Map.of("a", 10));

  public static Configuration configurationFilter = Configuration
    .from("Filter", List.of(configOptSnrIs10));


  // Expected FooParameters
  public static final FooParameters fooParamsDefaults = FooParameters
    .from(100, "string100", true);

  private TestUtilities() {
  }

  public static <T extends Throwable> void expectExceptionAndMessage(Executable executable,
    Class<T> throwableClass, String message) {

    final Throwable actualThrowable = assertThrows(throwableClass, executable);
    assertTrue(actualThrowable.getMessage().contains(message));
  }
}

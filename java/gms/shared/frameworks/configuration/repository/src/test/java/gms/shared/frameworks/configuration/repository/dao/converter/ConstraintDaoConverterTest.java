package gms.shared.frameworks.configuration.repository.dao.converter;

import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.ConfigurationOption;
import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.Operator;
import gms.shared.frameworks.configuration.Operator.Type;
import gms.shared.frameworks.configuration.constraints.BooleanConstraint;
import gms.shared.frameworks.configuration.constraints.DefaultConstraint;
import gms.shared.frameworks.configuration.constraints.DoubleRange;
import gms.shared.frameworks.configuration.constraints.NumericRangeConstraint;
import gms.shared.frameworks.configuration.constraints.NumericScalarConstraint;
import gms.shared.frameworks.configuration.constraints.PhaseConstraint;
import gms.shared.frameworks.configuration.constraints.StringConstraint;
import gms.shared.frameworks.configuration.constraints.TimeOfDayRange;
import gms.shared.frameworks.configuration.constraints.TimeOfDayRangeConstraint;
import gms.shared.frameworks.configuration.constraints.TimeOfYearRange;
import gms.shared.frameworks.configuration.constraints.TimeOfYearRangeConstraint;
import gms.shared.frameworks.configuration.constraints.WildcardConstraint;
import gms.shared.frameworks.configuration.repository.dao.ConfigurationOptionDao;
import gms.shared.frameworks.configuration.repository.dao.ConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.constraint.BooleanConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.constraint.DefaultConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.constraint.NumericRangeConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.constraint.NumericScalarConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.constraint.PhaseConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.constraint.StringConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.constraint.TimeOfDayRangeConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.constraint.TimeOfYearRangeConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.constraint.WildcardConstraintDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintDaoConverterTest {
  private ConstraintDaoConverter constraintDaoConverter;
  private Operator operatorIn = Operator.from(Type.IN, false);
  private Operator operatorEq = Operator.from(Type.EQ, false);

  @BeforeEach
  void setup() {

    Collection<ConfigurationOption> configurationOptions = new ArrayList<>();
    ConfigurationOption co = ConfigurationOption.from("config-option-test", new ArrayList<>(), new LinkedHashMap<>());
    configurationOptions.add(co);
    Configuration configuration = Configuration.from(
      "test-config", configurationOptions
    );
    ConfigurationDaoConverter cdConverter = new ConfigurationDaoConverter();
    ConfigurationOptionDaoConverter codConverter = new ConfigurationOptionDaoConverter(cdConverter.fromCoi(configuration));
    ConfigurationOptionDao configurationOptionDao = codConverter.fromCoi(co);
    constraintDaoConverter = new ConstraintDaoConverter(configurationOptionDao);
  }


  @ParameterizedTest
  @MethodSource("getConstraintTypes")
  void testConstraintConverter(Constraint fromConstraint, Class<?> fromClass, Class<?> toClass) {
    ConstraintDao constraintDao = constraintDaoConverter.fromCoi(fromConstraint);
    Constraint constraint = constraintDaoConverter.toCoi(constraintDao);

    assertTrue(toClass.isInstance(constraintDao), constraintDao.getClass().getCanonicalName() + " not correct type");
    assertTrue(fromClass.isInstance(constraint), constraint.getClass().getCanonicalName() + " not correct type");
    assertEquals(constraint, fromConstraint, "from Constraint not equal to converted Constraint");
  }

  private static Stream<Arguments> getConstraintTypes() {
    Operator operatorIn = Operator.from(Type.IN, false);
    Operator operatorEq = Operator.from(Type.EQ, false);

    return Stream.of(
      Arguments.of(BooleanConstraint.from("test", true, 1), BooleanConstraint.class, BooleanConstraintDao.class),
      Arguments.of(DefaultConstraint.from(), DefaultConstraint.class, DefaultConstraintDao.class),
      Arguments.of(NumericRangeConstraint.from("test", operatorIn, DoubleRange.from(0, 1), 1), NumericRangeConstraint.class, NumericRangeConstraintDao.class),
      Arguments.of(NumericScalarConstraint.from("test", operatorEq, 0, 1), NumericScalarConstraint.class, NumericScalarConstraintDao.class),
      Arguments.of(PhaseConstraint.from("test", operatorEq, new LinkedHashSet<>(), 1), PhaseConstraint.class, PhaseConstraintDao.class),
      Arguments.of(StringConstraint.from("test", operatorEq, new LinkedHashSet<>(), 1), StringConstraint.class, StringConstraintDao.class),
      Arguments.of(TimeOfDayRangeConstraint.from("test", operatorIn, TimeOfDayRange.from(LocalTime.now(), LocalTime.now()), 1), TimeOfDayRangeConstraint.class, TimeOfDayRangeConstraintDao.class),
      Arguments.of(TimeOfYearRangeConstraint.from("test", operatorIn, TimeOfYearRange.from(LocalDateTime.now(), LocalDateTime.now()), 1), TimeOfYearRangeConstraint.class, TimeOfYearRangeConstraintDao.class),
      Arguments.of(WildcardConstraint.from("test"), WildcardConstraint.class, WildcardConstraintDao.class)
    );
  }
}
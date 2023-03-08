package gms.shared.frameworks.configuration.repository.dao;

import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.ConfigurationOption;
import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.constraints.DefaultConstraint;
import gms.shared.frameworks.configuration.repository.dao.constraint.DefaultConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.converter.ConfigurationDaoConverter;
import gms.shared.frameworks.configuration.repository.dao.converter.ConfigurationOptionDaoConverter;
import gms.shared.frameworks.configuration.repository.dao.converter.ConstraintDaoConverter;
import gms.shared.frameworks.configuration.repository.dao.converter.OperatorDaoConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConstraintDaoTest {

  ConfigurationOptionDao cod;

  @BeforeEach
  public void init() {
    Collection<ConfigurationOption> configurationOptions = new ArrayList<>();

    ConfigurationOption co = ConfigurationOption.from("config-option-test", new ArrayList<>(), new LinkedHashMap<>());
    configurationOptions.add(co);

    Configuration configuration = Configuration.from(
      "test-config", configurationOptions
    );
    ConfigurationDaoConverter cdConverter = new ConfigurationDaoConverter();
    ConfigurationOptionDaoConverter codConverter = new ConfigurationOptionDaoConverter(cdConverter.fromCoi(configuration));

    this.cod = codConverter.fromCoi(co);
  }

  @Test
  void testEquals() {

    ConstraintDaoConverter constraintDaoConverter = new ConstraintDaoConverter(this.cod);
    ConstraintDao constraintDao = constraintDaoConverter.fromCoi(DefaultConstraint.from());

    ConstraintDao defaultConstraintDao = new DefaultConstraintDao();
    Constraint constraint = DefaultConstraint.from();
    defaultConstraintDao.setCriterion(constraint.getCriterion());
    defaultConstraintDao.setPriority(constraint.getPriority());
    OperatorDaoConverter operatorDaoConverter = new OperatorDaoConverter();
    defaultConstraintDao.setOperatorDao(operatorDaoConverter.fromCoi(constraint.getOperator()));
    defaultConstraintDao.setConfigurationOptionDao(this.cod);

    assertEquals(true, defaultConstraintDao.equals(constraintDao), "Equals not correct for ConstraintDao");
  }

  @Test
  void testHashCode() {
    ConstraintDao constraintDao = new DefaultConstraintDao();
    Constraint constraint = DefaultConstraint.from();
    constraintDao.setCriterion(constraint.getCriterion());
    constraintDao.setPriority(constraint.getPriority());
    OperatorDaoConverter operatorDaoConverter = new OperatorDaoConverter();
    constraintDao.setOperatorDao(operatorDaoConverter.fromCoi(constraint.getOperator()));
    constraintDao.setConfigurationOptionDao(this.cod);

    ConstraintDaoConverter constraintDaoConverter = new ConstraintDaoConverter(this.cod);
    ConstraintDao defaultConstraintDao = constraintDaoConverter.fromCoi(DefaultConstraint.from());

    Set<ConstraintDao> constraintDaoSet = new LinkedHashSet<>();
    constraintDaoSet.add(constraintDao);
    assertTrue(constraintDaoSet.contains(defaultConstraintDao), "HashCode not correct for ConstraintDao");
  }
}
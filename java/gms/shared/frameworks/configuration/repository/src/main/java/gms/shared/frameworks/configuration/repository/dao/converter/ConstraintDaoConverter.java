package gms.shared.frameworks.configuration.repository.dao.converter;

import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.ConstraintType;
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
import gms.shared.frameworks.utilities.jpa.EntityConverter;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class ConstraintDaoConverter implements EntityConverter<ConstraintDao, Constraint> {

  private ConfigurationOptionDao configurationOptionDao;

  private Map<ConstraintType, Supplier<ConstraintDao>> constraintDaoSupplierMap;

  public ConstraintDaoConverter(ConfigurationOptionDao configurationOptionDao) {
    this.configurationOptionDao = configurationOptionDao;
    this.constraintDaoSupplierMap = new EnumMap<>(ConstraintType.class);
    this.constraintDaoSupplierMap.put(ConstraintType.BOOLEAN, BooleanConstraintDao::new);
    this.constraintDaoSupplierMap.put(ConstraintType.DEFAULT, DefaultConstraintDao::new);
    this.constraintDaoSupplierMap.put(ConstraintType.NUMERIC_RANGE, NumericRangeConstraintDao::new);
    this.constraintDaoSupplierMap.put(ConstraintType.NUMERIC_SCALAR, NumericScalarConstraintDao::new);
    this.constraintDaoSupplierMap.put(ConstraintType.PHASE, PhaseConstraintDao::new);
    this.constraintDaoSupplierMap.put(ConstraintType.STRING, StringConstraintDao::new);
    this.constraintDaoSupplierMap.put(ConstraintType.TIME_OF_DAY_RANGE, TimeOfDayRangeConstraintDao::new);
    this.constraintDaoSupplierMap.put(ConstraintType.TIME_OF_YEAR_RANGE, TimeOfYearRangeConstraintDao::new);
    this.constraintDaoSupplierMap.put(ConstraintType.WILDCARD, WildcardConstraintDao::new);
  }

  @Override
  public ConstraintDao fromCoi(Constraint constraint) {
    Objects.requireNonNull(constraint);
    var operatorDaoConverter = new OperatorDaoConverter();

    var constraintDao = this.constraintDaoSupplierMap.get(constraint.getConstraintType()).get();
    constraintDao.setValue(constraint.getValue());
    constraintDao.setCriterion(constraint.getCriterion());
    constraintDao.setPriority(constraint.getPriority());
    constraintDao.setOperatorDao(operatorDaoConverter.fromCoi(constraint.getOperator()));
    constraintDao.setConfigurationOptionDao(this.configurationOptionDao);

    return constraintDao;
  }

  @Override
  public Constraint toCoi(ConstraintDao constraintDao) {
    Objects.requireNonNull(constraintDao);
    return constraintDao.createConstraint();
  }
}

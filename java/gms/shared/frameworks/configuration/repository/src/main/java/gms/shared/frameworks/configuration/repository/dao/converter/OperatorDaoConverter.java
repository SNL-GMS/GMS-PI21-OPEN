package gms.shared.frameworks.configuration.repository.dao.converter;

import gms.shared.frameworks.configuration.Operator;
import gms.shared.frameworks.configuration.repository.dao.OperatorDao;
import gms.shared.frameworks.utilities.jpa.EntityConverter;

import java.util.Objects;

public class OperatorDaoConverter implements EntityConverter<OperatorDao, Operator> {

  @Override
  public OperatorDao fromCoi(Operator operator) {
    Objects.requireNonNull(operator);
    var operatorDao = new OperatorDao();
    operatorDao.setNegated(operator.isNegated());
    operatorDao.setType(operator.getType());
    return operatorDao;
  }

  @Override
  public Operator toCoi(OperatorDao operatorDao) {
    Objects.requireNonNull(operatorDao);
    return Operator.from(operatorDao.getType(), operatorDao.isNegated());
  }
}


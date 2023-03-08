package gms.shared.frameworks.configuration.repository.dao.converter;

import gms.shared.frameworks.configuration.Operator;
import gms.shared.frameworks.configuration.Operator.Type;
import gms.shared.frameworks.configuration.repository.dao.OperatorDao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperatorDaoConverterTest {

  @Test
  void toCoi() {
    OperatorDaoConverter operatorDaoConverter = new OperatorDaoConverter();
    OperatorDao operatorDao = new OperatorDao();
    operatorDao.setType(Type.EQ);
    operatorDao.setNegated(true);
    Operator oper = operatorDaoConverter.toCoi(operatorDao);

    assertEquals(Type.EQ, oper.getType(), "Operator has wrong type");
    assertTrue(oper.isNegated(), "Operator should be negated");
  }

  @Test
  void fromCoi() {
    OperatorDaoConverter operatorDaoConverter = new OperatorDaoConverter();

    Operator oper = Operator.from(Type.EQ, true);
    OperatorDao operatorDao = operatorDaoConverter.fromCoi(oper);

    assertEquals(Type.EQ, operatorDao.getType(), "OperatorDao has wrong type");
    assertTrue(operatorDao.isNegated(), "OperatorDao should be negated");
  }
}
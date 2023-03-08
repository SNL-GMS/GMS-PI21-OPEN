package gms.shared.frameworks.osd.dao.util;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class StagedPrefixNamingStrategy extends PhysicalNamingStrategyStandardImpl {
  @Override
  public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
    return new Identifier(prefix(name.getText()), name.isQuoted());
  }

  private String prefix(String name) {
    return "staged_".concat(name);
  }
}

package gms.shared.utilities.bridge.database;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

class PrefixNamingStrategy implements PhysicalNamingStrategy {

  private final String prefix;

  public PrefixNamingStrategy(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
    return name;
  }

  @Override
  public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
    return name;
  }

  @Override
  public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
    if (!prefix.isBlank() && !prefix.isEmpty()) {
      return Identifier.toIdentifier(prefix.concat(name.getText()));
    } else {
      return name;
    }
  }

  @Override
  public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
    return name;
  }

  @Override
  public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
    return name;
  }
}

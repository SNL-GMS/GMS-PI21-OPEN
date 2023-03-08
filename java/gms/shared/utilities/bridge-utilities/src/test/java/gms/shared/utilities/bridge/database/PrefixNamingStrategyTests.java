package gms.shared.utilities.bridge.database;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrefixNamingStrategyTests {

  @Mock
  JdbcEnvironment jdbcEnvironment;

  @Test
  void testApplyNamingStrategy() {
    var prefix = "TEST_";
    var prefixNamingStrategy = new PrefixNamingStrategy(prefix);

    var id = "ID";
    var identifier = new Identifier(id, false);

    var expectedIdentifier = new Identifier(prefix + id, false);

    assertEquals(identifier, prefixNamingStrategy.toPhysicalCatalogName(identifier, jdbcEnvironment));
    assertEquals(identifier, prefixNamingStrategy.toPhysicalColumnName(identifier, jdbcEnvironment));
    assertEquals(expectedIdentifier, prefixNamingStrategy.toPhysicalTableName(identifier, jdbcEnvironment));
    assertEquals(identifier, prefixNamingStrategy.toPhysicalSchemaName(identifier, jdbcEnvironment));
    assertEquals(identifier, prefixNamingStrategy.toPhysicalSequenceName(identifier, jdbcEnvironment));
  }
}
package gms.shared.utilities.bridge.database;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InPrefixNamingStrategyTests {

  @Mock
  JdbcEnvironment jdbcEnvironment;

  @Test
  void testApplyNamingStrategy() {
    var inPrefixNamingStrategy = new InPrefixNamingStrategy();

    var id = "ID";
    var identifier = new Identifier(id, false);

    var expectedIdentifier = new Identifier(InPrefixNamingStrategy.IN_PREFIX + id, false);

    assertEquals(identifier, inPrefixNamingStrategy.toPhysicalCatalogName(identifier, jdbcEnvironment));
    assertEquals(identifier, inPrefixNamingStrategy.toPhysicalColumnName(identifier, jdbcEnvironment));
    assertEquals(expectedIdentifier, inPrefixNamingStrategy.toPhysicalTableName(identifier, jdbcEnvironment));
    assertEquals(identifier, inPrefixNamingStrategy.toPhysicalSchemaName(identifier, jdbcEnvironment));
    assertEquals(identifier, inPrefixNamingStrategy.toPhysicalSequenceName(identifier, jdbcEnvironment));
  }
}
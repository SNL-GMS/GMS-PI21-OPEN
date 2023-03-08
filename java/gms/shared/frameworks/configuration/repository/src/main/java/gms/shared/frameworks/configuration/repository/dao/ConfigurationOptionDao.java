package gms.shared.frameworks.configuration.repository.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "configuration_option")
@TypeDef(
  name = "json-node",
  typeClass = JsonNodeBinaryType.class
)
public class ConfigurationOptionDao {

  private static final ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configuration_option_sequence")
  @SequenceGenerator(name = "configuration_option_sequence", sequenceName = "configuration_option_sequence", allocationSize = 1)
  private int id;

  @Column(name = "name", nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "configuration_id", nullable = false, updatable = false)
  private ConfigurationDao configurationDao;

  @OneToMany(mappedBy = "configurationOptionDao", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  private Set<ConstraintDao> constraintDaos;

  @Type(type = "json-node")
  @Column(name = "parameters", columnDefinition = "jsonb")
  private JsonNode parameters;

  public ConfigurationOptionDao() {
    // Empty constructor needed for JPA
  }

  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ConfigurationDao getConfigurationDao() {
    return this.configurationDao;
  }

  public void setConfigurationDao(ConfigurationDao configurationDao) {
    this.configurationDao = configurationDao;
  }

  public Set<ConstraintDao> getConstraintDaos() {
    return this.constraintDaos;
  }

  public void setConstraintDaos(Set<ConstraintDao> constraintDaos) {
    this.constraintDaos = constraintDaos;
  }

  public static ObjectMapper getJsonObjectMapper() {
    return ConfigurationOptionDao.jsonObjectMapper;
  }

  public JsonNode getParameters() {
    return this.parameters;
  }

  public void setParameters(JsonNode parameters) {
    this.parameters = parameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    ConfigurationOptionDao that = (ConfigurationOptionDao) o;
    return this.name.equals(that.name) &&
      this.configurationDao.equals(that.configurationDao) &&
      Objects.equals(this.parameters, that.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.configurationDao, this.parameters);
  }
}

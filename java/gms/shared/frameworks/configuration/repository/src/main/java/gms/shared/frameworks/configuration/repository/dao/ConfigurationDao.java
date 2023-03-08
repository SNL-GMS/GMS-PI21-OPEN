package gms.shared.frameworks.configuration.repository.dao;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "configuration")
@NamedEntityGraph(name = "configuration.getConfiguration",
  attributeNodes = {
    @NamedAttributeNode(value = "configurationOptionDaos", subgraph = "constraint.subgraph")
  },
  subgraphs = {
    @NamedSubgraph(
      name = "constraint.subgraph",
      type = ConstraintDao.class,
      attributeNodes = @NamedAttributeNode(value = "constraintDaos")),
  })
public class ConfigurationDao {

  public static final String ENTITY_GRAPH_NAME = "configuration.getConfiguration";

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "configuration_sequence")
  @SequenceGenerator(name = "configuration_sequence", sequenceName = "configuration_sequence", allocationSize = 1)
  private int id;

  @Column(name = "name", nullable = false)
  private String name;

  @OneToMany(mappedBy = "configurationDao", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<ConfigurationOptionDao> configurationOptionDaos;

  public ConfigurationDao() {
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

  public Set<ConfigurationOptionDao> getConfigurationOptionDaos() {
    return this.configurationOptionDaos;
  }

  public void setConfigurationOptionDaos(
    Set<ConfigurationOptionDao> configurationOptionDaos) {
    this.configurationOptionDaos = configurationOptionDaos;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || this.getClass() != o.getClass()) {
      return false;
    }
    ConfigurationDao that = (ConfigurationDao) o;
    return this.name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name);
  }
}


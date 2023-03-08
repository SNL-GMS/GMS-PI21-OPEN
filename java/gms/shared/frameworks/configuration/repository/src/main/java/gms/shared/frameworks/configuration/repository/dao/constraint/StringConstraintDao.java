package gms.shared.frameworks.configuration.repository.dao.constraint;

import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.constraints.StringConstraint;
import gms.shared.frameworks.configuration.repository.dao.ConstraintDao;
import gms.shared.frameworks.configuration.repository.dao.converter.OperatorDaoConverter;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.CollectionTable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@DiscriminatorValue("string_constraint")
public class StringConstraintDao extends ConstraintDao {

  @ElementCollection
  @CollectionTable(name = "string_constraint_value",
    joinColumns = @JoinColumn(name = "constraint_id"))
  @LazyCollection(LazyCollectionOption.FALSE)
  private Set<String> value = new HashSet<>();

  @Override
  public Set<String> getValue() {
    return this.value;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setValue(Object value) {
    if (!(value instanceof Set)) {
      throw new IllegalArgumentException("Object passed to StringConstraintDao::setValue is not a Set");
    }
    this.value = (Set<String>) value;
  }

  @Override
  public Constraint createConstraint() {

    return StringConstraint.from(
      this.getCriterion(),
      new OperatorDaoConverter().toCoi(this.getOperatorDao()),
      this.getValue(),
      this.getPriority());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    StringConstraintDao that = (StringConstraintDao) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), value);
  }
}

package gms.shared.frameworks.configuration.repository.dao.constraint;

import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.constraints.WildcardConstraint;
import gms.shared.frameworks.configuration.repository.dao.ConstraintDao;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("wildcard_constraint")
public class WildcardConstraintDao extends ConstraintDao {

  //this isn't used, but it needed to override the abstract method...for Wildcard Constraint, the value is always '*'
  @Override
  public String getValue() {
    return "*";
  }

  @Override
  public Constraint createConstraint() {
    return WildcardConstraint.from(
      this.getCriterion());
  }
}

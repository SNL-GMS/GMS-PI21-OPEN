package gms.shared.frameworks.configuration.repository.dao.constraint;

import gms.shared.frameworks.configuration.Constraint;
import gms.shared.frameworks.configuration.constraints.DefaultConstraint;
import gms.shared.frameworks.configuration.repository.dao.ConstraintDao;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("default_constraint")
public class DefaultConstraintDao extends ConstraintDao {

  //value variable isn't used, but it needed to override the abstract method...for Default Constraint, the value is always '-'
  @Override
  public String getValue() {
    return "-";
  }

  @Override
  public Constraint createConstraint() {
    return DefaultConstraint.from();
  }
}

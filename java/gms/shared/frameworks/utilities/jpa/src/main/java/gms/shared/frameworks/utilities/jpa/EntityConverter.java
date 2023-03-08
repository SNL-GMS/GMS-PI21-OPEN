package gms.shared.frameworks.utilities.jpa;


import org.apache.commons.lang3.NotImplementedException;

import javax.persistence.EntityManager;

public interface EntityConverter<E, C> {

  default E fromCoi(C coi) {
    throw new NotImplementedException("fromCoi(C coi) has not been implemented");
  }

  default E fromCoi(C coi, EntityManager entityManager) {
    throw new NotImplementedException(
      "fromCoi(C coi, EntityManager entityManager) has not been implemented");
  }

  C toCoi(E entity);
}

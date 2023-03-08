package gms.shared.frameworks.osd.repository.utils;

import gms.shared.frameworks.osd.api.util.RepositoryExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

/**
 * A utility for making some common JPA operations easier.
 *
 * @param <COI> the type of the COI class
 * @param <DAO> the type of the DAO class
 */
public class RepositoryUtility<COI, DAO> {

  private static final Logger logger =
    LoggerFactory.getLogger(RepositoryUtility.class);

  private final Class<DAO> daoClass;
  private final Function<COI, DAO> coiToDao;
  private final Function<DAO, COI> daoToCoi;

  private RepositoryUtility(Class<DAO> daoClass,
    Function<COI, DAO> coiToDao,
    Function<DAO, COI> daoToCoi) {
    this.daoClass = Objects.requireNonNull(daoClass);
    this.coiToDao = Objects.requireNonNull(coiToDao);
    this.daoToCoi = Objects.requireNonNull(daoToCoi);
  }

  /**
   * Create {@link RepositoryUtility}.
   *
   * @param daoClass the class of the DAO
   * @param coiToDao a function from the COI to the DAO, used to convert a COI object into a DAO
   * before persisting
   * @param daoToCoi a function from the DAO to the COI, used to convert queried results into the
   * desired form (the COI).
   * @param <COI> type of the COI
   * @param <DAO> type of the DAO
   * @return a {@link RepositoryUtility}
   */
  public static <COI, DAO> RepositoryUtility<COI, DAO> create(
    Class<DAO> daoClass, Function<COI, DAO> coiToDao,
    Function<DAO, COI> daoToCoi) {
    return new RepositoryUtility<>(daoClass, coiToDao, daoToCoi);
  }

  /**
   * Saves an object.
   *
   * @param objs the objects to persist
   */
  public void persist(Collection<COI> objs, EntityManager entityManager) {
    Objects.requireNonNull(objs);
    try {
      entityManager.getTransaction().begin();
      for (COI coi : objs) {
        entityManager.persist(coiToDao.apply(coi));
      }
      entityManager.getTransaction().commit();
    } catch (Exception e) {
      entityManager.getTransaction().rollback();
      throw RepositoryExceptionUtils.wrapWithContext("Error committing transaction: " + e.getMessage(), e);
    }
  }

}

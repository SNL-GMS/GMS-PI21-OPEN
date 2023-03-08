package gms.shared.event.repository.connector;

import gms.shared.event.dao.NetMagDao;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Manages querying {@link NetMagDao} from the database
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NetMagDatabaseConnector {

  private final EntityManager entityManager;

  protected NetMagDatabaseConnector(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * Retrieves a list of {@link NetMagDao}s from the database with the specified orid
   * @param orid to query NetMagDaos for
   * @return a list of associated NetMagDaos
   */
  public List<NetMagDao> findNetMagByOrid(long orid) {
    var criteriaBuilder = entityManager.getCriteriaBuilder();
    var cbQuery = criteriaBuilder.createQuery(NetMagDao.class);
    var fromNetMag = cbQuery.from(NetMagDao.class);

    cbQuery.select(fromNetMag).where(criteriaBuilder.equal(fromNetMag.get("originId"), orid));

    return entityManager.createQuery(cbQuery).getResultList();
  }
}

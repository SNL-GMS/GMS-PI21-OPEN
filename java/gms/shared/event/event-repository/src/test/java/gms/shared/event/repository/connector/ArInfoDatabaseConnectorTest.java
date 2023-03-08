package gms.shared.event.repository.connector;

import gms.shared.event.dao.ArInfoDao;
import gms.shared.signaldetection.dao.css.AridOridKey;
import gms.shared.signaldetection.dao.css.AssocDao;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArInfoDatabaseConnectorTest extends DatabaseConnectorTest<ArInfoDatabaseConnector> {

  @Override
  protected ArInfoDatabaseConnector getDatabaseConnector(EntityManager entityManager) {
    return new ArInfoDatabaseConnector(entityManager);
  }

  @Test
  void testFindArInfoByOridIdAndAridId() {

    var queriedArInfo = databaseConnector.findArInfoByOridAndArid(11111L, 22222L);
    assertTrue(queriedArInfo.isPresent());

    queriedArInfo = databaseConnector.findArInfoByOridAndArid(23423L, 2342);
    assertTrue(queriedArInfo.isEmpty());

  }

  @Test
  void testFindArInfosByAssocs() {
    var assocDao1 = mock(AssocDao.class);
    var assocDao1Key = new AridOridKey();
    assocDao1Key.setOriginId(11111);
    assocDao1Key.setArrivalId(22222);
    when(assocDao1.getId()).thenReturn(assocDao1Key);

    var assocDao2 = mock(AssocDao.class);
    var assocDao2Key = new AridOridKey();
    assocDao2Key.setOriginId(11111);
    assocDao2Key.setArrivalId(33333);
    when(assocDao2.getId()).thenReturn(assocDao2Key);

    //this is not in the database!
    var assocDao3 = mock(AssocDao.class);
    var assocDao3Key = new AridOridKey();
    assocDao3Key.setOriginId(76575675);
    assocDao3Key.setArrivalId(4329);
    when(assocDao3.getId()).thenReturn(assocDao3Key);


    var queriedArInfo = databaseConnector.findArInfosByAssocs(List.of(assocDao1, assocDao2, assocDao3, assocDao1));
    assertEquals(2, queriedArInfo.entrySet().size());

    // test try/catch
    var entityManagerFactory = mock(EntityManagerFactory.class);
    var entityManager = mock(EntityManager.class);
    var criteriaBuilder = mock(CriteriaBuilder.class);
    var criteriaQuery = mock(CriteriaQuery.class);
    var fromBase = mock(Root.class);
    var predicate = mock(Predicate.class);
    var path = mock(Path.class);
    when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
    when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
    when(criteriaBuilder.createQuery(ArInfoDao.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(ArInfoDao.class)).thenReturn(fromBase);
    when(criteriaBuilder.and(any(), any(), any())).thenReturn(predicate);
    when(criteriaBuilder.or(any())).thenReturn(predicate);
    when(criteriaQuery.select(any())).thenReturn(criteriaQuery);
    when(fromBase.get("originIdArrivalIdKey")).thenReturn(path);
    var arInfoDatabaseConnector = new ArInfoDatabaseConnector(entityManager);
    var exception = new IllegalArgumentException("Test exception");
    when(entityManager.createQuery(criteriaQuery)).thenThrow(exception);
    assertEquals(Collections.emptyMap(), arInfoDatabaseConnector.findArInfosByAssocs(List.of(assocDao1)));

  }

}

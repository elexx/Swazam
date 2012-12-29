package swa.swazam.server.dao.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import swa.swazam.server.dao.HistoryDao;
import swa.swazam.server.entity.Request;

@Repository
public class HistoryDaoImpl implements HistoryDao {

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
	this.entityManager = entityManager;
    }

    @Transactional
    public List<Request> getAllRequestedRequestsFromUser(String username) {
	Query query = entityManager.createQuery("SELECT Request r WHERE r.requestor="+username);
	return (List<Request>) query.getResultList();
    }


    @Override
    public List<Request> getAllSolvedRequestsFromUser(String username) {
	Query query = entityManager.createQuery("SELECT Request r WHERE r.solver="+username);
	return (List<Request>) query.getResultList();
    }

}

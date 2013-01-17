package swa.swazam.server.dao.impl;

import java.util.List;
import java.util.UUID;

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
    	Query query = entityManager.createQuery("SELECT r FROM Request r WHERE r.requestor='" + username + "'");
    	return (List<Request>) query.getResultList();
    }


    @Transactional
    public List<Request> getAllSolvedRequestsFromUser(String username) {
    	Query query = entityManager.createQuery("SELECT r FROM Request r WHERE r.solver='" + username + "'");
    	return (List<Request>) query.getResultList();
    }

	@Transactional
	public boolean save(Request request) {
		Request alreadyExists = find(request.getUuid());
		if(alreadyExists != null)
			return false;
		
		entityManager.persist(request);
		return true;
	}

	@Transactional
	public boolean update(Request request) {
		Request alreadyExists = find(request.getUuid());
		if(alreadyExists == null)
			return false;
		
		alreadyExists.setStatus(request.isStatus());
		alreadyExists.setArtist(request.getArtist());
		alreadyExists.setSong(request.getSong());
		alreadyExists.setSolver(request.getSolver());
		entityManager.merge(alreadyExists);
		return true;
	}

	@Override
	public Request find(String requestUUID) {
		return entityManager.find(Request.class, requestUUID);
	}

}

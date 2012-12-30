package swa.swazam.server.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import swa.swazam.server.dao.HistoryDao;
import swa.swazam.server.entity.Request;
import swa.swazam.server.service.HistoryService;

@Service
public class HistoryServiceImpl implements HistoryService {

	@Autowired
	private HistoryDao historyDao;
	
	@Override
	public List<Request> getAllRequestedRequestsFromUser(String username) {
		return historyDao.getAllRequestedRequestsFromUser(username);
	}

	@Override
	public List<Request> getAllSolvedRequestsFromUser(String username) {
		return historyDao.getAllSolvedRequestsFromUser(username);
	}

	@Override
	public boolean saveOrUpdateRequest(Request request) {
		Request found = historyDao.find(request.getUuid());
		if(found == null)
			return historyDao.save(request);
		
		request.setStatus(true);
		return historyDao.update(request);
	}
}

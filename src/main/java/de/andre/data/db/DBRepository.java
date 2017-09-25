package de.andre.data.db;

import java.time.LocalDateTime;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

@Repository
public class DBRepository {
    @PersistenceContext
    private EntityManager entityManager;
    
    @Transactional
    public DBExperiment save(DBExperiment exp) {
	exp.setStartTime(LocalDateTime.now());
	entityManager.persist(exp);
	return exp;
    }

    @Transactional
    public void save(DBExperimentParameter param) {
	entityManager.persist(param);
    }

    @Transactional
    public void save(DBSnapshot snapshot) {
	snapshot.setTimestamp(LocalDateTime.now());
	entityManager.persist(snapshot);
    }
    
}

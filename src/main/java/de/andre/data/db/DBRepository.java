package de.andre.data.db;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

@Repository
public class DBRepository {

    @PersistenceContext
    private EntityManager entityManager;

    
}

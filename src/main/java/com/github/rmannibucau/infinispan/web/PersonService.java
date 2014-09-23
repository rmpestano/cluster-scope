package com.github.rmannibucau.infinispan.web;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;

/**
 * Created by rmpestano on 9/22/14.
 */
@Stateless
public class PersonService implements Serializable {

    @PersistenceContext
    EntityManager em;


    public List<Person> list(){
          return em.createQuery("select p from Person p").getResultList();
    }

    public void save(Person p){
        em.persist(p);
    }




}

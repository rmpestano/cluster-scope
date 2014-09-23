package com.github.rmannibucau.infinispan.web;

import com.github.rmannibucau.infinispan.api.ClusterScoped;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmpestano on 9/19/14.
 */

@Named
@ClusterScoped
public class ClusterBean implements Serializable {

    private List<Person> persons;
    private String name;
    private Logger log = Logger.getLogger(ClusterBean.class.getName());
    @PersistenceContext
    EntityManager em; //no issues

    //@Inject
    //PersonService personService; //see https://github.com/rmpestano/cluster-scope/issues/1


    @Inject
    Event<SimpleEvent> event;

    @PostConstruct
    public void init() {
        log.info("init cluster bean");//should be called only first time, others nodes will get instance from the first node accessing this bean
        log.info("cache miss");
        //personService = BeanManagerController.getBeanByType(PersonService.class); //also doesnt work
        //persons = personService.list();
        persons = new ArrayList<>();

    }

    public List<Person> getPersons() {
        return persons;
    }

    public void addPerson()  {
        if (name != null) {
            Person p = new Person(name);
            if (!persons.contains(p)) {
                //personService.save(p);
                persons.add(p);
                event.fire(new SimpleEvent(name));
            }
            name = null;
        } else {
            log.info("null item");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void observe(@Observes SimpleEvent simpleEvent) throws InterruptedException {
        persons.add(simpleEvent.getPerson());
    }
}

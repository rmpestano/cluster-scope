package com.github.rmannibucau.infinispan.web;

import com.github.rmannibucau.infinispan.api.ClusterScoped;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmpestano on 9/19/14.
 */

@Named
@ClusterScoped
public class ClusterBean implements Serializable {

    private List<String> itens;
    private String item;
    private Logger log = Logger.getLogger(ClusterBean.class.getName());

    @PostConstruct
    public void init() {
        log.info("init cluster bean");//should be called only first time, others nodes will get instance from the first node accessing this bean
        log.info("cache miss");
        itens = new ArrayList<>();
    }

    public List<String> getItens() {
        return itens;
    }

    public void addItem() {
        if (item != null) {
            getItens().add(item);
            item = null;
        }    else{
            log.info("null item");
        }
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }
}

package com.github.rmannibucau.infinispan.internal;

import com.github.rmannibucau.infinispan.api.ClusterScoped;
import org.infinispan.Cache;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.PassivationCapable;
import java.lang.annotation.Annotation;


/**
 * based on Romain Manni Hazelcast cluster scope:
 *
 * https://github.com/rmannibucau/cluster-scope
 */
public class ClusterContext implements Context {
    private ClusterScopeExtension infini;

    @Override
    public Class<? extends Annotation> getScope() {
        return ClusterScoped.class;
    }

    @Override
    public <T> T get(final Contextual<T> bean) {
        final Cache<String, ContextualInstanceInfo<?>> contextMap = infini().getInfinispanStorage();
        final ContextualInstanceInfo<T> contextualInstanceInfo = (ContextualInstanceInfo<T>) contextMap.get(getBeanKey(bean));
        if (contextualInstanceInfo == null) {
            return null;
        }
        infini.getInfinispanStorage().replace(getBeanKey(bean).toString(), contextualInstanceInfo);
        return contextualInstanceInfo.getContextualInstance();
    }

    @Override
    public <T> T get(final Contextual<T> bean, final CreationalContext<T> creationalContext) {
        if (creationalContext == null) {
            return null;
        }
        if (!PassivationCapable.class.isInstance(bean)) {
            throw new IllegalStateException(bean + " doesn't implement " + PassivationCapable.class.getName());
        }

        final String beanKey = getBeanKey(bean);
        final Cache<String, ContextualInstanceInfo<?>> distributedMap = infini().getInfinispanStorage();
        ContextualInstanceInfo<T> instanceInfo = (ContextualInstanceInfo<T>) distributedMap.get(beanKey.toString());
        if (instanceInfo == null) {
            instanceInfo = new ContextualInstanceInfo<T>();
            final ContextualInstanceInfo<T> old = (ContextualInstanceInfo<T>) distributedMap.putIfAbsent(beanKey.toString(), instanceInfo);
            if (old != null) {
                instanceInfo = old;
            }
        }

        T instance = instanceInfo.getContextualInstance();
        if (instance == null) {
            instance = bean.create(creationalContext);
            instanceInfo.setContextualInstance(instance);
            instanceInfo.setCreationalContext(creationalContext);
        }
        infini.getInfinispanStorage().put(getBeanKey(bean).toString(), instanceInfo);
        return instance;
    }

    private ClusterScopeExtension infini() {
        if (infini == null) {
            infini = BeanManagerController.getBeanByType(ClusterScopeExtension.class);
        }
        return infini;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    public static <T> String getBeanKey(final Contextual<T> bean) {
        return PassivationCapable.class.cast(bean).getId();
    }
}

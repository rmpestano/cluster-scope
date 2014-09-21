package com.github.rmannibucau.infinispan.internal;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import java.io.IOException;

public class ClusterScopeExtension implements Extension {
    private EmbeddedCacheManager instance;

    void addClusterScope(final @Observes AfterBeanDiscovery afb, final BeanManager bm) {
        afb.addContext(new ClusterContext());
    }

    void removeScope(final @Observes BeforeShutdown bs) {
        if (instance != null) {
            instance.stop();
        }
    }

    public Cache<String, ContextualInstanceInfo<?>> getInfinispanStorage() {
        try {
            ensureInstance();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return instance.getCache("beanCache");
    }

    private void ensureInstance() throws IOException {
        if (instance == null) {
            instance = new DefaultCacheManager(GlobalConfigurationBuilder.
                    defaultClusteredBuilder().transport().
                    defaultTransport().addProperty("configurationFile", "jgroups-udp.xml").
                    build(), new ConfigurationBuilder().clustering().
                    cacheMode(CacheMode.REPL_SYNC).
//                    expiration().lifespan(30,TimeUnit.MINUTES).
                    build());
        }
    }
}

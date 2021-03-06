package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.services.CouchbaseServiceRegistry;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.services.ServiceRegistryExecutionPlan;
import org.apereo.cas.services.ServiceRegistryExecutionPlanConfigurer;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * This is {@link CouchbaseServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("couchbaseServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CouchbaseServiceRegistryConfiguration implements ServiceRegistryExecutionPlanConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Service registry couchbase client factory couchbase client factory.
     *
     * @return the couchbase client factory
     */
    @RefreshScope
    @Bean
    public CouchbaseClientFactory serviceRegistryCouchbaseClientFactory() {
        val couchbase = casProperties.getServiceRegistry().getCouchbase();
        val nodes = StringUtils.commaDelimitedListToSet(couchbase.getNodeSet());
        return new CouchbaseClientFactory(nodes, couchbase.getBucket(),
            couchbase.getPassword(),
            Beans.newDuration(couchbase.getTimeout()).toMillis(),
            CouchbaseServiceRegistry.UTIL_DOCUMENT,
            CouchbaseServiceRegistry.ALL_VIEWS);
    }

    @Bean
    @RefreshScope
    public ServiceRegistry couchbaseServiceRegistry() {
        return new CouchbaseServiceRegistry(serviceRegistryCouchbaseClientFactory(), new DefaultRegisteredServiceJsonSerializer());
    }

    @Override
    public void configureServiceRegistry(final ServiceRegistryExecutionPlan plan) {
        plan.registerServiceRegistry(couchbaseServiceRegistry());
    }
}

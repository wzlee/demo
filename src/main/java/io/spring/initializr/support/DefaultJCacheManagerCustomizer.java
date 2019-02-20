package io.spring.initializr.support;

import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;

/**
 *
 * @author lizhiwei
 * 2019-01-28 14:02:53
 */
public class DefaultJCacheManagerCustomizer implements JCacheManagerCustomizer {

	public final static String SALT_TOEKN_CACHE_KEY = "salt.token";
	
	@Autowired
	CacheManager cacheManager;
	
	@Override
	public void customize(CacheManager cacheManager) {
		cacheManager.createCache(SALT_TOEKN_CACHE_KEY, config());
		cacheManager.createCache("initializr.metadata", config().setExpiryPolicyFactory(
						CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES)));
		cacheManager.createCache("initializr.dependency-metadata", config());
		cacheManager.createCache("initializr.project-resources", config());
	}

	private MutableConfiguration<Object, Object> config() {
		return new MutableConfiguration<>().setStoreByValue(false)
				.setManagementEnabled(true).setStatisticsEnabled(true);
	}
}

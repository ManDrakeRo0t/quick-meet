//package ru.bogatov.quickmeet.config.cache;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cache.annotation.CachingConfigurerSupport;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//
//import java.time.Duration;
//
//
//@Configuration
//public class RedisConfig extends CachingConfigurerSupport {
//
//    @Value("${spring.redis.host}")
//    private String redisHost;
//    @Value("${spring.redis.port}")
//    private int redisPort;
//    @Value("${spring.redis.cache-ttl}")
//    private int cacheTtl;
//
////    @Bean
////    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
////        return RedisCacheManager.create(connectionFactory);
////    }
////
////    @Bean
////    public LettuceConnectionFactory redisConnectionFactory() {
////        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
////        redisStandaloneConfiguration.setHostName(this.redisHost);
////        redisStandaloneConfiguration.setPort(this.redisPort);
////        return new LettuceConnectionFactory(redisStandaloneConfiguration);
////    }
////
////    @Bean
////    RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
////        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
////        redisTemplate.setConnectionFactory(cf);
////        return redisTemplate;
////    }
//
////    @Bean
////    public RedisCacheConfiguration cacheConfiguration() {
////        return RedisCacheConfiguration.defaultCacheConfig()
////                .entryTtl(Duration.ofMinutes(this.cacheTtl));
////    }
//
//}

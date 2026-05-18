package cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cache.entity")
public class CacheProperties{
        private Long ttlMinutes = 30L;       // cache entry TTL
        private Long tagTtlMinutes = 35L;    // tag set TTL (slightly longer than entry TTL)
}
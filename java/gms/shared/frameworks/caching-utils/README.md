# Utility for utilizing Ignite caches

This utility sets up Ignite caches with appropriate configurations.  

To initialize, call the create method on IgniteCacheFactory

```java
IgniteCacheFactory factory = IgniteCacheFactory.create(List.of(GmsCacheInfo.STATION_SOH));
```

When the factory has been initialized, you can retrieve a cache by name
```java
IgniteCache<String, String> cache = igniteCacheFactory.getCache(GmsCacheInfo.STATION_SOH.getCacheName());
```

Key/value pairs can be stored and retrieved
```java
cache.put("station", "Terrapin");
cache.get("station");
```

`create` throws `java.lang.IllegalStateException`'s when create is called more than once.
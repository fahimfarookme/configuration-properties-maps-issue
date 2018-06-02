## Issue
In spring-boot 2x `Maps` in `@ConfigurationProperties` are not being updated from the **second** `EnvironmentChangeEvent` onwards. i.e. Following test failed in spring-boot 2x, but passed in spring-boot 1x
```
@ConfigurationProperties
class Props {
   private String str;
   private Map<String, String> map = new HashMap<>();
   ...
}

// first EnvironmentChangeEvent
// 'str-value' gets reflected in @ConfigurationProperties bean - in both spring-boot 1x and 2x
TestPropertyValues.of("str=str-value").applyTo(this.environment);
context.publishEvent(new EnvironmentChangeEvent(..."str"));

// second EnvironmentChangeEvent - updates the map
// 'map-value' NOt getting reflected in @ConfigurationProperties bean - in spring-boot 2x
TestPropertyValues.of("map.key=map-value").applyTo(this.environment);
context.publishEvent(new EnvironmentChangeEvent(..."map.key"));

// fails only in spring-boot 2x	
assertEquals("map-value", context.getBean(Props.class).getMap().get("key")); 
```

## Analysis
* The `MapBinder` [iterates over](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/context/properties/bind/MapBinder.java#L152) `SpringIterableConfigurationPropertySource` in order to bind properties to the map.
* However `SpringIterableConfigurationPropertySource#iterator()` having reference only to initial/ old property-keys. This is because of the following bug in `Cache` implementation;
   - In case of `MapPropertySource`, `SpringIterableConfigurationPropertySource#getCacheKey()` [here](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/context/properties/source/SpringIterableConfigurationPropertySource.java#L146) always returns the same object in the heap (for different invocations), even though the object (i.e. key-set) state has changed. i.e. <br/>
   `return ((MapPropertySource) getPropertySource()).getSource().keySet();`
  
   - As a result, `if (ObjectUtils.nullSafeEquals(cacheKey, this.cacheKey))` [here](https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/context/properties/source/SpringIterableConfigurationPropertySource.java#L136) always evaluates to true and returns the old/ inital cache.
   
## Fix
`SpringIterableConfigurationPropertySource#getCacheKey()` should return a new object. This branch - `if (getPropertySource() instanceof MapPropertySource)` is not necessary.

```
private Object getCacheKey() {
   return getPropertySource().getPropertyNames(); // returns a new array
}
```
Whenever the `cacheKey` is updated, `Cache` is reinitialized (with all the property keys)

## Sample
* PoC [here](https://github.com/fahimfarookme/configuration-properties-maps-issue/tree/master) to confirm/ recreate the issue - the same set of test cases passed [here](https://github.com/fahimfarookme/configuration-properties-maps-issue/tree/master/spring-boot-1x-cloud-1x) (spring-boot 1x), but failed [here](https://github.com/fahimfarookme/configuration-properties-maps-issue/tree/master/spring-boot-2x-cloud-2x) (spring-boot 2x).


This issue supersedes [this one](https://github.com/spring-cloud/spring-cloud-netflix/issues/2538).

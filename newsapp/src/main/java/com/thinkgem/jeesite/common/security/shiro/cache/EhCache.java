package com.thinkgem.jeesite.common.security.shiro.cache;

import net.sf.ehcache.Element;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by wuwenhui on 2017/9/18.
 * 封装ehcache缓存，为两级缓存中的第一级缓存
 *
 * @version 1.0
 */

public class EhCache<K, V> implements LGCache<K,V> {

    private static final  Logger log = LoggerFactory.getLogger(EhCache.class);
    /**
     * ehcache缓存
     */
    private net.sf.ehcache.Ehcache cache;

    public EhCache(net.sf.ehcache.Ehcache cache) {
        if (cache == null) {
            throw new IllegalArgumentException("Cache argument cannot be null.");
        }
        this.cache = cache;
    }


    public V get(K key) throws CacheException {
        try {
            if (log.isTraceEnabled()) {
                log.trace("Getting object from cache [" + cache.getName() + "] for key [" + key + "]");
            }
            if (key == null) {
                return null;
            } else {
                Element element = cache.get(key);
                if (element == null) {
                    if (log.isTraceEnabled()) {
                        log.trace("Element for [" + key + "] is null.");
                    }
                    return null;
                } else {
                    //noinspection unchecked
                    return (V) element.getObjectValue();
                }
            }
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }


    public V put(K key, V value) throws CacheException {
        if (log.isTraceEnabled()) {
            log.trace("Putting object in cache [" + cache.getName() + "] for key [" + key + "]");
        }
        try {
            V previous = get(key);
            Element element = new Element(key, value);
            cache.put(element);
            return previous;
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }


    public V remove(K key) throws CacheException {
        if (log.isTraceEnabled()) {
            log.trace("Removing object from cache [" + cache.getName() + "] for key [" + key + "]");
        }
        try {
            V previous = get(key);
            cache.remove(key);
            return previous;
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }


    public void clear() throws CacheException {
        if (log.isTraceEnabled()) {
            log.trace("Clearing all objects from cache [" + cache.getName() + "]");
        }
        try {
            cache.removeAll();
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    public int size() {
        try {
            return cache.getSize();
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    public Set<K> keys() {
        try {
            @SuppressWarnings({"unchecked"})
            List<K> keys = cache.getKeys();
            if (!CollectionUtils.isEmpty(keys)) {
                return Collections.unmodifiableSet(new LinkedHashSet<K>(keys));
            } else {
                return Collections.emptySet();
            }
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    public Collection<V> values() {
        try {
            @SuppressWarnings({"unchecked"})
            List<K> keys = cache.getKeys();
            if (!CollectionUtils.isEmpty(keys)) {
                List<V> values = new ArrayList<V>(keys.size());
                for (K key : keys) {
                    V value = get(key);
                    if (value != null) {
                        values.add(value);
                    }
                }
                return Collections.unmodifiableList(values);
            } else {
                return Collections.emptyList();
            }
        } catch (Throwable t) {
            throw new CacheException(t);
        }
    }

    public String toString() {
        return "EhCache [" + cache.getName() + "]";
    }

    @Override
    public boolean isValable() {
        return true;
    }
}

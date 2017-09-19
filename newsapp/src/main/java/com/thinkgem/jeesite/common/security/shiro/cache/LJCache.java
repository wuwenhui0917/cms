package com.thinkgem.jeesite.common.security.shiro.cache;

import org.apache.shiro.cache.CacheException;

import java.util.Collection;
import java.util.Set;

/**
 * Created by wuwenhui on 2017/9/18.
 * since 两级缓存处理
 *
 * @version 1.0
 */
public class LJCache<K,V> implements LGCache<K,V> {

    private String cache_name;
    private LGCache<K,V> cache1;
    private LGCache<K,V> cache2;

    public LJCache(String name,LGCache<K,V> cache1,LGCache<K,V> cache2){
        this.cache_name = name;
        this.cache1 = cache1;
        this.cache2 =cache2;
    }
    @Override
    public boolean isValable() {
        return this.cache1.isValable()|this.cache2.isValable();
    }

    @Override
    public V get(K key) throws CacheException {
        //如果第一级缓存是可用的，直接使用第一级缓存
        if(this.cache1.isValable()){
            return this.cache1.get(key);
        }
        //第一级不可用，直接使用第二级
        if(this.cache2.isValable()){
            return this.cache2.get(key);
        }

        return null;
    }

    @Override
    public V put(K key, V value) throws CacheException {
        if(this.cache1.isValable()){
             this.cache1.put(key,value);
        }
        //第一级不可用，直接使用第二级
        if(this.cache2.isValable()){
            this.cache2.put(key,value);
        }
        return null;
    }

    @Override
    public V remove(K key) throws CacheException {
        if(this.cache1.isValable()){
             this.cache1.remove(key);
        }
        //第一级不可用，直接使用第二级
        if(this.cache2.isValable()){
            this.cache2.remove(key);
        }
        return null;
    }

    @Override
    public void clear() throws CacheException {
        if(this.cache1.isValable()){
             this.cache1.clear();
        }
        //第一级不可用，直接使用第二级
        if(this.cache2.isValable()){
            this.cache2.clear();
        }

    }

    @Override
    public int size() {
        if(this.cache1.isValable()){
           return  this.cache1.size();
        }
        //第一级不可用，直接使用第二级
        if(this.cache2.isValable()){
            return this.cache2.size();
        }
        return -1;
    }

    @Override
    public Set<K> keys() {
        if(this.cache1.isValable()){
            return  this.cache1.keys();
        }
        //第一级不可用，直接使用第二级
        if(this.cache2.isValable()){
            return this.cache2.keys();
        }
        return null;
    }

    @Override
    public Collection<V> values() {
        if(this.cache1.isValable()){
            return  this.cache1.values();
        }
        //第一级不可用，直接使用第二级
        if(this.cache2.isValable()){
            return this.cache2.values();
        }
        return null;
    }
}

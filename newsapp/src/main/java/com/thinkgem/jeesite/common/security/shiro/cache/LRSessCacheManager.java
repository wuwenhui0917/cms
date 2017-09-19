package com.thinkgem.jeesite.common.security.shiro.cache;

import org.apache.shiro.ShiroException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.Initializable;


import java.util.Collection;
import java.util.Currency;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wuwenhui on 2017/9/18.
 * 封装两级缓存缓存管理:进程内缓存和进程外缓存
 *
 * @version 1.0
 */
public class LRSessCacheManager implements CacheManager,Initializable, Destroyable {

    /**
     * ehcache缓存管理
     */
    private  net.sf.ehcache.CacheManager ehmanager;
    /**
     * 缓存类型 1-ehcach缓存，2-redis 缓存，3-为混合缓存（先找ehcache缓存，如果找不到则查找redis缓存）
     */
    private String cacheType=null;

    /**自动回复时间*/
    private long checkInterview;

    private int maxJedisCache;

    private Map<String,JedisCache> redisManager = new ConcurrentHashMap<String,JedisCache>();



    private JedisCache getJedisCache(String cacheName){


        JedisCache jc = this.redisManager.get(cacheName);

        if(jc==null){

            if(this.redisManager.keySet().size()>maxJedisCache){
                jc =   new JedisCache(cacheName);
                jc.setCheckInterview(this.checkInterview);

            }
            else {
                jc =   new JedisCache(cacheName);
                jc.setCheckInterview(this.checkInterview);
                this.redisManager.put(cacheName,jc);
            }

            return jc;
        }
        else {
            return this.redisManager.get(cacheName);
        }
    }



    private net.sf.ehcache.Cache  getEhCache(String name){
        net.sf.ehcache.Cache cache = this.ehmanager.getCache(name);
        if(cache==null){
             this.ehmanager.addCache(name);
        }
        return this.ehmanager.getCache(name);
    }


    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {

        System.out.println(">>>>>>>>>>>>"+name);
        //默认为ehcache
        if(this.cacheType==null||"".equals(this.cacheType)){
            if(ehmanager==null){
                throw new CacheException("default cache is ehcache but ehmanager is null");
            }
            return new EhCache(this.getEhCache(name));
        }
        //不等于空时
        else {
            if("1".equals(this.cacheType)){
                return new EhCache(this.getEhCache(name));
            }
            //redis 缓存
            else if("2".equals(this.cacheType)){

                return this.getJedisCache(name);
            }
            else if("3".equals(this.cacheType)){

                return new LJCache(name,this.getJedisCache(name) ,new EhCache(this.getEhCache(name)));
            }
            else {
                return new EhCache(this.getEhCache(name));
            }
        }

    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public net.sf.ehcache.CacheManager getEhmanager() {
        return ehmanager;
    }

    public void setEhmanager(net.sf.ehcache.CacheManager ehmanager) {
        this.ehmanager = ehmanager;
    }

    @Override
    public void destroy() throws Exception {
        this.ehmanager.shutdown();
    }

    @Override
    public void init() throws ShiroException {

    }

    public long getCheckInterview() {
        return checkInterview;
    }

    public void setCheckInterview(long checkInterview) {
        this.checkInterview = checkInterview;
    }

    public void setMaxJedisCache(int maxJedisCache) {
        this.maxJedisCache = maxJedisCache;
    }
}

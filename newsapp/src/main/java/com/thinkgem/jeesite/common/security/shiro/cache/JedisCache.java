package com.thinkgem.jeesite.common.security.shiro.cache;

import com.google.common.collect.Sets;
import com.thinkgem.jeesite.common.utils.JedisUtils;
import com.thinkgem.jeesite.common.utils.SpringContextHolder;
import com.thinkgem.jeesite.common.web.Servlets;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by wuwenhui on 2017/9/18.
 * jedis缓存
 *
 * @version 1.0
 */
public class JedisCache<K, V>   implements LGCache<K,V>{

    private Logger logger = LoggerFactory.getLogger(JedisCache.class);
    //设置状态 0：标示正常，1-标示不正常
    private AtomicInteger it=new AtomicInteger(0);

    private AtomicLong current = new AtomicLong(System.currentTimeMillis());

    private  long checkInterview = 1000l*60;


    //private static JedisPool jedisPool = SpringContextHolder.getBean(JedisPool.class);

    private String cacheKeyName;
    public JedisCache(String name){
        this.cacheKeyName = name;
    }


    @Override
    public V get(K key) throws CacheException {
        if (key == null) {
            return null;
        }

        V v = null;
        HttpServletRequest request = Servlets.getRequest();
        if (request != null) {
            v = (V) request.getAttribute(cacheKeyName+key);
            if (v != null) {
                return v;
            }
        }

        V value = null;
        Jedis jedis = null;
        try {
            jedis = JedisUtils.getResource();
            value = (V)JedisUtils.toObject(jedis.hget(JedisUtils.getBytesKey(cacheKeyName), JedisUtils.getBytesKey(key)));
            logger.debug("get {} {} {}", cacheKeyName, key, request != null ? request.getRequestURI() : "");
        } catch (Exception e) {
            logger.error("get {} {} {}", cacheKeyName, key, request != null ? request.getRequestURI() : "", e);
        } finally {
            JedisUtils.returnResource(jedis);
        }

        if (request != null && value != null){
            request.setAttribute(cacheKeyName, value);
        }

        return value;

    }

    //设置为不可用状态
    private void setDisable(){
       this.it.getAndSet(1);
    }

    //设置为不可用状态
    private void setEnable(){
        this.it.getAndSet(0);
    }



    @Override
    public V put(K key, V value) throws CacheException {
        if (key == null){
            return null;
        }

        Jedis jedis = null;
        try {
            jedis = JedisUtils.getResource();
            jedis.hset(JedisUtils.getBytesKey(cacheKeyName), JedisUtils.getBytesKey(key), JedisUtils.toBytes(value));
            logger.debug("put {} {} = {}", cacheKeyName, key, value);
        } catch (Exception e) {
//            this.setDisable();
            logger.error("put {} {}", cacheKeyName, key, e);
        } finally {
            JedisUtils.returnResource(jedis);
        }
        return value;
    }

    @Override
    public V remove(K key) throws CacheException {
        V value = null;
        Jedis jedis = null;
        try {
            jedis = JedisUtils.getResource();
            value = (V)JedisUtils.toObject(jedis.hget(JedisUtils.getBytesKey(cacheKeyName), JedisUtils.getBytesKey(key)));
            jedis.hdel(JedisUtils.getBytesKey(cacheKeyName), JedisUtils.getBytesKey(key));
            logger.debug("remove {} {}", cacheKeyName, key);
        } catch (Exception e) {
//            this.setDisable();
            logger.warn("remove {} {}", cacheKeyName, key, e);
        } finally {
            JedisUtils.returnResource(jedis);
        }
        return value;
    }

    @Override
    public void clear() throws CacheException {
        Jedis jedis = null;
        try {
            jedis = JedisUtils.getResource();
            jedis.hdel(JedisUtils.getBytesKey(cacheKeyName));
            logger.debug("clear {}", cacheKeyName);

        } catch (Exception e) {
//            this.setDisable();
            logger.error("clear {}", cacheKeyName, e);
        } finally {
            JedisUtils.returnResource(jedis);
        }

    }

    @Override
    public int size() {
        int size = 0;
        Jedis jedis = null;
        try {
            jedis = JedisUtils.getResource();
            size = jedis.hlen(JedisUtils.getBytesKey(cacheKeyName)).intValue();
            logger.debug("size {} {} ", cacheKeyName, size);
            return size;
        } catch (Exception e) {
//            this.setDisable();
            logger.error("clear {}",  cacheKeyName, e);
        } finally {
            JedisUtils.returnResource(jedis);
        }
        return size;
    }

    @Override
    public Set<K> keys() {
        Set<K> keys = Sets.newHashSet();
        Jedis jedis = null;
        try {
            jedis = JedisUtils.getResource();
            Set<byte[]> set = jedis.hkeys(JedisUtils.getBytesKey(cacheKeyName));
            for(byte[] key : set){
                Object obj = (K)JedisUtils.getObjectKey(key);
                if (obj != null){
                    keys.add((K) obj);
                }
            }
            logger.debug("keys {} {} ", cacheKeyName, keys);
            return keys;
        } catch (Exception e) {
//            this.setDisable();
            logger.error("keys {}", cacheKeyName, e);
        } finally {
            JedisUtils.returnResource(jedis);
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
//        Collection<V> vals = Collections.emptyList();;
        List vals = new ArrayList();
        Jedis jedis = null;
        try {
            jedis = JedisUtils.getResource();
            Collection<byte[]> col = jedis.hvals(JedisUtils.getBytesKey(cacheKeyName));
            for(byte[] val : col){
                Object obj = JedisUtils.toObject(val);
                if (obj != null){
                    vals.add((V) obj);
                }
            }
            logger.debug("values {} {} ", cacheKeyName, vals);
            return vals;
        } catch (Exception e) {
//            this.setDisable();
            logger.error("values {}",  cacheKeyName, e);
        } finally {
            JedisUtils.returnResource(jedis);
        }
        return vals;
    }

    @Override
    public boolean isValable() {

        System.out.println("check;;;;;;;;;;;;;;;");

        System.out.println("进入check+   isValable"+this.it.get()+" :当前时间："+System.currentTimeMillis()+" 保存时间为："+this.current.get());

        Jedis jedis = null;
        //判断当前是否正常如果不正常不需要检测，如果不正常时判断是否需要到达回复检测，
        if(this.it.get()!=0){
            long currenttime = System.currentTimeMillis();
            //不需要检测
            if((currenttime-this.current.get())<this.checkInterview){
//                if(logger.isDebugEnabled()){
//                    logger.debug("检测结果为不需要检测");
//                }
                System.out.println("检测结果为不需要检测");
              return false;
            }
        }


        try{
//            if(logger.isDebugEnabled()){
//                logger.debug("redis进入状态判断");
//            }
            System.out.println("redis进入状态判断");
            jedis = JedisUtils.getResource();
            boolean result=  jedis.isConnected();
//            if(logger.isDebugEnabled()){
//                logger.debug("可用状态:"+result);
//            }
            System.out.println("可用状态:"+result);
            //设置为正常状态
            setEnable();
            return true;
        }catch (Exception e){
            this.setDisable();
            return false;
        }
        finally {

            //设置检测时间
            this.current.set(System.currentTimeMillis());
        }

    }

    public long getCheckInterview() {
        return checkInterview;
    }

    public void setCheckInterview(long checkInterview) {
        this.checkInterview = checkInterview;
    }
}

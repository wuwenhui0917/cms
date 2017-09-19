package com.thinkgem.jeesite.common.security.shiro.cache;

import org.apache.shiro.cache.Cache;

/**
 * Created by wuwenhui on 2017/9/18.
 * since
 *
 * @version 1.0
 */
public interface LGCache<K,V> extends Cache<K, V> {

    /**
     * 缓存是否可用
     * @return
     */
    public boolean isValable();
}

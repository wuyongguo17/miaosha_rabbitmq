package com.imooc.miaosha.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {
	@Autowired
	JedisPool jedisPool;
	
	public <T> T get(KeyPrefix prefix,String key,Class<T> clazz) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String realKey = prefix.getPrefix() + key;
			String str = jedis.get(realKey);
			T t = stringToBean(str,clazz);
			return t;
		}finally {
			returnToPool(jedis);
		}
	}
	
	public <T> boolean set(KeyPrefix prefix,String key,T value) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String str = beanToString(value);
			if (str == null || str.length() <= 0) {
				return false;
			}
			String realKey = prefix.getPrefix() + key;
			int expire = prefix.expireSeconds();
			if (expire <= 0) {
				//永不过期
				jedis.set(realKey,str);
			}else {
				jedis.setex(realKey, expire, str);
			}
			
			
			return true;
		}finally {
			returnToPool(jedis);
		}
	}
	
	public <T> boolean exists(KeyPrefix prefix,String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String realKey = prefix.getPrefix() + key;
			Boolean exists = jedis.exists(realKey);
			return exists;
		}finally {
			returnToPool(jedis);
		}
	}
	
	public Long incr(KeyPrefix prefix,String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String realKey;
			if (prefix == null) {
				realKey = key;
			}else {
				realKey = prefix.getPrefix() + key;
			}
			return jedis.incr(realKey);
		}finally {
			returnToPool(jedis);
		}
	}
	
	public Long decr(KeyPrefix prefix,String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String realKey = prefix.getPrefix() + key;
			return jedis.decr(realKey);
		}finally {
			returnToPool(jedis);
		}
	}
	
	private <T> String beanToString(T value) {
		if (value == null) {
			return null;
		}
		
		Class<?> clazz = value.getClass();
		if (clazz == int.class || clazz == Integer.class) {
			return ""+value;
		}else if (clazz == String.class) {
			return (String) value;
		}else if (clazz == long.class || clazz == Long.class) {
			return ""+value;
		}else {
			return JSON.toJSONString(value);
		}
		
	}

	@SuppressWarnings("unchecked")
	private <T> T stringToBean(String str,Class<T> clazz) {
		if (str == null || str.length() <= 0 || clazz == null) {
			return null;
		}
		
		if (clazz == int.class || clazz == Integer.class) {
			return (T) Integer.valueOf(str);
		}else if (clazz == String.class) {
			return (T) str;
		}else if (clazz == long.class || clazz == Long.class) {
			return (T) Long.valueOf(str);
		}else {
			return JSON.toJavaObject(JSON.parseObject(str), clazz);
		}
		
	}
	
	
	private void returnToPool(Jedis jedis) {
		if(jedis != null) {
			//实际上并不会关掉，而是返回连接池里
			jedis.close();
		}
		
	}
	
	/**
	 * 删除
	 * */
	public boolean delete(KeyPrefix prefix, String key) {
		 Jedis jedis = null;
		 try {
			 jedis =  jedisPool.getResource();
			//生成真正的key
			String realKey  = prefix.getPrefix() + key;
			long ret =  jedis.del(key);
			return ret > 0;
		 }finally {
			  returnToPool(jedis);
		 }
	}

}

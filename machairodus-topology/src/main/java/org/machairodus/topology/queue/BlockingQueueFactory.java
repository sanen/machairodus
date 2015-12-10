package org.machairodus.topology.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 阻塞队列工厂类
 * 
 * @author yanghe
 * @date 2015年6月8日 下午4:38:06 
 *
 */
public class BlockingQueueFactory {

	private static BlockingQueueFactory FACTORY;
	private static final Object LOCK = new Object();
	
	private Logger LOG = LoggerFactory.getLogger(BlockingQueueFactory.class);
	
	private ConcurrentMap<String , BlockingQueue<Object>> queueMap;
	
	public static final int DEFAULT_QUEUE_SIZE = 10000;
	
	private BlockingQueueFactory() {
		queueMap = new ConcurrentHashMap<String , BlockingQueue<Object>>();
		
	}
	
	public static BlockingQueueFactory getInstance() {
		if(FACTORY == null) {
			synchronized (LOCK) {
				if(FACTORY == null)
					FACTORY = new BlockingQueueFactory();
				
			}
		}
		
		return FACTORY;
	}
	
	/**
	 * 根据Key获取阻塞队列
	 * 
	 * @param key 队列Key
	 * @return 返回阻塞队列
	 * @throws RuntimeException 运行时异常
	 */
	public BlockingQueue<Object> getQueue(String key) throws RuntimeException {
		if(!queueMap.containsKey(key)) {
			synchronized (LOCK) {
				if(!queueMap.containsKey(key))
					initQueue(key);
			}
			
		}
			
		return queueMap.get(key);
		
	}
	
	/**
	 * 初始化队列
	 * @param key 队列Key
	 * @param size 队列大小
	 */
	public void initQueue(String key , int size) {
		if(size <= 0) {
			throw new IllegalArgumentException("队列大小不能小于等于0, 队列Key: [ " + key + " ]");
		}
		
		BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(size);
		queueMap.put(key, queue);
		LOG.debug("初始化队列: [ " + key + " ] , 大小: [ " + size + " ]");
		
	}
	
	/**
	 * 初始化队列
	 * @param key 队列Key
	 */
	public void initQueue(String key) {
		BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(DEFAULT_QUEUE_SIZE);
		queueMap.put(key, queue);
		LOG.debug("初始化队列: [ " + key + " ] , 大小: [ " + DEFAULT_QUEUE_SIZE + " ] <默认值>");
		
	}
	
	/**
	 * 向工厂中添加队列
	 * 
	 * @param key 队列Key
	 * @param queue 队列
	 */
	public void setQueue(String key , ArrayBlockingQueue<Object> queue) {
		if(getQueue(key) != null) {
			BlockingQueue<Object> theQueue = getQueue(key);
			theQueue.addAll(queue);
		} else {
			queueMap.put(key, queue);
			
		}
		
	}
	
	/**
	 * 向队列中添加元素
	 * 
	 * @param key 队列Key
	 * @param bean 对象
	 * @throws InterruptedException 中断异常
	 */
	public void put(String key , Object bean) throws InterruptedException {
		getQueue(key).put(bean);
		
	}
	
	/**
	 * 从队列中获取元素
	 * 
	 * @param key 队列Key
	 * @return 返回对象
	 * @throws InterruptedException 中断异常
	 */
	public Object take(String key) throws InterruptedException {
		return getQueue(key).take();
		
	}
	
	/**
	 * 向队列中添加元素
	 * 
	 * @param key 队列Key
	 * @param bean 对象
	 * @return 返回添加结果
	 */
	public boolean offer(String key , Object bean) {
		return getQueue(key).offer(bean);
		
	}
	
	/**
	 * 在设定时间内向队列中添加元素，超时抛出中断异常
	 * 
	 * @param key 队列Key
	 * @param bean 对象
	 * @param time 超时时间
	 * @param unit 时间类型
	 * @return 返回添加结果
	 * @throws InterruptedException 中断异常
	 */
	public boolean offer(String key , Object bean , long time , TimeUnit unit) throws InterruptedException {
		return getQueue(key).offer(bean , time , unit);
		
	}
	
	/**
	 * 从队列中获取元素
	 * @param key 队列key
	 */
	@SuppressWarnings("unchecked")
	public <T> T poll(String key) {
		return (T) getQueue(key).poll();
	}
	
	/**
	 * 在设定时间内从队列中获取元素，超时抛出中断异常
	 * @param key 队列Key
	 * @param time 超时时间
	 * @param unit 时间类型
	 * @return 返回队列元素
	 * @throws InterruptedException 中断异常
	 */
	@SuppressWarnings("unchecked")
	public <T> T poll(String key , long time , TimeUnit unit) throws InterruptedException {
		return (T) getQueue(key).poll(time, unit);
		
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> poll(String key, int size, long time, TimeUnit unit) {
		List<T> batch = new ArrayList<T>();
		try {
			while(batch.size() < size) {
				T value = (T) getQueue(key).poll(time, unit);
				if(value == null)
					break;
				
				batch.add(value);
			}
		} catch(InterruptedException e) { }
		
		return batch;
	}
	
	public static final int howManyElementInQueues() {
		BlockingQueueFactory factory = getInstance();
		if(factory.queueMap != null) {
			if(factory.queueMap.size() == 0)
				return 0;
			
			else {
				AtomicInteger size = new AtomicInteger();
				for(BlockingQueue<Object> queue : factory.queueMap.values()) {
					if(queue != null && queue.size() > 0)
						size.addAndGet(queue.size());
				}

				return size.get();
			}
		} else 
			return 0;
			
	}
	
}

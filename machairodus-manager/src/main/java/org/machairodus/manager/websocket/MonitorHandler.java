package org.machairodus.manager.websocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.StringUtils;
import org.machairodus.commons.util.RedisClientNames;
import org.machairodus.commons.util.ResponseStatus;
import org.nanoframework.extension.websocket.AbstractWebSocketHandler;
import org.nanoframework.extension.websocket.ChannelGroupSupport;
import org.nanoframework.extension.websocket.WebSocket;
import org.nanoframework.orm.jedis.GlobalRedisClient;
import org.nanoframework.orm.jedis.RedisClient;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

@WebSocket(value = "MonitorHandler", portProperty = "websocket.monitor.tps.port", sslProperty = "websocket.monitor.tps.ssl", locationProperty = "websocket.monitor.tps.location")
public class MonitorHandler extends AbstractWebSocketHandler {
	private static final ConcurrentMap<String, Map<String, String>> requestMap = new ConcurrentHashMap<>();
	private Map<ChannelHandlerContext, String> ctxMapper = new HashMap<>(); 
	private RedisClient MANAGER = GlobalRedisClient.get(RedisClientNames.MANAGER.value());
	
	@Override
	public void exec(ChannelHandlerContext ctx, WebSocketFrame frame) {
		String value = ((TextWebSocketFrame) frame).text();
		if(StringUtils.isNotBlank(value)) {
			Map<String, String> map = JSON.parseObject(value, new TypeReference<Map<String, String>>(){ });
			String key = map.get("sid") + "_" + map.get("server") + "_" + map.get("monitorType");
			String type = map.get("type");
			if("add".equals(type)) {
				requestMap.put(key, map);
				
				ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
				group.add(ctx.channel());
				ChannelGroupSupport support = new ChannelGroupSupport(key, group);
				List<ChannelGroupSupport> supports = new ArrayList<>();
				supports.add(support);
				ChannelGroupSupport.GROUP.put(key, supports);
				ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(ResponseStatus.OK)));
				ctxMapper.put(ctx, key);
				
				MANAGER.hset(map.get("uid") + "_" + map.get("monitorType"), map.get("server"), map);
			} else if("remove".equals(type)) {
				requestMap.remove(key, map);
				ChannelGroupSupport.GROUP.remove(key);
				MANAGER.hdel(map.get("uid") + "_" + map.get("monitorType"), map.get("server"));
				
			}
		} else {
			ctx.channel().writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(ResponseStatus.FAIL)));
		}
		
	}
	
	public static final Map<String, String> get(String key) {
		return requestMap.get(key);
	}
	
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		String key = ctxMapper.get(ctx);
		if(StringUtils.isNotBlank(key))
			ChannelGroupSupport.GROUP.remove(key);
		
	}
	
}

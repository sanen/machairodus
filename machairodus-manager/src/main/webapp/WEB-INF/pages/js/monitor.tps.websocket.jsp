<%@ page language="java" contentType="application/javascript; charset=UTF-8" pageEncoding="UTF-8"%>
var Chat = {};
Chat.socket = null;

Chat.connect = (function(host) {
	if ('WebSocket' in window) {
		Chat.socket = new WebSocket(host);
	} else if ('MozWebSocket' in window) {
		Chat.socket = new MozWebSocket(host);
	} else {
		console.log('Error: WebSocket is not supported by this browser.');
		return;
	}

	Chat.socket.onopen = function() { 
		
	};

	Chat.socket.onclose = function() { 
		try { Chat.initialize(); } catch(error) { }
	};

	Chat.socket.onmessage = function(message) {
		var json = JSON.parse(message.data);
		Monitor.TPS.receiver(json);
	};
});

Chat.initialize = function() {
	Chat.connect('<%=request.getAttribute("url") %>');
};

Chat.sendMessage = function(id, server, type, sendMessage_count) {
	if(server && type) {
		if(server.indexOf(':') > 0) {
			var map = {
				uid: '<%=request.getAttribute("uid") %>',
				sid: '<%=request.getAttribute("sid") %>', 
				monitorType: 'monitor.tps', 
				id: id,
				server: server, 
				type: type
			};
			
			try { Chat.socket.send(JSON.stringify(map)); } catch(error) { }
		} else {
			if(!sendMessage_count)
				sendMessage_count = 0;
			
			if(sendMessage_count > 100)
				return ;
			
			setTimeout(function() { Chat.sendMessage(id, server, type, ++ sendMessage_count); }, 10);
		}
	} else {
		console.log('Unknown select server');
	}
}

Chat.initialize();

package com.uccc.websocketdemo.spring.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class SocketHandler extends TextWebSocketHandler {

    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;

    private final Logger logger= LoggerFactory.getLogger(this.getClass());

   // private Map<Long, WebSocketSession> sessionPool = new ConcurrentHashMap<>();

    private static CopyOnWriteArraySet<WebSocketSession> webSocketSet = new CopyOnWriteArraySet<WebSocketSession>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        /*final Long userId = (Long) session.getAttributes().get("webSocketUserId");
        logger.debug("userId:{"+userId+"}, connect to the websocket success");
        this.sessionPool.put(userId, session);*/

        webSocketSet.add(session);
        addOnlineCount();           //在线数加1
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,CloseStatus status) throws Exception {
        /*final Long userId = (Long) session.getAttributes().get("webSocketUserId");
        logger.debug("userId:{"+userId+"}, websocket connection closed, close reason:{"+status.getReason()+"}");
        this.sessionPool.remove(userId);*/
        webSocketSet.remove(session);
        addOnlineCount();           //在线数加1
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        /*if(session.isOpen()){
            session.close();
        }
        final Long userId = (Long) session.getAttributes().get("webSocketUserId");
        logger.debug("userId:{"+userId+"}, websocket handleTransportError:{"+exception+"}");
        sessionPool.remove(userId);*/

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {
        /*if (session.isOpen()) {
            final Long userId = (Long) session.getAttributes().get("webSocketUserId");
            logger.debug("userId:{"+userId+"}, websocket send message{"+message.getPayload()+"}");
            session.sendMessage(message);
        }*/
        System.out.println("来自客户端的消息:" + message);

        //群发消息
        for (WebSocketSession item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


   /* private void sendMessageToUsers(final List<Long> userIds, final TextMessage message) {
        try{
            for (Map.Entry<Long, WebSocketSession> entry : this.sessionPool.entrySet()) {
                if (entry.getValue().isOpen()) {
                    final Long userId = (Long) entry.getValue().getAttributes().get("webSocketUserId");
                    logger.debug("userId:{"+userId+"}, websocket send message{"+message.getPayload()+"}");
                    for(long uid : userIds){
                        if(userId == uid){
                            entry.getValue().sendMessage(message);
                        }
                    }
                }
            }
        }catch(IOException e){
            logger.error("websocket message send error:", e);
        }
    }*/

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        SocketHandler.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        SocketHandler.onlineCount--;
    }
}

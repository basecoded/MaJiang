package com.zxz.utils;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import org.apache.mina.common.IoSession;
import org.json.JSONObject;

import com.zxz.domain.Game;
import com.zxz.domain.OneRoom;
import com.zxz.domain.User;
import com.zxz.service.PlayGameService;


public class NotifyTool implements Constant{
	
	public static void notifyUserErrorMessage(IoSession session, String message) {
		session.write(message);
	}
	
	/**消息通知
	 * @param ioSession
	 * @param message
	 */
	public static void notify(IoSession ioSession,String message){
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put("message", message);
		ioSession.write(outJsonObject.toString());
	}
	
	
	/**消息通知
	 * @param ioSession
	 * @param message
	 */
	public static  void notify(IoSession ioSession,JSONObject outJsonObject){
		System.out.println("通知的地址是:"+ioSession.getRemoteAddress());
		ioSession.write(outJsonObject.toString());
	}
	
	/**消息通知
	 * @param room
	 * @param user 
	 * @param message
	 */
	public static void notifyAllUser(OneRoom room,User user, String message){
		List<User> userList = room.getUserList();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("playId",user.getId());
		jsonObject.put("direction", user.getDirection());
		jsonObject.put("description", message);
		for(User u:userList){
			u.getIoSession().write(jsonObject.toString());
		}
	}

	
	
	/**通知sessionList玩家的消息
	 * @param sessionlist
	 * @param jsonObject
	 */
	public static synchronized void  notifyIoSessionList(List<IoSession> sessionlist,JSONObject jsonObject){
		for(int i=0;i<sessionlist.size();i++){
			sessionlist.get(i).write(jsonObject.toString());
		}
	}
	
}

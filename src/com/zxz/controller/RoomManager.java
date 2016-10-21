package com.zxz.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;
import org.json.JSONObject;

import com.zxz.domain.OneRoom;
import com.zxz.service.ServerHandler;
import com.zxz.utils.Constant;
import com.zxz.utils.NotifyTool;


/**
 * ����һ��һ���ķ���,�����ܼ�
 *
 */
public class RoomManager implements Constant{
	private static Logger logger = Logger.getLogger(RoomManager.class);  
	private static Map<String,OneRoom> map = new LinkedHashMap<String, OneRoom>();
	
	public static void addRoomMap(String roomId,OneRoom oneRoom){
		map.put(roomId, oneRoom);
	}
	
	public static Map<String,OneRoom> getRoomMap(){
		return map;
	}
	
	
	/**��ɢ����
	 * @param roomId
	 */
	public static boolean removeOneRoom(String roomId){
		boolean containsKey = map.containsKey(roomId);
		if(containsKey){
			OneRoom oneRoom = map.get(roomId);
			//֪ͨ��������û��������Ѿ���ɢ
			List<IoSession> sessionList = oneRoom.getUserIoSessionList();
			JSONObject outJsonObject = new JSONObject();
			outJsonObject.put(code, success);
			outJsonObject.put(discription,"�����ѽ�ɢ");
			NotifyTool.notifyIoSessionList(sessionList, outJsonObject);
			map.remove(roomId);
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * @param roomId
	 * @return
	 */
	public static OneRoom removeOneRoomByRoomId(String roomId){
		OneRoom oneRoom = null;
		if(map.containsKey(roomId)){
			oneRoom = map.remove(roomId);
			logger.info("RoomManager:"+roomId+" ��ɢ���� ");
		}else{
			logger.fatal("�Ƴ�����ʧ��:"+roomId);
		}
		return oneRoom;
	}
	
	/**���ݷ���ŵõ�����
	 * @param roomId
	 * @return
	 */
	public static OneRoom getRoomWithRoomId(String roomId){
		return map.get(roomId);
	}
	
}
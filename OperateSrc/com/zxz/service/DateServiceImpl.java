package com.zxz.service;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.zxz.controller.GameManager;
import com.zxz.controller.RoomManager;
import com.zxz.domain.Game;
import com.zxz.domain.OneRoom;
import com.zxz.utils.DateUtils;

public class DateServiceImpl implements DateService {

	/**
	 * 总的在线用户
	 */
	private static int totalOnLineUser = 0;
	
	/**
	 *总的登录用户 
	 */
	private static int totalLoginUser = 0;
	
	
	/**
	 * 用户建立连接的时候，增加在线的用户数
	 */
	public void addOnLineUser(){
		totalOnLineUser ++;
	}
	
	
	/**
	 * 用户离线的时候,减少在线的用户数
	 */
	public void subOnLineUser(){
		totalOnLineUser --;
	}
	
	
	/**
	 * 用户登录的时候添加用户数
	 */
	public void addLoginUser(){
		totalLoginUser ++;
	}
	
	/**
	 * 如果用户在登录状态离线的时候减小登录数
	 */
	public void subLoginUser(){
		totalLoginUser --;
	}
	
	@Override
	public int getTotalOneLineUser() {
		return totalOnLineUser;
	}

	@Override
	public int getTotalLoginLineUser() {
		return totalLoginUser;
	}

	@Override
	public JSONObject getGameJSONObject() {
		return null;
	}

	@Override
	public String getRoomJSONObject() {
		Map<String, OneRoom> roomMap = RoomManager.getRoomMap();
		Iterator<String> iterator = roomMap.keySet().iterator();
		JSONArray jsonArray = new JSONArray();
		while(iterator.hasNext()){
			String key = iterator.next();
			OneRoom oneRoom = roomMap.get(key);
			int roomNumber = oneRoom.getRoomNumber();//房间号
			String userName = oneRoom.getCreateUser().getUserName();//房主昵称
			int total = oneRoom.getTotal();//局数
			int zhama = oneRoom.getZhama();//扎码数
			Date createDate = oneRoom.getCreateDate();//创建时间
			String sCreateDate = DateUtils.getFormatDate(createDate, "yyyy/MM/dd hh:mm:ss");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("roomNumber", roomNumber);
			jsonObject.put("userName", userName);
			jsonObject.put("total", total);
			jsonObject.put("zhama", zhama);
			jsonObject.put("createDate", sCreateDate);
			jsonObject.put("createUserId", oneRoom.getCreateUserId());
			jsonArray.put(jsonObject);
		}
		return jsonArray.toString();
	}

}

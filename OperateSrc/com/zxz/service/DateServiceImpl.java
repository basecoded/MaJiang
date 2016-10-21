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
	 * �ܵ������û�
	 */
	private static int totalOnLineUser = 0;
	
	/**
	 *�ܵĵ�¼�û� 
	 */
	private static int totalLoginUser = 0;
	
	
	/**
	 * �û��������ӵ�ʱ���������ߵ��û���
	 */
	public void addOnLineUser(){
		totalOnLineUser ++;
	}
	
	
	/**
	 * �û����ߵ�ʱ��,�������ߵ��û���
	 */
	public void subOnLineUser(){
		totalOnLineUser --;
	}
	
	
	/**
	 * �û���¼��ʱ������û���
	 */
	public void addLoginUser(){
		totalLoginUser ++;
	}
	
	/**
	 * ����û��ڵ�¼״̬���ߵ�ʱ���С��¼��
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
			int roomNumber = oneRoom.getRoomNumber();//�����
			String userName = oneRoom.getCreateUser().getUserName();//�����ǳ�
			int total = oneRoom.getTotal();//����
			int zhama = oneRoom.getZhama();//������
			Date createDate = oneRoom.getCreateDate();//����ʱ��
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

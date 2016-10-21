package com.zxz.service;

import org.apache.mina.common.IoSession;
import org.json.JSONObject;

public abstract class BasePlay implements PlayOfHongZhong{

	
	SettingService settingService = new SettingService();
	ServerService serverService = new ServerService();
	ScoreService scoreService = new ScoreService();
	
	
	@Override
	public void getSetting(JSONObject jsonObject, IoSession session) {
		settingService.getSetting(jsonObject, session);
	}


	@Override
	public void getServerInfo(JSONObject jsonObject, IoSession session) {
		serverService.getServerInfo(jsonObject, session);
	}
	
	@Override
	public void getUserScore(JSONObject jsonObject, IoSession session) {
		scoreService.getUserScore(jsonObject,session);
	}
	
	
	@Override
	public void recommend(JSONObject jsonObject, IoSession session) {
		settingService.recommend(jsonObject,session);
	}
	
	
	@Override
	public void playAudio(JSONObject jsonObject, IoSession session) {
		MessageService messageService = new MessageService();
		messageService.playAudio(jsonObject,session);
	}
	
}

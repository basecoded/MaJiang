package com.zxz.service;

import org.apache.mina.common.IoSession;
import org.json.JSONObject;

public class ServiceMaster {

	JSONObject jsonObject;
	// UserService userService = new UserService();
	// PlayGameService playGameService = new PlayGameService();
	// SettingService settingService = new SettingService();
	// ServerService serverService = new ServerService();
	public ServiceMaster() {
	}
	public ServiceMaster(JSONObject jsonObject) {
		super();
		this.jsonObject = jsonObject;
	}

	public void serviceStart(IoSession session, JSONObject jsonObject) {
		String method = jsonObject.getString("method");
		if (method == null) {
			session.write("method is null");
			return;
		}
		hongZhongMaster(session, jsonObject);
	}

	private void hongZhongMaster(IoSession session, JSONObject jsonObject) {
		String method = jsonObject.getString("method");
		PlayOfHongZhong playOfHongZhong = new UserService();
		switch (method) {
		case "login":
			playOfHongZhong.login(jsonObject, session);// 登录
			break;
		case "createRoom":
			playOfHongZhong.createRoom(jsonObject, session);// 创建房间
			break;
		case "enterRoom":
			playOfHongZhong.enterRoom(jsonObject, session);// 进入房间
			break;
		case "readyGame":
			playOfHongZhong.readyGame(jsonObject, session);// 准备游戏
			break;
		case "playGame":
			playOfHongZhong.playGame(jsonObject, session);// 开始打牌
			break;
		case "disbandRoom":
			playOfHongZhong.disbandRoom(jsonObject, session);// 解散房间
			break;
		case "getMyInfo":
			playOfHongZhong.getMyInfo(jsonObject, session);// 得到我自己的信息
			break;
		case "leaveRoom":
			playOfHongZhong.leaveRoom(jsonObject, session);// 离开房间
			break;
		case "getSettingInfo":
			playOfHongZhong.getSetting(jsonObject, session);// 得到自己的设置信息
			break;
		case "getServerInfo":
			playOfHongZhong.getServerInfo(jsonObject, session);// 得到剩余的牌
			break;
		case "continueGame":
			playOfHongZhong.continueGame(jsonObject, session);// 继续游戏
			break;
		case "settingAuto":
			playOfHongZhong.settingAuto(jsonObject, session);// 设置托管
			break;
		case "cancelAuto":
			playOfHongZhong.cancelAuto(jsonObject, session);// 取消托管
			break;
		case "downGameInfo":
			playOfHongZhong.downGameInfo(jsonObject, session);// 断线重连
			break;
		case "getUserScore":
			playOfHongZhong.getUserScore(jsonObject, session);// 得到战绩
			break;
		case "recommend":
			playOfHongZhong.recommend(jsonObject, session);//保存推荐号
			break;
		case "playAudio":
			playOfHongZhong.playAudio(jsonObject, session);//发送消息
			break;
		}
	}

}

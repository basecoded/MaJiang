package com.zxz.utils;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.zxz.controller.GameManager;
import com.zxz.domain.Game;
import com.zxz.domain.OneRoom;
import com.zxz.domain.User;
import com.zxz.service.PlayGameService;

public class WatchThread implements Runnable{
	
	String nowDirection;//现在的方向
	Game game;
	OneRoom room;
	
	int step;
	
	public WatchThread(String nowDirection,Game game,int step) {
		super();
		this.nowDirection = nowDirection;
		this.game = game;
		this.room = game.getRoom();
		this.step = game.getStep();
	}

	@Override
	public void run() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Map<String, Game> gameMap = GameManager.getGameMap();
				Game nowGame = gameMap.get(room.getId()+"");
				int nowStep = nowGame.getStep();
				Map<String, User> seatMap = nowGame.getSeatMap();
				User user = seatMap.get(nowDirection);
				if(step==nowStep){//卡死在这
					System.out.println(user.getUserName()+"用户卡死在这,没有出牌");
					int remove = user.autoChuPai();//用户出的牌
					JSONObject outJSONObject = PlayGameService.getChuPaiOutJSONObject(remove, user);
					nowGame.setStep(nowStep++);//游戏步数+1
					NotifyTool.notifyIoSessionList(GameManager.getSessionListWithRoomNumber(user.getRoomId()), outJSONObject);
					nowDirection = PlayGameService.getNextDirection(nowDirection);
					PlayGameService.analysis(remove, user, nowGame);//分析下一个人是否可以抓牌或杠牌,然后进一步计算
				}
			}
		}, 1000);//15秒后检查该玩家是否出牌，如果没有出牌替他出
	}
	
	
	
}

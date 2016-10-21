package com.zxz.utils;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import com.zxz.controller.GameManager;
import com.zxz.domain.Game;
import com.zxz.domain.User;
import com.zxz.service.PlayGameService;

/**
 *听牌或杠牌的线程
 */
public class TingOrGangThread implements Runnable,Constant{

	
	Map<String, Game> gameMap = GameManager.getGameMap();
	int step;
	Game game;
	User user;
	
	public TingOrGangThread(int step,Game game,User user) {
		super();
		this.step = step;
		this.game = game;
		this.user = user;
	}

	@Override
	public void run() {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Game nowGame = gameMap.get(user.getRoomId());
				int nowStep = nowGame.getStep();
				if(step==nowStep){//该用户没有做出选择，没有碰牌和杠牌
					System.out.println("该用户没有做出选择，没有碰牌和杠牌,通知下一个人抓牌.");
					JSONObject outJsonObject = new JSONObject();
//					User nextUser = PlayGameService.nextUserdrawCard(nowGame, game.getSeatMap(), user.getDirection());//下一个用户抓牌
//					outJsonObject.put(dircetion, nextUser.getDirection());
//					outJsonObject.put(discription, "出牌的方向");
//					NotifyTool.notifyIoSessionList(GameManager.getSessionListWithRoomNumber(user.getRoomId()+""), outJsonObject);
//					nowGame.setStep(nowStep++);
//					//开启一个线程监视下一个用户是否出牌
//					WatchThread watchThread = new WatchThread(nextUser.getDirection(), nowGame, nowGame.getStep());//方向改变 
//					new Thread(watchThread).start();
				}
			}
		}, 500);
	}

}

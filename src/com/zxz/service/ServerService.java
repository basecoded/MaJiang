package com.zxz.service;

import java.util.List;
import java.util.Map;

import org.apache.mina.common.IoSession;
import org.json.JSONObject;

import com.zxz.controller.GameManager;
import com.zxz.domain.Game;
import com.zxz.domain.User;
import com.zxz.utils.CardsMap;

public class ServerService {

	
	
	/**得到剩余的牌
	 * @param jsonObject
	 * @param session
	 */
	public void getServerInfo(JSONObject jsonObject, IoSession session) {
		User user = (User) session.getAttribute("user");
		String roomId = user.getRoomId();
		if(roomId==null||"".equals(roomId)){
			session.write("啥都没有");
			return;
		}
		Map<String, Game> gameMap = GameManager.getGameMap();
		Game game = gameMap.get(roomId);
		List<Integer> remainCards = game.getRemainCards();
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put("remainCards", remainCards);
		session.write(outJsonObject.toString());
		
		StringBuffer sb = new StringBuffer("  ");
		for(int i=0;i<remainCards.size();i++){
			Integer card = remainCards.get(i);
			String cardType = CardsMap.getCardType(card);
			sb.append(cardType+"");
		}
		session.write("remainCards:"+sb);
	}
	
}

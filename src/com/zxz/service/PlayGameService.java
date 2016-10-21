package com.zxz.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;
import org.json.JSONArray;
import org.json.JSONObject;

import com.zxz.controller.GameManager;
import com.zxz.controller.RoomManager;
import com.zxz.dao.SumScoreDao;
import com.zxz.dao.UserScoreDao;
import com.zxz.domain.Game;
import com.zxz.domain.GangCard;
import com.zxz.domain.OneRoom;
import com.zxz.domain.Score;
import com.zxz.domain.SumScore;
import com.zxz.domain.User;
import com.zxz.domain.UserScore;
import com.zxz.utils.Algorithm2;
import com.zxz.utils.CardsMap;
import com.zxz.utils.Constant;
import com.zxz.utils.MathUtil;
import com.zxz.utils.NotifyTool;

public class PlayGameService implements Constant{

	private static Logger logger = Logger.getLogger(PlayGameService.class);  
	static UserScoreDao userScoreDao = UserScoreDao.getInstance();
	static SumScoreDao sumScoreDao = SumScoreDao.getInstance();
	
	public void playGame(JSONObject jsonObject, IoSession session) {
		String type = jsonObject.getString("type");//出牌，杠牌，碰牌,胡牌
		if(type.equals("chupai")){//出牌
			chuPai(jsonObject, session);
		}else if(type.equals("peng")){//碰牌
			peng(jsonObject,session);
		}else if(type.equals("gang")){//杠牌
			gang(jsonObject,session);
		}else if(type.equals("fangqi")){//不碰也不杠
			fangqi(jsonObject,session);
		}else if(type.equals("gongGang")){//公杠  也称 明杠
			gongGang(jsonObject,session);
		}else if(type.equals("anGang")){//暗杠
			anGang(jsonObject,session);
		}
	}
	
	/**暗杠
	 * @param jsonObject
	 * @param session
	 */
	private void anGang(JSONObject jsonObject, IoSession session) {
		Game game = getGame(session);
		Integer cardId = jsonObject.getInt("cardId");
		User sessionUser = (User) session.getAttribute("user");
		User user = game.getUserInRoomList(sessionUser.getId());
		List<Integer> cards = user.getCards();
		List<Integer> gangCards = getGangList(cards, cardId);//杠的牌
		//gangCards.add(cardId);
		user.userGangCards(gangCards);
		//记录玩家杠的牌
		user.recordUserGangCards(1, gangCards);
		notifyAllUserAnGang(game, gangCards,user);//通知所有的玩家杠的牌 
		modifyUserScoreForAnGang(game, user);//修改玩家暗杠得分
		//该玩家在抓一张牌 
		userDrawCard(game, user.getDirection());
	}

	
	/**修改玩家暗杠得分 
	 * @param game
	 * @param user //暗杠的用户
	 */
	public static void modifyUserScoreForAnGang(Game game, User user) {
		User anGangUser = game.getUserInRoomList(user.getId());
		anGangUser.addScoreForAnGang();
		List<User> userList = game.getUserList();
		for(int i=0;i<userList.size();i++){
			User u = userList.get(i);
			if(u.getId()!=user.getId()){//非杠牌的玩家减分
				u.reduceScoreForAnGang();
			}
		}
	}

	/**公杠
	 * @param jsonObject
	 * @param session
	 */
	private void gongGang(JSONObject jsonObject, IoSession session) {
		int cardId = jsonObject.getInt("cardId");//得到的那张牌
		User user = (User) session.getAttribute("user");
		Game game = getGame(session);
		User gamingUser = getGamingUser(user.getId(), user.getRoomId());
		List<Integer> pengCards = gamingUser.getPengCards();//用户碰的牌
		List<Integer> removeList = getRemoveList(cardId, pengCards);
		for(int i=0;i<removeList.size();i++){
			Integer revomeCard = removeList.get(i);
			pengCards.remove(revomeCard);
		}
		removeList.add(cardId);
		//从自己的牌中移除公杠的那张牌
		gamingUser.removeCardFromGongGang(cardId);
		//记录玩家杠的牌
		gamingUser.recordUserGangCards(2, removeList);
		notifyAllUserGongGang(game, removeList,user);//通知所有的玩家杠的牌 
		modifyUserScoreForGongGang(game, gamingUser);//修改玩家公杠得分
		//该玩家在抓一张牌 
		userDrawCard(game, user.getDirection());
	}
	
	
	/**得到需要从碰的牌中移除的集合
	 * @param card
	 * @param pengCards
	 * @return
	 */
	public static List<Integer> getRemoveList(int card,List<Integer> pengCards){
		List<Integer> list = new ArrayList<>();
		String cardType = CardsMap.getCardType(card);
		for(int i=0;i<pengCards.size();i++){
			Integer pengCard = pengCards.get(i);
			String pengCardType = CardsMap.getCardType(pengCard);
			if(cardType.equals(pengCardType)){
				list.add(pengCard);
			}
		}
		return list;
	}
	
	
	/**公杠的用户得分
	 * @param game
	 * @param user
	 */
	public static void modifyUserScoreForGongGang(Game game, User user) {
		user.addScoreForMingGang();
		//其它的三个玩家减1分
		List<User> userList = game.getUserList();
		for (int i = 0; i < userList.size(); i++) {
			User u = userList.get(i);
			if(u.getId()!=user.getId()){//非杠牌用户减分
				u.reduceScoreForMingGang();
			}
		}
	}

	/**不碰也不杠
	 * @param jsonObject
	 * @param session
	 */
	private void fangqi(JSONObject jsonObject, IoSession session) {
		User user = (User) session.getAttribute("user");
		setUserGiveUp(user);//用户放弃出牌
		Game game = GameManager.getGameWithRoomNumber(user.getRoomId());
		String beforeTingOrGangDirection = game.getBeforeTingOrGangDirection();
		boolean isCanGiveUp = checkUserIsCanGiveUp(beforeTingOrGangDirection);
		if(!isCanGiveUp){
			NotifyTool.notifyUserErrorMessage(session, "不可以放弃，也轮不到你放弃");
			return;
		}
		String direction = getNextDirection(beforeTingOrGangDirection);
		notifyUserDirectionChange(user, direction);
		userDrawCard(game, direction);
	}
	
	/**检测用户是否可以放弃
	 * @param beforeTingOrGangDirection
	 */
	public boolean checkUserIsCanGiveUp(String beforeTingOrGangDirection){
		if(beforeTingOrGangDirection==null||"".equals(beforeTingOrGangDirection)){
			return false;
		}
		return true;
	}
	
	/**用户放弃出牌
	 * @param user
	 */
	private void setUserGiveUp(User user){
		user.setCanGang(false);
		user.setCanPeng(false);
	}
	
	
	/**杠牌
	 * @param jsonObject
	 * @param session
	 */
	private void gang(JSONObject jsonObject, IoSession session) {
		int cardId = jsonObject.getInt("cardId");
		Game game = getGame(session);
		User sessionUser  = (User) session.getAttribute("user");
		User user = getGamingUser(sessionUser.getId(), sessionUser.getRoomId());//game 中 oneRoom中 的user 得到游戏中的玩家
		boolean isUserCanGang = checkUserIsCanGang(user);
		if(!isUserCanGang){
			notifyUserCanNotPengOrGang(session, 2);
			return;
		}
		user.setUserCanPlay(true);
		Map<String, User> seatMap = game.getSeatMap();
		User u = seatMap.get(user.getDirection());
		List<Integer> cards = u.getCards();
		List<Integer> gangCards = getGangList(cards, cardId);//杠的牌
		u.userGangCards(gangCards);
		gangCards.add(cardId);
		//记录玩家杠的牌
		u.recordUserGangCards(0, gangCards);
		modifyUserScoreForGang(game, u);//修改玩家得分
		notifyAllUserGang(game, gangCards,user);//通知所有的玩家杠的牌 
		//该玩家在抓一张牌 
		userDrawCard(game, user.getDirection());
	}
	
	
	/**得到游戏中的玩家
	 * @param userId
	 * @param roomId
	 */
	public static User getGamingUser(int userId,String roomId){
		Game game = GameManager.getGameWithRoomNumber(roomId);
		List<User> userList = game.getRoom().getUserList();
		for(int i=0;i<userList.size();i++){
			User user = userList.get(i);
			if(user.getId()==userId){
				return user; 
			}
		}
		logger.fatal("未找到游戏中的人................");
		return null;
	}
	

	/**修改放杠和接杠的得分
	 * @param game
	 * @param user 杠牌的用户(接杠)
	 */
	public static void modifyUserScoreForGang(Game game, User user) {
		//杠牌者得分
		User jieGangUser = game.getUserInRoomList(user.getId());
		jieGangUser.addScoreForCommonGang();
		//放杠者减分
		int fangGangUserId = game.getFangGangUser().getId();
		User fangGangUser = game.getUserInRoomList(fangGangUserId);
		fangGangUser.reduceScoreForFangGang();
	}
	
	/**抓牌
	 * @param game
	 * @param direction 当前的方向
	 */
	public static void userDrawCard(Game game,String direction){
		List<Integer> remainCards = game.getRemainCards();
		int lastIndex  =remainCards.size()-1;
		//通知该玩家抓到的牌
		Integer removeCard = remainCards.remove(lastIndex);
		System.out.println("当前的牌还有:"+remainCards);
		User user = game.getSeatMap().get(direction);
		game.setDirec(direction);//把当前出牌的方向改变
		game.setGameStatus(GAGME_STATUS_OF_CHUPAI);//设置成出牌的状态
		boolean isWin = user.zhuaPai(removeCard);//抓牌 
		if(!isWin){//没有赢牌
			userNotWin(game, remainCards, removeCard, user);
		}else{//赢牌
			userWin(game, removeCard, user);
		}
	}

	/**用户没有赢牌
	 * @param game
	 * @param remainCards
	 * @param removeCard
	 * @param user
	 */
	private static void userNotWin(Game game, List<Integer> remainCards, Integer removeCard, User user) {
		if(chouZhuang(remainCards,game)){
			logger.info("臭庄");
			afterChouZhuang(game, user,removeCard);//臭庄之后处理
			return;
		}
		//分析用户是否可以公杠
		user.setUserCanPlay(true);//该用户可以打牌
		List<Integer> gongGangCards = analysisUserIsCanGongGang(removeCard,user);
		if(gongGangCards.size()>0){
			if(user.isAuto()){//如果该玩家托管,首先通知玩家抓到的牌,然后自动帮他公杠掉
				notifyUserDrawDirection(removeCard, user,gongGangCards,0);//通知抓牌的方向
				game.setCanGongGangUser(user);
				game.setGongGangCardId(removeCard);
				autoGongGang(game);
			}else{//如果没有托管
				noticeUserCanGongGang(game, removeCard, user, gongGangCards);//通知用户可以公杠
			}
		}else{
			//分析该用户是否可以暗杠
			List<Integer> anGangCards = isUserCanAnGang(user);
			if(anGangCards.size()==4){//设置出牌的状态为暗杠
				game.setAnGangCards(anGangCards);
				game.setCanAnGangUser(user);
				if(user.isAuto()){
					notifyUserDrawDirection(removeCard, user,anGangCards,1);//通知抓牌的方向
					autoAnGang(game);
				}else{
					game.setGameStatus(GAGME_STATUS_OF_ANGANG);//暗杠
					notifyUserDrawDirection(removeCard, user,anGangCards,1);//通知抓牌的方向
				}
			}else{//玩家不可以暗杠
				notifyUserDrawDirection(removeCard, user,null,-1);//通知抓牌的方向
				if(user.isAuto()){
					autoChuPai(game);//自动出牌
				}
			}
		}
	}
	
	/**自动暗杠
	 * @param game
	 * @param user
	 * @param anGangCards
	 */
	public static void autoAnGang(Game game) {
		User canAnGangUser = game.getCanAnGangUser();
		List<Integer> anGangCards = game.getAnGangCards();
		canAnGangUser.userGangCards(anGangCards);
		logger.info("自动出牌...暗杠.................:"+Algorithm2.showPai(anGangCards));
		canAnGangUser.recordUserGangCards(1, anGangCards);
		PlayGameService.notifyAllUserAnGang(game, anGangCards,canAnGangUser);//通知所有的玩家杠的牌 
		PlayGameService.modifyUserScoreForAnGang(game, canAnGangUser);//修改玩家暗杠得分
		//该玩家在抓一张牌 
		PlayGameService.userDrawCard(game, canAnGangUser.getDirection());
	}

	/**托管自动公杠
	 * @param game
	 */
	public static void autoGongGang(Game game){
		User user = game.getCanGongGangUser();
		Integer cardId = game.getGongGangCardId();
		List<Integer> pengCards = user.getPengCards();//用户碰的牌
		List<Integer> removeList = PlayGameService.getRemoveList(cardId, pengCards);
		for(int i=0;i<removeList.size();i++){
			Integer revomeCard = removeList.get(i);
			pengCards.remove(revomeCard);
		}
		removeList.add(cardId);
		logger.info("托管自动公杠....................:"+Algorithm2.showPai(removeList));
		//从自己的牌中移除公杠的那张牌
		user.removeCardFromGongGang(cardId);
		//记录玩家杠的牌
		user.recordUserGangCards(2, removeList);
		PlayGameService.notifyAllUserGongGang(game, removeList,user);//通知所有的玩家杠的牌 
		PlayGameService.modifyUserScoreForGongGang(game, user);//修改玩家公杠得分
		//该玩家在抓一张牌 
		userDrawCard(game, user.getDirection());
	}
	
	/**通知用户可以公杠
	 * @param game
	 * @param removeCard
	 * @param user
	 * @param gongGangCards
	 */
	private static void noticeUserCanGongGang(Game game, Integer removeCard, User user, List<Integer> gongGangCards) {
		game.setGameStatus(GAGME_STATUS_OF_GONG_GANG);//公杠
		game.setGongGangCardId(removeCard);
		game.setCanGongGangUser(user);//可以公杠的玩家
		notifyUserDrawDirection(removeCard, user,gongGangCards,0);//通知抓牌的方向
	}
	
	
	/**
	 * 在本局臭庄之后，计算用户的得分，依然把最后的结算发送给用户
	 */
	public static void afterChouZhuang(Game game,User lastGetCardUser,int removeCard){
		//依然通知玩家抓的牌是什么
		notifyUserDrawDirection(removeCard, lastGetCardUser,null,-1);//通知抓牌的方向
		OneRoom room = game.getRoom();
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put("type", "hupai");
		outJsonObject.put("description", "胡牌了");
		outJsonObject.put("method", "playGame");
		JSONArray userJsonArray = getUserJSONArray(room);
		List<Integer> remainCards = game.getRemainCards();
		outJsonObject.put("remainCards", remainCards);//剩余的牌
		outJsonObject.put("users", userJsonArray);
		NotifyTool.notifyIoSessionList(GameManager.getSessionListWithRoomNumber(lastGetCardUser.getRoomId()+""), outJsonObject);
		initializeUser(lastGetCardUser);//初始化用户的数据
		setCurrentGameOver(game);//设置当前的游戏结束
	}
	
	/**臭庄
	 * @return
	 */
	public static boolean chouZhuang(List<Integer> remainCards,Game game){
		OneRoom room = game.getRoom();
		int zhama = room.getZhama();
		if(remainCards.size()<=zhama){
			return true;
		}else{
			return false;
		}
	}
	

	/**用户赢牌
	 * @param game 
	 * @param removeCard 最后抓的牌
	 * @param user 赢牌的玩家
	 */
	private static void userWin(Game game, Integer removeCard, User user) {
		//依然通知玩家抓的牌是什么
		notifyUserDrawDirection(removeCard, user,null,-1);//通知抓牌的方向
		//计算中码
		List<Integer> zhongMaCards = getZhongMaCard(game, user);
		moidfyUserScoreByZhongMaCards(user, game, zhongMaCards);//根据中码数修改用户的成绩
		modifyUserScoreByHuPai(user, game);//根据胡牌修改用户的得分
		//记录下当前的局的战绩
		recordUserScore(game);
		notifyUserWin(user,game,removeCard,zhongMaCards);//通知用户赢牌
		initializeUser(user);//初始化用户的数据
		setCurrentGameOver(game);//设置当前的游戏结束
	}
	
	
	/**记录下用户的得分
	 * @param game
	 */
	public static void recordUserScore(Game game){
		List<User> userList = game.getUserList();
		int roomid = game.getRoom().getId();//房间号
		java.util.Date createDate = new java.util.Date();
		for(int i=0;i<userList.size();i++){
			User user = userList.get(i);
			int currentGame = user.getCurrentGame();//当前的局数
			int score = user.getCurrentGameSore();
			int userid = user.getId();//用户的ID
			UserScore userScore = new UserScore(userid, roomid,currentGame,score,createDate);
			userScoreDao.saveUserScore(userScore);
		}
	}
	

	/**设置当前的游戏结束
	 * @param game
	 */
	private static void setCurrentGameOver(Game game) {
		int alreadyTotalGame = game.getAlreadyTotalGame();
		game.getGameStatusMap().put(alreadyTotalGame, GAME_END);
		game.setGameStatus(GAGME_STATUS_OF_CHUPAI);//游戏的状态变成出牌
		game.setStatus(GAGME_STATUS_OF_WAIT_START);//游戏等待
		//判断当前的游戏是否结束
		OneRoom room = game.getRoom();
		int roomTotal = room.getTotal();
		if(alreadyTotalGame==roomTotal){//游戏结束
			summarizedAll(game);
			game = null;
		}else{//如果总的游戏还没有结束,30秒后还有未准备的玩家,则自动准备 
			if(game!=null){
				prepare(game);
			}
		}
	}
	
	/**自动准备游戏,如果总的游戏还没有结束,30秒后还有未准备的玩家,则自动准备 
	 * @param game
	 */
	private static void prepare(final Game game) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(game.getStatus()==GAGME_STATUS_OF_WAIT_START){//等待开局
					OneRoom room = game.getRoom();
					List<User> userList = room.getUserList();
					for(int i=0;i<userList.size();i++){
						User user = userList.get(i);
						if(!user.isReady()){
							user.setReady(true);
							//通知该玩家去掉结算的界面
							JSONObject autoStartJsonObject = new JSONObject();
							autoStartJsonObject.put(method, "autoStart");
							user.getIoSession().write(autoStartJsonObject.toString());//通知玩家自动开始,去掉结算的界面
							JSONObject readyJsonObject = UserService.getReadyJsonObject(user);
							NotifyTool.notifyIoSessionList(room.getUserIoSessionList(), readyJsonObject);
						}
					}
					//开始游戏
					UserService userService = new UserService();
					userService.beginGame(room);
				}
			}
		}, TIME_TO_START_GAME);
	}

	/**结算，房间解散 
	 * @param game
	 */
	private static void summarizedAll(Game game) {
		OneRoom room = game.getRoom();
		List<User> userList = room.getUserList();
		JSONObject outJSONObject = getSummarizeJsonObject(userList);
		NotifyTool.notifyIoSessionList(GameManager.getSessionListWithRoomNumber(room.getId()+""), outJSONObject);
		for(int i=0;i<userList.size();i++){
			User user = userList.get(i);
			user.clearAll();//清空用户所有的属性
		}
		//记录玩家的总成绩
		recoredUserScore(outJSONObject, game);
		//先移除游戏中的map,后移除房间中的map 否则有空指针异常,顺序不可颠倒
		GameManager.removeGameWithRoomNumber(room.getId()+"");
		RoomManager.removeOneRoomByRoomId(room.getId()+"");
	}
	
	
	/**
	 * 记录玩家的总成绩
	 */
	public static void recoredUserScore(JSONObject jsonObject,Game game){
		JSONArray userArray = jsonObject.getJSONArray("userScoreArray");
		int roomNumber = game.getRoom().getRoomNumber();
		Date createDate = new Date();
		for(int i=0;i<userArray.length();i++){
			JSONObject user = userArray.getJSONObject(i);
			SumScore sumScore = new SumScore();
			sumScore.setZhongMaTotal(user.getInt("zhongMa"));
			sumScore.setFinalScore(user.getInt("finallyScore"));
			sumScore.setAnGangTotal(user.getInt("anGang"));
			sumScore.setHuPaiTotal(user.getInt("hupai"));
			sumScore.setAnGangTotal(user.getInt("gongGang"));
			sumScore.setUserid(user.getInt("userId"));
			sumScore.setRoomNumber(roomNumber+"");
			sumScore.setCreateDate(createDate);
			sumScoreDao.saveSumScore(sumScore);//保存用户的房间号
		}
	}
	
	

	/**得到结算的jsonObejct
	 * @param userList
	 * @return
	 */
	private static JSONObject getSummarizeJsonObject(List<User> userList) {
		JSONObject outJSONObject = new JSONObject();
		JSONArray userScoreArray = new JSONArray();
		outJSONObject.put(method, "summarizedAll");
		for (int i = 0; i < userList.size(); i++) {
			User user = userList.get(i);
			Map<Integer, Score> scoreMap = user.getScoreMap();
			Iterator<Integer> iterator = scoreMap.keySet().iterator();
			int hupai = 0;
			int gongGang = 0;
			int anGang = 0;
			int zhongMa = 0;
			int finallyScore = 0;
			while(iterator.hasNext()){
				Integer key = iterator.next();
				Score score = scoreMap.get(key);
				int huPaiTotal = score.getHuPaiTotal();
				hupai = hupai+huPaiTotal;
				int gongGangTotal = score.getMingGangtotal();//公杠也称明杠
				gongGang = gongGang + gongGangTotal;
				int anGangTotal = score.getAnGangTotal();
				anGang = anGang + anGangTotal;
				int zhongMaTotal = score.getZhongMaTotal();
				zhongMa = zhongMa + zhongMaTotal;
				int finalScore = score.getFinalScore();
				finallyScore = finallyScore + finalScore;
			}
			JSONObject userScoreJSONObject = new JSONObject();
			userScoreJSONObject.put("hupai", hupai);
			userScoreJSONObject.put("gongGang", gongGang);
			userScoreJSONObject.put("anGang", anGang);
			userScoreJSONObject.put("zhongMa", zhongMa);
			userScoreJSONObject.put("finallyScore", finallyScore);
			userScoreJSONObject.put("userId", user.getId());
			userScoreJSONObject.put(direction, user.getDirection());
			userScoreArray.put(userScoreJSONObject);
		}
		outJSONObject.put("userScoreArray", userScoreArray);
		return outJSONObject;
	}
	
	/**根据胡牌修改用户的得分 
	 * @param user 胡牌的用户
	 * @param game
	 */
	private static void modifyUserScoreByHuPai(User user, Game game) {
		List<User> userList = game.getUserList();
		for(User u:userList){
			if(u.getId()==user.getId()){//胜利的玩家
				u.addScoreForHuPai();
				//System.out.println("dd");
			}else{//输牌的玩家
				u.reduceScoreForHuPai();
			}
		}
	}

	/**修改用户的成绩根据扎码个数
	 * @param user
	 */
	public static void moidfyUserScoreByZhongMaCards(User winUser,Game game,List<Integer> zhongMaCards){
		int totalZhongMa = getTotalZhongMa(zhongMaCards);
		List<User> userList = game.getUserList();
		for(int i=0;i<userList.size();i++){
			User user = userList.get(i);
			if(user.getId()!=winUser.getId()){//输牌的玩家
				user.reduceScoreForZhongMa(totalZhongMa);
			}else if(user.getId()==winUser.getId()){//胡牌的玩家
				user.addScoreForZhongMa(totalZhongMa);
			}
		}
	}

	/**得到总的中码数 (1,5,9,红中)
	 * @param zhongMaCards
	 */
	private static int getTotalZhongMa(List<Integer> zhongMaCards) {
		int totalZhongMa = 0;
		for(int i=0;i<zhongMaCards.size();i++){
			Integer card = zhongMaCards.get(i);
			int typeInt = Algorithm2.getTypeInt(card);
			String cardType = CardsMap.getCardType(card);
			if(typeInt==1||typeInt==5||typeInt==9||cardType.equals("红中")){
				totalZhongMa ++;
			}
		}
		return totalZhongMa;
	}
	
	/**通知所有的玩家 暗杠的牌
	 * @param game
	 * @param pengCards
	 */
	public static void notifyAllUserAnGang(Game game, List<Integer> gangCards,User user) {
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put("gangCards", gangCards);
		outJsonObject.put(method, "playGame");
		outJsonObject.put(type, "anGang");
		outJsonObject.put("gangDirection", user.getDirection());
		outJsonObject.put(discription, "玩家杠的牌");
		List<IoSession> userIoSessionList = game.getRoom().getUserIoSessionList();
		NotifyTool.notifyIoSessionList(userIoSessionList, outJsonObject);
	}
	
	
	
	/**通知所有的玩家杠的牌
	 * @param game
	 * @param pengCards
	 */
	public static void notifyAllUserGang(Game game, List<Integer> gangCards,User user) {
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put("gangCards", gangCards);
		outJsonObject.put(method, "playGame");
		outJsonObject.put(type, "gang");
		outJsonObject.put("gangDirection", user.getDirection());
		outJsonObject.put(discription, "玩家杠的牌");
		List<IoSession> userIoSessionList = game.getRoom().getUserIoSessionList();
		NotifyTool.notifyIoSessionList(userIoSessionList, outJsonObject);
	}
	
	
	/**通知所有的玩家公杠的牌
	 * @param game
	 * @param pengCards
	 */
	public static void notifyAllUserGongGang(Game game, List<Integer> gangCards,User user) {
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put("gangCards", gangCards);
		outJsonObject.put(method, "playGame");
		outJsonObject.put(type, "gongGang");
		outJsonObject.put("gangDirection", user.getDirection());
		outJsonObject.put(discription, "玩家杠的牌");
		List<IoSession> userIoSessionList = game.getRoom().getUserIoSessionList();
		NotifyTool.notifyIoSessionList(userIoSessionList, outJsonObject);
	}
	
	/**碰牌或杠牌 ,出完牌后改变出牌的状态
	 * @param jsonObject
	 * @param session
	 */
	private void peng(JSONObject jsonObject, IoSession session) {
		int cardId = jsonObject.getInt("cardId");
		Game game = getGame(session);
		User sessionUser = (User) session.getAttribute("user");
		sessionUser.setUserCanPlay(true);
		User gamingUser = getGamingUser(sessionUser.getId(), sessionUser.getRoomId());
		gamingUser.setUserCanPlay(true);
		boolean canPeng = checkUserIsCanPeng(gamingUser);
		if(!canPeng){
			notifyUserCanNotPengOrGang(session, 1);
			return;
		}
		Map<String, User> seatMap = game.getSeatMap();
		User u = seatMap.get(sessionUser.getDirection());
		List<Integer> cards = u.getCards();
		List<Integer> pengList = getPengList(cards, cardId);//得到可以碰的集合
		u.userPengCards(pengList);//玩家碰牌
		pengList.add(cardId);
		u.addUserPengCards(pengList);//用户添加碰出的牌
		u.setUserCanPlay(true);//该玩家可以出牌
		game.setGameStatus(GAGME_STATUS_OF_CHUPAI);//游戏的状态变为出牌
		int hashCode = game.hashCode();
		System.out.println("碰牌后 gameHashCode:"+hashCode);
		game.setDirec(u.getDirection());
		u.setLastChuPaiDate(new Date());
		//通知所有的玩家，游戏方向的改变
		//notifyAllUserDirectionChange(user);
		//通知所有的玩家，碰的牌是什么
		notifyAllUserPeng(game, pengList,u);
	}
	
	
	/**通知用户不可碰
	 * @param session
	 * @param type 1 、不可以碰，2、不可以杠
	 */
	public void notifyUserCanNotPengOrGang(IoSession session,int type){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(code, error);
		switch (type) {
		case 1:
			jsonObject.put(discription, "不可以碰");
			break;
		case 2:
			jsonObject.put(discription, "不可以杠");
			break;
		}
		session.write(jsonObject.toString());
	}
	
	
	
	public boolean checkUserIsCanPeng(User user){
		boolean result =  user.isCanPeng();
		return result;
	}
	
	
	/**检测用户是否可以杠
	 * @param user
	 * @return
	 */
	public boolean checkUserIsCanGang(User user){
		boolean result =  user.isCanGang();
		return result;
	}
	
	
	
	/**得到可以碰的集合
	 * @param cards
	 * @param cardId
	 * @return
	 */
	public static List<Integer> getPengList(List<Integer> cards,int cardId){
		List<Integer> list = new ArrayList<>();
		String cardType = CardsMap.getCardType(cardId);
		int total = 0;
		for(Integer card:cards){
			if(CardsMap.getCardType(card).equals(cardType)&&total<2){//4444 万 到第二个4停止,因为后边又加了一个
				list.add(card);
				total ++ ;
			}
		}
		return list;
	}
	
	
	/**得到可以碰的集合
	 * @param cards
	 * @param cardId
	 * @return
	 */
	public static List<Integer> getGangList(List<Integer> cards,int cardId){
		List<Integer> list = new ArrayList<>();
		String cardType = CardsMap.getCardType(cardId);
		int total = 0;
		for(Integer card:cards){
			if(CardsMap.getCardType(card).equals(cardType)&&total<4){//4444 万 到第四个4停止
				list.add(card);
				total ++ ;
			}
		}
		return list;
	}
	
	
	
	/**通知所有的玩家游戏方向改变
	 * @param user
	 */
	public void notifyAllUserDirectionChange(User user){
		Game game = getGame(user);
		String nextDirection = user.getDirection();
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put(method, "playDirection");
		outJsonObject.put("direction", nextDirection);
		outJsonObject.put("description", "出牌的的方向");
		game.setDirec(user.getDirection());//出牌的方向改变
		NotifyTool.notifyIoSessionList(GameManager.getSessionListWithRoomNumber(user.getRoomId()+""), outJsonObject);
	}
	

	/**通知所有的玩家碰的牌
	 * @param game
	 * @param pengCards
	 * @param user 
	 */
	public static void notifyAllUserPeng(Game game, List<Integer> pengCards, User user) {
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put(method, "playGame");
		outJsonObject.put(type, "peng");
		outJsonObject.put("pengDirction",user.getDirection());
		outJsonObject.put("pengCards", pengCards);
		List<IoSession> userIoSessionList = game.getRoom().getUserIoSessionList();
		NotifyTool.notifyIoSessionList(userIoSessionList, outJsonObject);
	}
	
	/**得到游戏
	 * @param session
	 * @return
	 */
	public static Game getGame(IoSession session) {
		User user = (User) session.getAttribute("user");
		Game game = getGame(user);
		return game;
	}
	
	/**出牌,清空该用户的没有出牌次数
	 * @param jsonObject
	 * @param session
	 */
	public void chuPai(JSONObject jsonObject, IoSession session){
		int cardId = jsonObject.getInt("cardId");//出牌的牌号
		User user = (User) session.getAttribute("user");
		Game game = getGame(user);
		User gamingUser = getGamingUser(user.getId(), user.getRoomId());
		gamingUser.setTotalNotPlay(0);//清空该用户的没有出牌的次数
		int result = checkPower(game, user,cardId);//检测用户出牌的权限
		if(result<0){
			notifyUserCanNotChuPai(session,result);
			return;
		}
		Map<String, User> seatMap = game.getSeatMap();
		String direction = gamingUser.getDirection();//得到当前的座次
		User u = seatMap.get(direction);
		int removeCardId = u.chuPai(cardId);//出牌
		if(removeCardId<0){
			return;
		}
		JSONObject outJsonObject = getChuPaiOutJSONObject(cardId, u);
		NotifyTool.notifyIoSessionList(GameManager.getSessionListWithRoomNumber(user.getRoomId()), outJsonObject);//通知所有用户打出的牌 是什么
		analysis(cardId, gamingUser, game);//继续分析是下一个人出牌还是能够碰牌和杠牌
	}

	
	
	/**给用户提示错误信息,不可以出牌
	 * @param session
	 * @param result 
	 */
	public void notifyUserCanNotChuPai(IoSession session, int result){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(code, "error");
		if(result==-1){
			jsonObject.put(discription, "不该你出牌");
		}else if(result==-2){
			jsonObject.put(discription, "该张牌不存在");
		}
		session.write(jsonObject.toString());
	}
	
	
	/**检测用户的权限
	 * @param cardId 
	 * @return -1 不该出牌 -2牌不存在
	 */
	public int checkPower(Game game,User user, int cardId){
		int result = 0;
		String direc = game.getDirec();
		if(!direc.equals(user.getDirection())){
			return -1;
		}
		List<Integer> cards = game.getSeatMap().get(user.getDirection()).getCards();
		int index = MathUtil.binarySearch(cardId, cards);
		if(index<0){
			return -2;
		}
		return result;
	}
	
	/**得到出牌的json对象
	 * @param cardId
	 * @param u
	 * @return
	 */
	public static JSONObject getChuPaiOutJSONObject(int cardId, User u) {
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put("cardId", cardId);//打出的牌
		outJsonObject.put("direction", u.getDirection());//出牌的人的座次
		outJsonObject.put("userId", u.getId());
		outJsonObject.put("type", "chupai");
		outJsonObject.put("method", "playGame");
		outJsonObject.put("discription", "出牌");
		return outJsonObject;
	}

	/**分析下一个人是否可以抓牌或杠牌,然后进一步计算
	 * @param cardId 打出的牌
	 * @param user 当前的人
	 * @param game
	 * @param seatMap
	 * @param direction 出牌的方向
	 */
	public static void analysis(int cardId, User user, Game game) {
		Map<String, User> seatMap = game.getSeatMap();
		//计算可以碰牌或杠牌的人
		User canPengOrGangUser = getPengOrGangCardUser(cardId,seatMap,user.getId());
		if(canPengOrGangUser!=null){
			//可以听牌或杠牌,该用户也没有托管才通知它
			boolean auto = canPengOrGangUser.isAuto();//用户是否托管
			if(auto){//如果托管自动碰
				List<Integer> cards = canPengOrGangUser.getCards();
				boolean isTing = false;
				Integer myGrabCard = canPengOrGangUser.getMyGrabCard();
				if(myGrabCard!=null){
					logger.info("在用户托管的时候检测到用户碰牌或杠牌:"+canPengOrGangUser);
					isTing = canPengOrGangUser.isUserTingPaiOfPengOrGang(cards);//用户是否听牌
				}
				if(isTing){//如果该用户已经听牌
					logger.info("碰牌或杠牌的时候直接越过该用户"+canPengOrGangUser);
					nextUserDrawCards(cardId, user, game);
					return;
				}
				notifyUserCanPengOrGang(cardId, user, game, canPengOrGangUser);
				autoPengOrGang(canPengOrGangUser,game);
			}else{
				notifyUserCanPengOrGang(cardId, user, game, canPengOrGangUser);
			}
		}else{//下一个人抓牌
			nextUserDrawCards(cardId, user, game);
		}
	}

	/**下一个用户抓牌
	 * @param cardId
	 * @param user
	 * @param game
	 */
	private static void nextUserDrawCards(int cardId, User user, Game game) {
		int hashCode = user.hashCode();
		user.addMyPlays(cardId);
		String nextDirection = getNextDirection(user.getDirection());
		notifyUserDirectionChange(user, nextDirection);//通知用户出牌的方向改变
		userDrawCard(game, nextDirection);//用户抓牌
	}
	
	/**自动碰牌和杠牌
	 * @param canPengOrGangUser
	 */
	public static void autoPengOrGang(User canPengOrGangUser,Game game) {
		if(canPengOrGangUser.getPengOrGang()==1){//可以碰牌
			List<Integer> pengList = canPengOrGangUser.getPengOrGangList();
			canPengOrGangUser.userPengCards(pengList);
			canPengOrGangUser.addUserPengCards(pengList);//用户添加碰出的牌
			notifyAllUserPeng(game, pengList,canPengOrGangUser);//通知碰牌
			game.setDirec(canPengOrGangUser.getDirection());//重新改变游戏的方向
			//碰牌后游戏的状态变为出牌
			game.setGameStatus(GAGME_STATUS_OF_CHUPAI);
			canPengOrGangUser.setLastChuPaiDate(new Date());
			if(canPengOrGangUser.isAuto()){
				autoChuPai(game);//自动出牌
			}
		}else if(canPengOrGangUser.getPengOrGang()==2){//可以杠牌
			List<Integer> gangCards = canPengOrGangUser.getPengOrGangList();
			canPengOrGangUser.userGangCards(gangCards);
			//记录玩家杠的牌
			canPengOrGangUser.recordUserGangCards(0, gangCards);
			modifyUserScoreForGang(game, canPengOrGangUser);//修改玩家得分
			PlayGameService.notifyAllUserGang(game, gangCards,canPengOrGangUser);//通知所有的玩家杠的牌 
			//该玩家在抓一张牌 
			PlayGameService.userDrawCard(game, canPengOrGangUser.getDirection());
		}
	}
	
	/**
	 * FIXME 注意这里加锁是为了，同一时间出牌
	 * 自动出牌
	 */
	public static synchronized void autoChuPai(Game game){
		String direc = game.getDirec();
		Map<String, User> seatMap = game.getSeatMap();
		User user = seatMap.get(direc);
		user.setUserCanPlay(true);
		int cardId = user.autoChuPai();//自动出的牌
		if(cardId<0){
			logger.info("可能在同一时间打牌了");
			return;
		}
		try {
			//FIXME 为了出牌不太快，线程休眠 1秒钟
			Thread.currentThread().sleep(1000);
			logger.info("出牌的时候暂停一秒");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.info("托管自动出牌....................:"+cardId+" "+CardsMap.getCardType(cardId));
		JSONObject outJsonObject = PlayGameService.getChuPaiOutJSONObject(cardId, user);
		NotifyTool.notifyIoSessionList(GameManager.getSessionListWithRoomNumber(user.getRoomId()), outJsonObject);//通知所有用户打出的牌 是什么
		analysis(cardId, user, game);
	}

	/**通知用户可以碰或者杠牌,这里设置了游戏的状态,碰牌或者杠牌
	 * @param cardId
	 * @param user 如果可以杠牌就是放杠的用户
	 * @param game
	 * @param canPengOrGangUser
	 */
	private static void notifyUserCanPengOrGang(int cardId, User user, Game game, User canPengOrGangUser) {
		canPengOrGangUser.setLastChuPaiDate(new Date());//记录下他可以碰和杠的时间
		//FIXME 明明可以杠牌提示不可以，需要改一下
		User gamingUser = getGamingUser(user.getId(), game.getRoom().getId()+"");
		if(canPengOrGangUser.getPengOrGang()==1){//可以碰牌
			canPengOrGangUser.setCanPeng(true);
			game.setAutoPengCardId(cardId);//可以碰的牌号
			game.setCanPengUser(canPengOrGangUser);//可以碰牌的用户
			game.setGameStatus(GAGME_STATUS_OF_PENGPAI);//碰牌
			gamingUser.setCanPeng(true);
		}else if(canPengOrGangUser.getPengOrGang()==2){//可以杠牌
			game.setAutoGangCardId(cardId);
			canPengOrGangUser.setCanPeng(true);
			canPengOrGangUser.setCanGang(true);
			gamingUser.setCanPeng(true);
			gamingUser.setCanGang(true);
			game.setFangGangUser(user);
			game.setCanGangUser(canPengOrGangUser);//可以杠牌的用户
			game.setGameStatus(GAGME_STATUS_OF_GANGPAI);//杠牌
		}
		notifyUserCanPengOrGang(cardId, game, canPengOrGangUser);
	}

	/**通知用户出牌的方向改变
	 * @param user
	 * @param nextDirection
	 */
	private static void notifyUserDirectionChange(User user,
			String nextDirection) {
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put(method, "playDirection");
		outJsonObject.put("direction", nextDirection);
		outJsonObject.put("description", "出牌的的方向");
		NotifyTool.notifyIoSessionList(GameManager.getSessionListWithRoomNumber(user.getRoomId()+""), outJsonObject);
	}

	/**通知用户可以碰或杠
	 * @param cardId
	 * @param game
	 * @param canPengOrGangUser
	 */
	private static void notifyUserCanPengOrGang(int cardId, Game game,
			User canPengOrGangUser) {
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put("cardId", cardId);
		outJsonObject.put("pengOrGangUser", canPengOrGangUser.getDirection());
		outJsonObject.put(discription, "该用户可以碰牌或杠牌");
		outJsonObject.put(method, "testType");//可以杠
		int pengOrGang = canPengOrGangUser.getPengOrGang();
		if(pengOrGang==1){
			outJsonObject.put(type, "canPeng");//可以碰
		}else if(pengOrGang==2){
			outJsonObject.put(type, "canGang");//可以杠
		}
		String nowDirection = game.getDirec();
		game.setBeforeTingOrGangDirection(nowDirection);//设置原来的方向
		game.setDirec(canPengOrGangUser.getDirection());//出牌的方向改变
		canPengOrGangUser.getIoSession().write(outJsonObject);//通知该用户可以碰牌或杠牌
	}
	

	
	/**抓牌
	 * @param game
	 * @param seatMap
	 * @param direction
	 * @return
	 *//*
	public static User userDrawCard(Game game, Map<String, User> seatMap,
			String direction) {
		List<Integer> remainCards = game.getRemainCards();
		Integer removeCard = remainCards.remove(0);
		System.out.println("当前的牌还有:"+remainCards);
		String nextDirection = getNextDirection(direction);
		User user = seatMap.get(nextDirection);//
		game.setDirec(user.getDirection());//把当前出牌的方向改变
		boolean isWin = user.zhuaPai(removeCard);//抓牌 
		user.setMyGrabCard(removeCard);//当前用户抓到的牌 
		int zhama = game.getRoom().getZhama();
		if(isWin){ 
			afterUserWin(game, user,removeCard);
		}else if(zhama==remainCards.size()){//扎码数停止
			game.setGameOver(true);
			notifyUserNoUserWin(game);
			restartGame(game, user);
		}else{
			//分析用户是否可以公杠
			List<Integer> gongGangCards = analysisUserIsCanGongGang(removeCard,user);
			if(gongGangCards.size()>0){
				notifyUserDrawDirection(removeCard, user,gongGangCards,GONG_GANG);//通知抓牌的方向
			}else{
				//分析该用户是否可以暗杠
				List<Integer> anGangCards = isUserCanAnGang(user);
				notifyUserDrawDirection(removeCard, user,anGangCards,AN_GANG);//通知抓牌的方向
			}
		}
		return user;
	}*/
	
	
	/**分析用户是否可以暗杠
	 * @return
	 */
	public static List<Integer> isUserCanAnGang(User user){
		List<Integer> cards = user.getCards();
		String type = CardsMap.getCardType(cards.get(0));
		int total = 0;
		int compareCard = cards.get(0);
		List<Integer> anGangCards = new ArrayList<>();
		for(int i=1;i<cards.size();i++){
			Integer card = cards.get(i);
			String currentType = CardsMap.getCardType(card);
			if(type.equals(currentType)){
				total++;
				anGangCards.add(card);
				if(total==3){
					anGangCards.add(compareCard);
					break;
				}
			}else{
				type = currentType;
				total = 0;//计数清零
				anGangCards = new ArrayList<>();
				compareCard = card;
			}
		}
		
		if(anGangCards.size()!=4){
			return new ArrayList<Integer>();
		}
		
		Collections.sort(anGangCards);
		return anGangCards;
	}
	

	/**分析用户是否可以公杠
	 * @param removeCard
	 * @param user
	 */
	private static List<Integer> analysisUserIsCanGongGang(Integer removeCard, User user) {
		List<Integer> pengCards = user.getPengCards();//碰出的牌
		String type = CardsMap.getCardType(removeCard);
		int total = 0;
		List<Integer> gongGangCards =  new ArrayList<>();
		for(int i=0;i<pengCards.size();i++){
			Integer card = pengCards.get(i);
			String cardType = CardsMap.getCardType(card);
			if(type.equals(cardType)){
				gongGangCards.add(card);
				total ++;
			}
			if(total==3){
				break;
			}
		}
		return gongGangCards;
	}

	/**提示没有用户赢牌
	 * @param game
	 */
	private static void notifyUserNoUserWin(Game game) {
		List<User> userList = game.getUserList();
		JSONObject outJsonObject = new JSONObject();
		List<Integer> remainCards = game.getRemainCards();
		outJsonObject.put("remainCards", remainCards);//剩余的牌
		JSONArray jsonArray = new JSONArray();
		for(int i=0;i<userList.size();i++){
			User u = userList.get(i);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(direction, u.getDirection());
			jsonObject.put("cards", u.getCards());
			jsonObject.put("userId", u.getId());
			jsonArray.put(jsonObject);
		}
		outJsonObject.put("users", jsonArray);
		List<IoSession> ioSessionList = game.getIoSessionList();
		NotifyTool.notifyIoSessionList(ioSessionList, outJsonObject);
	}

	/**游戏结束之后
	 * @param game
	 * @param user
	 */
	private static void afterUserWin(Game game, User user,int huCardId) {
//		try {
//			Thread.currentThread().sleep(100);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		game.setGameOver(true);//游戏结束，有玩家赢牌了
		//计算中码
		List<Integer> zhongMaCards = getZhongMaCard(game, user);
		moidfyUserScoreByZhongMaCards(user, game, zhongMaCards);//根据中码数修改用户的成绩
		notifyUserWin(user,game,huCardId,zhongMaCards);//通知用户赢牌
		restartGame(game, user);
	}
	
	/**重新开始游戏
	 * @param game
	 * @param bankUser 庄家
	 */
	public static void restartGame(Game game,User bankUser){
		List<User> userList = game.getUserList();
		for(User u:userList){
			u.setCanGang(false);
			u.setCanPeng(false);
			u.setReady(false);//取消准备
			u.setPengCards(null);//碰出去的牌
			u.setGangCards(null);//杠出去的牌
			u.setBanker(false);
		}
		OneRoom room = game.getRoom();
		room.setUse(false);//设置房间可以使用
		bankUser.setBanker(true);
	}
	
	/**通知用户赢牌
	 * @param direction 胡牌的方向
	 * @param user  赢牌的玩家
	 * @param game 
	 * @param zhongMaCards 
	 */
	private static void notifyUserWin(User user, Game game,int huCardId, List<Integer> zhongMaCards) {
		OneRoom room = game.getRoom();
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put("type", "hupai");
		outJsonObject.put("userId", user.getId());
		outJsonObject.put("description", "胡牌了");
		outJsonObject.put("method", "playGame");
		outJsonObject.put("huCardId", huCardId);
		outJsonObject.put("huCards", user.getCards());
		outJsonObject.put("hupaiDirection",user.getDirection());
		outJsonObject.put("zhongMaCards", zhongMaCards);//中码的牌
		JSONArray userJsonArray = getUserJSONArray(room);
		List<Integer> remainCards = game.getRemainCards();
		outJsonObject.put("remainCards", remainCards);//剩余的牌
		outJsonObject.put("users", userJsonArray);
		NotifyTool.notifyIoSessionList(GameManager.getSessionListWithRoomNumber(user.getRoomId()+""), outJsonObject);
		//afterUserWin(user);//在用户赢牌以后分析
	}

	/**得到当前房间里用户的信息
	 * @param room
	 * @return
	 */
	private static JSONArray getUserJSONArray(OneRoom room) {
		JSONArray jsonArray = new JSONArray();
		List<User> userList = room.getUserList();
		for(int i=0;i<userList.size();i++){
			User u = userList.get(i);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(direction, u.getDirection());
			jsonObject.put("pengCards", u.getPengCards());//碰出的牌
			List<GangCard> gangCards = u.getGangCards();
			if(gangCards.size()>0){
				JSONArray gangCardArray = getJGangCardArray(gangCards);
				jsonObject.put("gangCardArray", gangCardArray);
			}
			jsonObject.put("score", u.getCurrentGameSore());//得到本局的得分
			jsonObject.put("cards", u.getCards());//剩余的牌
			jsonObject.put("userId", u.getId());
			int playerScoreByAdd = UserService.getUserCurrentGameScore(u);//用户当前的分数
			jsonObject.put("playerScoreByAdd", playerScoreByAdd);
			jsonArray.put(jsonObject);
		}
		return jsonArray;
	}

	
	/**得到中码的牌
	 * @param game
	 * @param huPaiUser
	 */
	public static List<Integer> getZhongMaCard(Game game,User huPaiUser){
		List<Integer> remainCards = game.getRemainCards();
		OneRoom room = game.getRoom();
		int zhama = room.getZhama();//扎码数
		List<Integer> cards = huPaiUser.getCards();
		boolean haveHongZhong = isHaveHongZhong(cards);
		if(!haveHongZhong){//如果胡牌人的手里没有红中则多加一个码
			zhama = zhama+1;
		}
		List<Integer> zhongMaCardList = removeAndGetZhongMaCardList(remainCards, zhama);
		return zhongMaCardList;
	}
	
	/**
	 * @param remainCards 剩余的牌
	 * @param zhama 扎码的个数
	 * @return
	 */
	public static List<Integer> removeAndGetZhongMaCardList(List<Integer> remainCards,int zhama){
		int size = remainCards.size();
		if(size>zhama){//唯恐敦上的牌不够
			size = zhama;
		}
		List<Integer> zhongMaList =  new ArrayList<>();//中码的牌
		for(int i=0;i<size;i++){
			zhongMaList.add(remainCards.get(i));
		}
		//在剩余的牌中移除
		for(int i=0;i<zhongMaList.size();i++){
			Integer removeCard = zhongMaList.get(i);
			remainCards.remove(removeCard);
		}
		return zhongMaList;
	}
	
	
	/**查看牌中是否含有红中
	 * @param cards 
	 * @return
	 */
	public static boolean isHaveHongZhong(List<Integer> cards){
		//倒序遍历，因为最大的是红中
		for(int i=cards.size()-1;i>=0;i--){
			Integer card = cards.get(i);
			if(CardsMap.getCardType(card).equals("红中")){
				return true;
			}
		}
		return false;
	}
	
	
	
	/**得到杠牌的类型json数组
	 * @param gangCards
	 * @return
	 */
	private static JSONArray getJGangCardArray(List<GangCard> gangCards) {
		JSONArray gangCardArray =  new JSONArray();
		for(int j=0;j<gangCards.size();j++){
			JSONObject gangCardJSONObject = new JSONObject();
			GangCard gangCard = gangCards.get(j);
			int gangType = gangCard.getType();
			String sGangType = "";
			if(gangType == 0){
				sGangType = "jieGang";
			}else if(gangType == 1){
				sGangType = "anGang";
			}else if(gangType == 2){
				sGangType = "gongGang";
			}
			gangCardJSONObject.put("gangType",sGangType);
			gangCardJSONObject.put("gangCard", gangCard.getCards());
			gangCardArray.put(gangCardJSONObject);
		}
		return gangCardArray;
	}

	
	/**在用户赢牌以后,初始化用户的一些数据
	 * @param user,胜利的玩家，或者是最后抓牌的玩家
	 */
	private static void initializeUser(User user) {
		Game game = getGame(user);
		int alreadyTotalGame = game.getAlreadyTotalGame();
		int totalGame = game.getTotalGame();
		if(alreadyTotalGame+1<totalGame){
			setNewBank(user,game);//设置新的庄家
			List<User> userList = game.getUserList();
			for(int i=0;i<userList.size();i++){
				User u = userList.get(i);
				List<Integer> myPlays = new ArrayList<>();//出去的牌
				u.setMyPlays(myPlays);
				List<Integer> pengCards = new ArrayList<>();//碰的牌
				u.setPengCards(pengCards);
				List<GangCard> gangCards = new ArrayList<>();//杠的牌
				u.setGangCards(gangCards);
			}
		}
		game.setAlreadyTotalGame(alreadyTotalGame+1);//设置已经万的游戏局数 
		game.setDirec("");
		game.setStep(0);
		game.setBeforeTingOrGangDirection("");
	}


	/**设置新的庄家
	 * @param user
	 */
	private static void setNewBank(User user,Game game) {
		OneRoom room = game.getRoom();
		List<User> userList = room.getUserList();
		for(User u:userList){
			if(u.getId()!=user.getId()){
				u.setBanker(false);
			}
		}
		user.setBanker(true);
	}

	/**通知抓牌的方向
	 * @param removeCard
	 * @param nextUser 抓牌的玩家
	 * @param cards 暗杠或者公杠的牌
	 * @param type 0、接杠 1、暗杠 2、公杠
	 */
	private static void notifyUserDrawDirection(Integer removeCard, User nextUser, List<Integer> cards,int type) {
		Game game = getGame(nextUser);
		List<User> userList = getUserListWithGame(game);
		for(User  user : userList){
			JSONObject outJsonObject = new JSONObject();
			//通知他抓到的牌
			outJsonObject.put("description", "抓牌的方向");
			outJsonObject.put("type", "zhuapai");
			outJsonObject.put(method, "playGame");
			outJsonObject.put("direction", nextUser.getDirection());
			if(user.getId()==nextUser.getId()){
				outJsonObject.put("getCardId", removeCard);
				if(cards!=null&&cards.size()>0){//该玩家可以明杠
					outJsonObject.put("isCanGangType", type);
					outJsonObject.put("cards", cards);
				}
			}else{
				outJsonObject.put("getCardId", 1000);//返回一个不存在的数
			}
			NotifyTool.notify(user.getIoSession(), outJsonObject);
			System.out.println("通知的玩家："+user.getId()+" "+user.getNickName());
			//user.getIoSession().write(outJsonObject.toString());
		}
		//NotifyTool.notify(nextUser.getIoSession(), outJsonObject);
	}

	
	private static List<User> getUserListWithGame(Game game){
		OneRoom room = game.getRoom();
		List<User> userList = room.getUserList();
		return userList;
	}
	
	/**得到游戏
	 * @param user
	 * @return
	 */
	public static Game getGame(User user) {
		Map<String, Game> gameMap = GameManager.getGameMap();
		String roomId = user.getRoomId();
		Game game = gameMap.get(roomId);
		return game;
	}

	/**计算出可以碰牌或杠牌的人
	 * @param cardId 上一个人打出的牌
	 * @param seatMap
	 * @param nowUserId
	 * @return
	 */
	public static User getPengOrGangCardUser(int cardId,Map<String, User> seatMap,int nowUserId){
		Iterator<String> it = seatMap.keySet().iterator();
		String type = CardsMap.getCardType(cardId);
		while(it.hasNext()){
			String next = it.next();
			User u = seatMap.get(next);
			if(u.getId()!=nowUserId){//不是现在这个人
				List<Integer> cards = u.getCards();
				int total = 0;
				List<Integer> pengOrGangList = new ArrayList<>();
				for(Integer cId:cards){
					if(CardsMap.getCardType(cId).equals(type)&&cId<108){//不是红中
						total ++;
						pengOrGangList.add(cId);
					}
				}
				if(total>=2){
					pengOrGangList.add(cardId);
					u.setPengOrGangList(pengOrGangList);
					if(total==2){
						u.setPengOrGang(1);
					}else if(total == 3){
						u.setPengOrGang(2);
					}
					return u;
				}
			}
		}
		return null;
	}
	
	
	/**得到下一个玩家的方向
	 * @param nowDirection 现在的方向
	 * @return
	 */
	public static String getNextDirection(String nowDirection){
		String direction = "";//方向
		switch (nowDirection) {
			case "east":
				direction = "north";
				break;
			case "north":
				direction = "west";
				break;
			case "west":
				direction = "south";
				break;
			case "south":
				direction = "east";
				break;
			default:
				break;
		}
		return direction;
	}
}

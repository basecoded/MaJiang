package com.zxz.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;

import com.zxz.controller.StartServer;
import com.zxz.utils.MathUtil;

public class Game{
	
	private static Logger logger = Logger.getLogger(Game.class);  
	
	OneRoom room;
	private List<Integer> cardList;
	int totalGame;//游戏的局数
	Map<String, User> seatMap;//座次
	String direc;//出牌方向
	List<Integer> remainCards;//剩余的牌
	int step=0;//出牌的步数
	String beforeTingOrGangDirection;//玩家可以碰或杠的时候出牌的方向
	int alreadyTotalGame = 0;//已经万的游戏局数,默认为0
	boolean isGameOver = false;//游戏是否结束
	/**
	 * 游戏的状态,0游戏进行中,1游戏等待开始
	 */
	private int status;
	List<Integer> anGangCards;//暗杠的牌
	Integer gongGangCardId;//(公杠)明杠的牌
	private Map<Integer, String> gameStatusMap = new HashMap<>();//当局是否结束
	private User fangGangUser;//放杠的用户
	Integer autoPengCardId;//自动的碰牌的牌号
	Integer autoGangCardId;//自动的杠牌的牌号
	User canPengUser;//可以碰牌的用户
	User canGangUser;//可以杠牌的用户
	User canGongGangUser;//公杠的用户
	User canAnGangUser;//暗杠的用户 
	

	int gameStatus;//碰牌，听牌，杠牌,出牌

	public Game(OneRoom room) {
		super();
		this.room = room;
		this.totalGame = room.getTotal();
	}
	
	
	
	
	public synchronized int getGameStatus() {
		return gameStatus;
	}

	/**设置出牌的状态 ，出牌、听牌、杠牌、暗杠
	 * @param gameStatus
	 */
	public synchronized void setGameStatus(int gameStatus) {
		this.gameStatus = gameStatus;
	}

	
	public User getCanGongGangUser() {
		return canGongGangUser;
	}

	public void setCanGongGangUser(User canGongGangUser) {
		this.canGongGangUser = canGongGangUser;
	}

	public User getCanAnGangUser() {
		return canAnGangUser;
	}

	public void setCanAnGangUser(User canAnGangUser) {
		this.canAnGangUser = canAnGangUser;
	}

	public Integer getAutoGangCardId() {
		return autoGangCardId;
	}

	public void setAutoGangCardId(Integer autoGangCardId) {
		this.autoGangCardId = autoGangCardId;
	}

	public User getCanGangUser() {
		return canGangUser;
	}

	public void setCanGangUser(User canGangUser) {
		this.canGangUser = canGangUser;
	}

	public User getCanPengUser() {
		return canPengUser;
	}

	public void setCanPengUser(User canPengUser) {
		this.canPengUser = canPengUser;
	}

	public Integer getAutoPengCardId() {
		return autoPengCardId;
	}

	public void setAutoPengCardId(Integer autoPengCardId) {
		this.autoPengCardId = autoPengCardId;
	}

	
	/**
	 * 得到游戏中的玩家
	 */
	public User getUserInRoomList(int userId){
		List<User> userList = this.getUserList();
		for(int i=0;i<userList.size();i++){
			User user = userList.get(i);
			if(user.getId()==userId){
				return user;
			}
		}
		logger.fatal("加减分的时候没有找到用户:");
		return null;
	}
	
	
	public User getFangGangUser() {
		return fangGangUser;
	}

	public void setFangGangUser(User fangGangUser) {
		this.fangGangUser = fangGangUser;
	}

	public Map<Integer, String> getGameStatusMap() {
		return gameStatusMap;
	}

	public void setGameStatusMap(Map<Integer, String> gameStatusMap) {
		this.gameStatusMap = gameStatusMap;
	}

	

	public Integer getGongGangCardId() {
		return gongGangCardId;
	}

	public void setGongGangCardId(Integer gongGangCardId) {
		this.gongGangCardId = gongGangCardId;
	}

	public List<Integer> getAnGangCards() {
		return anGangCards;
	}

	public void setAnGangCards(List<Integer> anGangCards) {
		this.anGangCards = anGangCards;
	}

	/**游戏的状态，等待游戏开始或游戏进行中
	 * @return
	 */
	public synchronized int getStatus() {
		return status;
	}

	/**游戏等待或者是游戏开始
	 * @param status
	 */
	public synchronized void setStatus(int status) {
		this.status = status;
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public void setGameOver(boolean isGameOver) {
		this.isGameOver = isGameOver;
	}

	public int getAlreadyTotalGame() {
		return alreadyTotalGame;
	}

	public void setAlreadyTotalGame(int alreadyTotalGame) {
		this.alreadyTotalGame = alreadyTotalGame;
	}

	public String getBeforeTingOrGangDirection() {
		return beforeTingOrGangDirection;
	}

	public void setBeforeTingOrGangDirection(String beforeTingOrGangDirection) {
		this.beforeTingOrGangDirection = beforeTingOrGangDirection;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}


	public void playGame() {
		room.setUse(true);//房间已经使用 
		cardList = MathUtil.creatRandom(0, 111);//生成牌
		remainCards = dealCard(room,cardList);//发牌
//		remainCards = virtualDealCard(room,cardList);//测试明杠发牌
		//remainCards = virtualAnGangDealCard(room, cardList);//测试暗杠
//		remainCards = liangGeNengGangDePai(room, cardList);
//		remainCards = wangluo(room, cardList);
//		remainCards = zidongCards(room, cardList);
//		remainCards = liangGeNengGangDePai(room, cardList);
		seatMap = generateSeat(room);//得到座次Map
		direc = getDirPaly(room);//出牌的方向
		//修改用户的当前的局数
		moifyUserCurrentGame(this);
		logger.info("发出牌后，玩家剩余的牌："+remainCards + " 总张数:"+remainCards.size());
	}
	
	
	
	/**修改用户当前的局数,并且设置当前的分数
	 * @param game
	 */
	public void moifyUserCurrentGame(Game game){
		List<User> userList = game.getUserList();
		for(int i=0;i<userList.size();i++){
			User user = userList.get(i);
			user.setCurrentGame(user.getCurrentGame()+1);
			Score score = new Score();
			Map<Integer, Score> scoreMap = user.getScoreMap();
			scoreMap.put(user.getCurrentGame(), score);
		}
	}


	public List<Integer> getRemainCards() {
		return remainCards;
	}


	public void setRemainCards(List<Integer> remainCards) {
		this.remainCards = remainCards;
	}


	public OneRoom getRoom() {
		return room;
	}

	public void setRoom(OneRoom room) {
		this.room = room;
	}

	public List<Integer> getCardList() {
		return cardList;
	}


	public void setCardList(List<Integer> cardList) {
		this.cardList = cardList;
	}


	public int getTotalGame() {
		return totalGame;
	}


	public void setTotalGame(int totalGame) {
		this.totalGame = totalGame;
	}


	public Map<String, User> getSeatMap() {
		return seatMap;
	}


	public void setSeatMap(Map<String, User> seatMap) {
		this.seatMap = seatMap;
	}


	public String getDirec() {
		return direc;
	}


	public void setDirec(String direc) {
		this.direc = direc;
	}


	/**得到出牌的方向
	 * @param room
	 * @return
	 */
	private String getDirPaly(OneRoom room){
		List<User> userList = room.getUserList();
		for(User user:userList){
			if(user.isBanker()){
				return user.getDirection();
			}
		}
		return "";
	}
	
	
	/**生成座次
	 * @param room
	 * @return 
	 */
	private Map<String, User> generateSeat(OneRoom room) {
		List<User> users = room.getUserList();
		Map<String, User> map = new HashMap<String, User>();
		for(int i=0;i<users.size();i++){
			User user = users.get(i);
			String direction = user.getDirection();
			map.put(direction, user);
		}
		return map;
	}
	
	
	/**得到所有的用户
	 * @return
	 */
	public List<User> getUserList(){
		List<User> userList = room.getUserList();
		return userList;
	}
	
	
	
	/**得到房间里所有的IoSession
	 * @return
	 */
	public List<IoSession> getIoSessionList(){
		List<User> userList = getUserList();
		List<IoSession> sessionList =  new ArrayList<>();
		for(int i=0;i<userList.size();i++){
			User user = userList.get(i);
			sessionList.add(user.getIoSession());
		}
		return sessionList;
	}
	
	
	/**发牌
	 * @param room
	 * @param cardList
	 * @return remainList 剩余的牌
	 */
	private List<Integer> dealCard(OneRoom room, List<Integer> cardList) {
		List<User> userList = room.getUserList();
		int begin = 0 ;
		for(int i=0;i<userList.size();i++){
			User user = userList.get(i);
			List<Integer> cards = new LinkedList<Integer>();
			boolean banker = user.isBanker();//如果是庄家
			if(banker){
				int end = begin +14;
				for(int j=begin;j<end;j++){
					cards.add(cardList.get(j));
					begin ++;
				}
			}else{
				int end = begin +13;
				for(int j=begin;j<end;j++){
					cards.add(cardList.get(j));
					begin ++;
				}
			}
			Collections.sort(cards);//给牌排序
			user.setCards(cards);
		}
		List<Integer> remainList = new LinkedList<Integer>();
		for (int i=53;i<cardList.size();i++){
			remainList.add(cardList.get(i));
		}
		return remainList;
	}
	
	
	/**发牌
	 * @param room
	 * @param cardList
	 * @return
	 */
	private List<Integer> virtualDealCard(OneRoom room, List<Integer> cardList) {
		List<User> userList = room.getUserList();
		
		//赵
		User zhao = userList.get(0);
		int [] zhaoCardArray = {3,6,14,15,16,17,23,37,52,70,86,90,96,101};
		List<Integer> zhaoCards = new LinkedList<Integer>();
		for(int i=0;i<zhaoCardArray.length;i++){
			zhaoCards.add(zhaoCardArray[i]);
		}
		zhao.setCards(zhaoCards);
		
		//钱
		User qian = userList.get(1);
		int [] qianCardArray = {13,32,44,46,47,50,51,63,66,74,79,100,107};
		List<Integer> qianCards = new LinkedList<Integer>();
		for(int i=0;i<qianCardArray.length;i++){
			qianCards.add(qianCardArray[i]);
		}
		qian.setCards(qianCards);
		
		//孙
		User sun = userList.get(2);
		int [] sunCardArray = {19,38,43,71,73,75,83,84,85,87,97,103,108};
		List<Integer> sunCards = new LinkedList<Integer>();
		for(int i=0;i<sunCardArray.length;i++){
			sunCards.add(sunCardArray[i]);
		}
		sun.setCards(sunCards);
		
		
		//李
		User li = userList.get(3);
		int [] liCardArray = {0,1,7,9,18,22,27,39,45,53,67,82,93};
		List<Integer> liCards = new LinkedList<Integer>();
		for(int i=0;i<liCardArray.length;i++){
			liCards.add(liCardArray[i]);
		}
		li.setCards(liCards);
		
		
		//剩余的牌
		int[] remainArray = {56, 105, 8, 10, 59, 41, 58, 49, 24, 2, 48, 31, 11, 69, 106, 92, 60, 68, 35, 64, 110, 12, 95, 40, 4, 104, 102, 57, 55, 81, 54, 21, 62, 30, 25, 77, 89, 111, 28, 80, 72, 91, 42, 78, 98, 94, 61, 109, 36, 29, 34, 26, 76, 33, 99, 20, 5, 65, 88};
		List<Integer> remainList = new LinkedList<Integer>();
		for (int i=0;i<remainArray.length;i++){
			remainList.add(remainArray[i]);
		}
		return remainList;
	}
	
	/**发牌
	 * @param room
	 * @param cardList
	 * @return
	 */
	private List<Integer> virtualAnGangDealCard(OneRoom room, List<Integer> cardList) {
		List<User> userList = room.getUserList();
		//赵
		User zhao = userList.get(0);
		int [] zhaoCardArray = {18,21,24,36,49,53,54,59,61,65,79,80,88,101};
		List<Integer> zhaoCards = new LinkedList<Integer>();
		for(int i=0;i<zhaoCardArray.length;i++){
			zhaoCards.add(zhaoCardArray[i]);
		}
		zhao.setCards(zhaoCards);
		
		//钱
		User qian = userList.get(1);
		int [] qianCardArray = {4,5,6,7,13,16,23,25,39,44,64,78,94};
		List<Integer> qianCards = new LinkedList<Integer>();
		for(int i=0;i<qianCardArray.length;i++){
			qianCards.add(qianCardArray[i]);
		}
		qian.setCards(qianCards);
		
		//孙
		User sun = userList.get(2);
		int [] sunCardArray = {2,15,34,40,46,56,58,71,76,85,92,95,110};
		List<Integer> sunCards = new LinkedList<Integer>();
		for(int i=0;i<sunCardArray.length;i++){
			sunCards.add(sunCardArray[i]);
		}
		sun.setCards(sunCards);
		
		//李
		User li = userList.get(3);
		int [] liCardArray = {1,9,11,12,14,27,47,50,66,98,105,106,108};
		List<Integer> liCards = new LinkedList<Integer>();
		for(int i=0;i<liCardArray.length;i++){
			liCards.add(liCardArray[i]);
		}
		li.setCards(liCards);
		
		//剩余的牌
		int[] remainArray = {86,57,43,20,0,41,74,69,100,82,10,38,90,26,77,51,33,67,22,109,3,52,62,35,103,55,68,8,73,83,30,70,93,48,29,84,96,104,99,111,37,89,17,75,28,31,19,102,72,63,45,42,91,97,32,107,87,81,60};
		List<Integer> remainList = new LinkedList<Integer>();
		for (int i=0;i<remainArray.length;i++){
			remainList.add(remainArray[i]);
		}
		return remainList;
	}
	
	/**两个能杠的牌
	 * @param room
	 * @param cardList
	 * @return
	 */
	private List<Integer> liangGeNengGangDePai(OneRoom room, List<Integer> cardList) {
		List<User> userList = room.getUserList();
		
		int [][]notBankCardArray = {
									{8,9,10,11,12,13,14,15,106, 82, 36, 41, 91},
									{16,17,18,19,20,21,22,23,40, 35, 62, 78, 108},
									{24,25,26,27,28,29,30,31,70, 42, 33, 72, 54}
								};
		int begin = 0;
		for(int i=0;i<userList.size();i++){
			User user = userList.get(i);
			List<Integer> userCards = new LinkedList<Integer>();
			if(user.isBanker()){//庄家
				int [] bankCardArray = {0,1,2,3,4,5,6,7,103, 49, 105, 95, 89, 93};
				for(int j=0;j<bankCardArray.length;j++){
					userCards.add(bankCardArray[j]);
				}
			}else{//不是庄家
				for(int j=0;j<notBankCardArray[begin].length;j++){
					userCards.add(notBankCardArray[begin][j]);
				}
				begin++;
			}
			Collections.sort(userCards);
			user.setCards(userCards);
		}
		//剩余的牌
		int[] remainArray = {110, 50, 68, 94, 98, 34, 32, 56, 86, 38, 43, 75, 109, 101, 102, 66, 92, 79, 55, 67, 45, 71, 64, 59, 58, 96, 53, 99, 61, 73, 111, 52, 85, 63, 77, 48, 80, 107, 100, 88, 83, 39, 74, 69, 97, 57, 87, 65, 90, 37, 51, 47, 76, 84, 104, 44, 46, 60, 81};
		List<Integer> remainList = new LinkedList<Integer>();
		for (int i=0;i<remainArray.length;i++){
			remainList.add(remainArray[i]);
		}
		return remainList;
	}
	
	
	/**两个能杠的牌
	 * @param room
	 * @param cardList
	 * @return
	 */
	private List<Integer> zidongCards(OneRoom room, List<Integer> cardList) {
		List<User> userList = room.getUserList();
		//赵
		User zhao = userList.get(0);
		int [] zhaoCardArray = {27,40,51,59,63,65,68,70,73,76,79,91,93,97};
		List<Integer> zhaoCards = new LinkedList<Integer>();
		for(int i=0;i<zhaoCardArray.length;i++){
			zhaoCards.add(zhaoCardArray[i]);
		}
		zhao.setCards(zhaoCards);
		
		//钱
		User qian = userList.get(1);
		int [] qianCardArray = {4,5,14,16,23,30,47,64,67,77,86,102,106};
		List<Integer> qianCards = new LinkedList<Integer>();
		for(int i=0;i<qianCardArray.length;i++){
			qianCards.add(qianCardArray[i]);
		}
		qian.setCards(qianCards);
		
		//孙
		User sun = userList.get(2);
		int [] sunCardArray = {8,12,24,25,46,55,58,71,83,90,95,103,111};
		List<Integer> sunCards = new LinkedList<Integer>();
		for(int i=0;i<sunCardArray.length;i++){
			sunCards.add(sunCardArray[i]);
		}
		sun.setCards(sunCards);
		
		//李
		User li = userList.get(3);
		int [] liCardArray = {2,11,28,29,32,35,41,52,60,61,89,99,110};
		List<Integer> liCards = new LinkedList<Integer>();
		for(int i=0;i<liCardArray.length;i++){
			liCards.add(liCardArray[i]);
		}
		li.setCards(liCards);
		
		//剩余的牌
		int[] remainArray = {72, 101, 78, 3, 38, 44, 53, 9, 1, 75, 22, 100, 82, 85, 17, 104, 21, 37, 57, 36, 94, 49, 15, 6, 43, 10, 74, 19, 31, 56, 81, 62, 105, 26, 109, 54, 87, 13, 45, 66, 69, 0, 108, 18, 88, 50, 84, 7, 80, 39, 42, 33, 98, 92, 48, 20, 96, 34, 107};
		List<Integer> remainList = new LinkedList<Integer>();
		for (int i=0;i<remainArray.length;i++){
			remainList.add(remainArray[i]);
		}
		return remainList;
	}
	/**两个能杠的牌
	 * @param room
	 * @param cardList
	 * @return
	 */
	private List<Integer> wangluo(OneRoom room, List<Integer> cardList) {
		List<User> userList = room.getUserList();
		//赵
		User zhao = userList.get(0);
		int [] zhaoCardArray = {11,13,16,42,53,62,64,66,67,71,75,86,87,103};
		List<Integer> zhaoCards = new LinkedList<Integer>();
		for(int i=0;i<zhaoCardArray.length;i++){
			zhaoCards.add(zhaoCardArray[i]);
		}
		zhao.setCards(zhaoCards);
		
		//钱
		User qian = userList.get(1);
		int [] qianCardArray = {0,15,25,27,45,47,48,51,54,63,84,104,107};
		List<Integer> qianCards = new LinkedList<Integer>();
		for(int i=0;i<qianCardArray.length;i++){
			qianCards.add(qianCardArray[i]);
		}
		qian.setCards(qianCards);
		
		//孙
		User sun = userList.get(2);
		int [] sunCardArray = {6,22,39,49,61,69,70,72,79,83,88,91,99};
		List<Integer> sunCards = new LinkedList<Integer>();
		for(int i=0;i<sunCardArray.length;i++){
			sunCards.add(sunCardArray[i]);
		}
		sun.setCards(sunCards);
		
		//李
		User li = userList.get(3);
		int [] liCardArray = {9,14,17,20,29,33,34,36,41,59,60,90,109};
		List<Integer> liCards = new LinkedList<Integer>();
		for(int i=0;i<liCardArray.length;i++){
			liCards.add(liCardArray[i]);
		}
		li.setCards(liCards);
		
		//剩余的牌
		int[] remainArray = {12, 111, 46, 26, 31, 18, 38, 28, 8, 85, 19, 105, 24, 78, 82, 23, 65, 52, 98, 30, 74, 40, 68, 94, 100, 81, 37, 101, 92, 77, 76, 1, 7, 95, 43, 97, 58, 102, 55, 32, 89, 3, 56, 93, 110, 44, 4, 57, 10, 21, 80, 50, 106, 2, 108, 73, 96, 5, 35};
		List<Integer> remainList = new LinkedList<Integer>();
		for (int i=0;i<remainArray.length;i++){
			remainList.add(remainArray[i]);
		}
		return remainList;
	}

	@Override
	public String toString() {
		return "Game [autoPengCardId=" + autoPengCardId + ", autoGangCardId=" + autoGangCardId + ", canPengUser="
				+ canPengUser + ", canGangUser=" + canGangUser + ", canGongGangUser=" + canGongGangUser
				+ ", canAnGangUser=" + canAnGangUser + "]";
	}
	
	
	
}

package com.zxz.domain;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Scrollable;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.zxz.controller.GameManager;
import com.zxz.controller.RoomManager;
import com.zxz.controller.StartServer;
import com.zxz.service.UserService;
import com.zxz.utils.Algorithm2;
import com.zxz.utils.CardsMap;
import com.zxz.utils.ScoreType;

public class User {
	
	private static Logger logger = Logger.getLogger(User.class);  
	int id;
	/******************微信的所有东西**********************/
	String openid;//普通用户的标识，对当前开发者帐号唯一
	String nickName;//昵称 
	private String sex;//普通用户性别，1为男性，2为女性
	String province	;//普通用户个人资料填写的省份
	String city	;//普通用户个人资料填写的城市
	String country	;//国家，如中国为CN
	String unionid;//用户统一标识。针对一个微信开放平台帐号下的应用，同一用户的unionid是唯一的。
	String headimgurl;//头像
	String refreshToken;//refresh_token拥有较长的有效期（30天），当refresh_token失效的后，需要用户重新授权。
	/*******************微信的所有东西*********************/
	Date createDate;//注册时间
	int roomCard;//房卡的数量
	String userName;
	String password;
	String roomId;//房间号 
	boolean ready;//是否准备
	IoSession ioSession;
	private List<Integer> cards;//自己手中的牌
	String direction;//东 西 南 北
	boolean banker;//庄
	boolean isAuto = false;//用户是否托管
	int pengOrGang=0;// 0 不可以牌和杠 1 可以碰 2 可以杠 
	private List<Integer> myPlays = new ArrayList<>();//已经出的的牌
	private List<Integer> pengCards = new ArrayList<>();//碰出的牌
	List<GangCard> gangCards = new ArrayList<>();//杠出去的牌
	private Integer myGrabCard;//我抓到的牌
	boolean isCanPeng = false;//是不是可以碰
	boolean isCanGang = false;//是不是可以杠
	/**
	 * 得分记录的集合,key:第几局，Score 该局的得分
	 */
	private Map<Integer, Score> scoreMap =  new LinkedHashMap<>();
	
	/**
	 * 当前所在的局数
	 */
	int currentGame;
	
	Date lastChuPaiDate;//最后一次的出牌时间
	List<Integer> pengOrGangList;//可以碰或者杠的牌
	
	/**
	 *  说明:此变量是为了，解决当用户托管的时候，用户出牌和系统托管的时候，一起出牌，造成打两次牌的情况
	 */
	boolean isUserCanPlay = false;//false
	int recommendId;//推荐人id
	
	
	private int totalNotPlay = 0;//用户没有出牌的次数
	
	
	public int getTotalNotPlay() {
		return totalNotPlay;
	}


	public void setTotalNotPlay(int totalNotPlay) {
		this.totalNotPlay = totalNotPlay;
	}


	/**
	 * 清空所有的属性
	 */
	public void clearAll(){
		this.roomId = null;
		this.isAuto = false;
		this.currentGame = 0;
		this.setPengCards(new ArrayList<Integer>());
		List<GangCard> gangCards = new ArrayList<>();//杠的牌
		this.setGangCards(gangCards);
		this.myGrabCard = -100;//我找到的牌
		this.isCanPeng = false;//是不是可以碰
		this.isCanGang = false;//是不是可以杠
		this.banker = false;//是否是房主
		this.currentGame = 0;//当前的局数设置为0
		this.myPlays = new ArrayList<Integer>();//打出的牌清空
		this.setScoreMap(new LinkedHashMap<Integer,Score>());//清空成绩
		this.totalNotPlay = 0;//用户没有出牌的次数清零
		logger.info("清空后当前用户已经打出的牌:" + this.getMyPlays());
	}
	
	
	public int getRecommendId() {
		return recommendId;
	}

	public void setRecommendId(int recommendId) {
		this.recommendId = recommendId;
	}

	public boolean isUserCanPlay() {
		return isUserCanPlay;
	}

	public void setUserCanPlay(boolean isUserCanPlay) {
		this.isUserCanPlay = isUserCanPlay;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getSex() {
		if(sex==null){
			return "0";//男
		}else if(sex.equals("2")){
			return "1";//女
		}else if(sex.equals("1")){
			return "0";//男
		}
		return sex==null?"0":sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getUnionid() {
		return unionid;
	}

	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}

	public List<Integer> getPengOrGangList() {
		return pengOrGangList;
	}

	public void setPengOrGangList(List<Integer> pengOrGangList) {
		this.pengOrGangList = pengOrGangList;
	}

	public Date getLastChuPaiDate() {
		return lastChuPaiDate;
	}

	public void setLastChuPaiDate(Date lastChuPaiDate) {
		this.lastChuPaiDate = lastChuPaiDate;
	}

	public List<Integer> getPengCards() {
		return pengCards;
	}

	public void setPengCards(List<Integer> pengCards) {
		this.pengCards = pengCards;
	}

	public List<GangCard> getGangCards() {
		return gangCards;
	}

	public void setGangCards(List<GangCard> gangCards) {
		this.gangCards = gangCards;
	}

	public int getCurrentGame() {
		return currentGame;
	}

	public void setCurrentGame(int currentGame) {
		this.currentGame = currentGame;
	}

	public Map<Integer, Score> getScoreMap() {
		return scoreMap;
	}

	public void setScoreMap(Map<Integer, Score> scoreMap) {
		this.scoreMap = scoreMap;
	}

	public int getRoomCard() {
		return roomCard;
	}

	public void setRoomCard(int roomCard) {
		this.roomCard = roomCard;
	}

	public boolean isCanPeng() {
		return isCanPeng;
	}

	public void setCanPeng(boolean isCanPeng) {
		this.isCanPeng = isCanPeng;
	}

	public boolean isCanGang() {
		return isCanGang;
	}

	public void setCanGang(boolean isCanGang) {
		this.isCanGang = isCanGang;
	}

	public Integer getMyGrabCard() {
		return myGrabCard;
	}

	public void setMyGrabCard(Integer myGrabCard) {
		this.myGrabCard = myGrabCard;
	}

	public List<Integer> getMyPlays() {
		return myPlays;
	}

	public void setMyPlays(List<Integer> myPlays) {
		this.myPlays = myPlays;
	}



	public int getPengOrGang() {
		return pengOrGang;
	}

	public void setPengOrGang(int pengOrGang) {
		this.pengOrGang = pengOrGang;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public boolean isAuto() {
		return isAuto;
	}

	public void setAuto(boolean isAuto) {
		this.isAuto = isAuto;
	}

	public User() {
	}
	
	public boolean isBanker() {
		return banker;
	}
	public void setBanker(boolean banker) {
		this.banker = banker;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public List<Integer> getCards() {
		return cards;
	}
	public void setCards(List<Integer> cards) {
		this.cards = cards;
	}
	public IoSession getIoSession() {
		return ioSession;
	}
	public void setIoSession(IoSession ioSession) {
		this.ioSession = ioSession;
	}
	public boolean isReady() {
		return ready;
	}
	public void setReady(boolean ready) {
		this.ready = ready;
	}
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return getNickName();
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public User(String userName, String password) {
		super();
		this.userName = userName;
		this.password = password;
	}
	
	
	/**得到用户的详细信息,包括,是否托管、已经打出的牌、手中的牌、碰的牌、杠的牌、自己方向 
	 * @param showMyCards 是否显示我手中的牌，true显示,false不显示
	 * @return
	 */
	public JSONObject getMyInfo(boolean showMyCards){
		JSONObject infoJSONObject = new JSONObject();
		infoJSONObject.put("isAuto", isAuto);//是否托管
		infoJSONObject.put("myPlays", myPlays);//已经打出的牌
		if(showMyCards){
			infoJSONObject.put("cards", cards);//手中的牌
		}
		infoJSONObject.put("pengCards", pengCards);
		JSONArray gangCardArray = new JSONArray();//杠牌的数组
		for(int i=0;i<gangCards.size();i++){
			GangCard gangCard = gangCards.get(i);
			JSONObject gangJsonObject = new JSONObject();
			int type = gangCard.getType();
			if (type==0) {
				gangJsonObject.put("gangType", "jieGang");
			}else if(type == 1){
				gangJsonObject.put("gangType", "anGang");
			}else if(type == 2){
				gangJsonObject.put("gangType", "gongGang");
			}
			List<Integer> gangCards = gangCard.getCards();
			gangJsonObject.put("gangCards", gangCards);
			gangCardArray.put(gangJsonObject);
		}
		infoJSONObject.put("userid",id);
		infoJSONObject.put("headimgurl", headimgurl);
		infoJSONObject.put("userName", getUserName());
		infoJSONObject.put("gangCardArray", gangCardArray);
		infoJSONObject.put("direction", direction);
		infoJSONObject.put("ready", ready);
		infoJSONObject.put("ip", getIp());//ip地址
		int playerScoreByAdd = UserService.getUserCurrentGameScore(this);//当前的分数
		infoJSONObject.put("playerScoreByAdd", playerScoreByAdd);
		infoJSONObject.put("sex", getSex());
		return infoJSONObject;
	}
	
	
	
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("  ");
		for(int i=0;i<cards.size();i++){
			Integer card = cards.get(i);
			String cardType = CardsMap.getCardType(card);
			sb.append(cardType+"");
		}
//		Score score = scoreMap.get(currentGame);
//		return "手中的牌:"+sb.toString()+" 本局的得分是："+score.getFinalScore()+" 是否托管:"+isAuto+" 得分记录:"+scoreMap;
		StringBuffer sb2 = new StringBuffer("  ");
		for(int i=0;i<myPlays.size();i++){
			Integer card = myPlays.get(i);
			String cardType = CardsMap.getCardType(card);
			sb2.append(cardType+"");
		}
		return "手中的牌:"+sb.toString()+"出去的牌"+sb2;
	}

	/**出牌
	 * @param cardId 出牌的牌号
	 */
	public int chuPai(int cardId) {
		int removeCard = -10; //移除掉的牌
		if(!isUserCanPlay){
			return -1000;
		}
		isUserCanPlay = false;
		for(int i=0;i<cards.size();i++){
			Integer cardNumber = cards.get(i);
			if(cardNumber==cardId){
				removeCard = cards.remove(i);//把该张牌移除
				break;
			}
		}
		return removeCard;
	}
	
	
	
	
	/**
	 * 自动出牌
	 * 如果这里改成托管，以后可能重点改这里.........
	 * @return 出的牌牌号
	 */
	public int autoChuPai1(){
		int[] array = getCardsArray();
//		int[] array ={16,24,25,52,60,64,68,72,84,88,89,96,108};
		List<Integer> mycards = Algorithm2.getListWithoutHongZhong(array);
		mycards = Algorithm2.getCardsWithoutSen(mycards);
		//Algorithm2.showPai(mycards);
		mycards = Algorithm2.getCardsWithoutCan(mycards);
		Integer card = mycards.get(0);
		//cards.remove(card);
		return card;
	}
	
	public static void main(String[] args) {
		long currentTimeMillis1 = System.currentTimeMillis();
		User user = new User();
		user.autoChuPai();
		long currentTimeMillis2 = System.currentTimeMillis();
		System.out.println(currentTimeMillis2-currentTimeMillis1);
	}
	
	/**智能出牌
	 * @return
	 */
	public  synchronized int autoChuPai2(){
//		if(!isUserCanPlay){
//			return -100;
//		}
//		isUserCanPlay = false; //现在不可以打牌了
//		int[] array = getCardsArray();
//		int[] array ={16,24,25,52,60,64,68,72,84,88,89,96,108};
		int[] array ={3, 8, 16, 22, 29, 30, 48, 56, 58, 73, 74, 75, 82, 105};;
		List<Integer> mycards = Algorithm2.getListWithoutHongZhong(array);
		mycards = Algorithm2.getCardsWithoutSen(mycards);
		Algorithm2.showPai(mycards);
		mycards = Algorithm2.getCardsWithoutCan(mycards);
		Algorithm2.showPai(mycards);
		Integer card = mycards.get(0);
		String cardType = CardsMap.getCardType(card);
		System.out.println(cardType);
		//cards.remove(card);
		return card;
	}
	
	
	/**智能出牌
	 * @return
	 */
	public  synchronized int autoChuPai(){
		if(!isUserCanPlay){
			return -100;
		}
		isUserCanPlay = false; //现在不可以打牌了
		Integer myGrabCard = this.getMyGrabCard();//抓到的牌
		int[] array = getCardsArray();
		if(myGrabCard != null){
			List<Integer> cards = this.getCards();
			boolean win = isUserTingPai(myGrabCard,cards);
			if(win){//如果听牌
				logger.info("自动出牌的时候检测到听牌,用户手中牌是："+ Algorithm2.showPai(array));
				cards.remove(myGrabCard);
				return myGrabCard;
			}
		}
//		int[] array ={3, 8, 16, 22, 29, 30, 48, 56, 58, 73, 74, 75, 105, 109};
		Integer maxWeightCardId = getWitchCardToPlay(array);
//		String cardType = CardsMap.getCardType(maxWeightCardId);
//		System.out.println(cardType);
		cards.remove(maxWeightCardId);
		return maxWeightCardId;
	}


	/**检测用户是否听牌
	 * @param myGrabCard
	 * @return
	 */
	public static boolean isUserTingPai(Integer myGrabCard,List<Integer> cards) {
		logger.info("抓到的牌是:"+myGrabCard+" "+ CardsMap.getCardType(myGrabCard));
		int[] tingPaiCars = getCardsWithRemoveDrawCardsWithReplaceHongZhong(myGrabCard,cards);//计算要听的牌
		boolean win = Algorithm2.isWin(tingPaiCars);
		return win;
	}

	
	
	/**在检测到用户碰牌的时候，检测用户是否听牌
	 * @param myGrabCard
	 * @return
	 */
	public static boolean isUserTingPaiOfPengOrGang(List<Integer> cards) {
		int[] tingPaiCars = getCardsWithAddHongZhong(cards);//计算要听的牌
		boolean win = Algorithm2.isWin(tingPaiCars);
		return win;
	}

	/**在用户碰牌或者是杠牌的时候检测用户是否听牌,从而返回牌
	 * @return
	 */
	private static int[] getCardsWithAddHongZhong(List<Integer> cards){
		List<Integer> returnCards = new LinkedList<Integer>();
		for(int i=0;i<cards.size();i++){
			Integer card = cards.get(i);
			returnCards.add(card);
		}
		returnCards.add(108);
		Collections.sort(returnCards);
		int[] array = new int[returnCards.size()];
		for(int i=0;i<returnCards.size();i++){
			array[i] = returnCards.get(i);
		}
		return array;
	}
	
	

	/**计算出哪一张牌最合适
	 * @param array
	 * @return
	 */
	private Integer getWitchCardToPlay(int[] array) {
		Algorithm2.showPai(array);
		List<Integer> mycards = Algorithm2.getListWithoutHongZhong(array);//没有红中的牌
		Map<Integer,Integer> cardWeightMap = new LinkedHashMap<>();
		for(int i=0;i<mycards.size();i++){
			Integer cardId = mycards.get(i);//当前的牌号
			Integer preCardId;//前一张牌
			Integer nextCardId;//后一张牌
			if(i==0){
				preCardId = -1000;
			}else{
				preCardId = mycards.get(i-1);
			}
			if(i==mycards.size()-1){
				nextCardId = 1000;
			}else{
				nextCardId = mycards.get(i+1);
			}
			int intervalPre= Algorithm2.getInterval(preCardId,cardId);
			int intervalNext = Algorithm2.getInterval(cardId, nextCardId);
			int weight = getCardWeight(intervalPre, intervalNext);
			cardWeightMap.put(cardId, weight);
		}
		Iterator<Integer> iterator = cardWeightMap.keySet().iterator();
		int weight = Integer.MIN_VALUE ;//最大权重的那张牌
		Integer maxWeightCardId = 0;
		while(iterator.hasNext()){
			Integer cardId = iterator.next();
			Integer cardWeight = cardWeightMap.get(cardId);
			//System.out.println(CardsMap.getCardType(cardId)+" 权重 : "+cardWeight);
			if(cardWeight>weight){
				weight = cardWeight;
				maxWeightCardId = cardId;
			}
		}
		return maxWeightCardId;
	}

	
	/**移除掉抓到的牌，并且用一颗红中来替换
	 * @return
	 */
	private static int[] getCardsWithRemoveDrawCardsWithReplaceHongZhong(Integer myDrawCard,List<Integer> cards){
		//Algorithm2.showPai(cards);
		List<Integer> returnCards = new LinkedList<Integer>();
		for(int i=0;i<cards.size();i++){
			Integer card = cards.get(i);
			if(card!=myDrawCard){
				returnCards.add(card);
			}
		}
		returnCards.add(108);
		Collections.sort(returnCards);
		int[] array = new int[returnCards.size()];
		for(int i=0;i<returnCards.size();i++){
			array[i] = returnCards.get(i);
		}
		return array;
	}
	
	
	
	
	
	/**判断用户是否听牌
	 * @param array
	 * @return
	 */
	public boolean isTingPai(int array[]){
		
		
		return false;
	}
	
	
	/**得到该张牌的权重
	 * @param intervalPre
	 * @param intervalNext
	 * @return
	 */
	private int getCardWeight(int intervalPre, int intervalNext) {
		int weight = 0;
		//如果是孤立的一张牌
		if(intervalPre==-1&&intervalNext==-1){
			weight = 10000;
		}else{
			if(intervalPre==1){//可能是一句话
				weight = weight-5000;
			}else if(intervalPre==0){//可能组成坎
				weight = weight - 10000;
			}else if(intervalPre == 2){
				weight = weight - 3000;
			}else{
				weight = weight+intervalPre;
			}
			if(intervalNext == 1){//可能是一句话
				weight = weight - 5000;
			}else if(intervalNext==0){//可能组成坎
				weight = weight - 10000;
			}else if(intervalNext==2){
				weight = weight - 3000;
			}else{
				weight = weight+intervalNext;
			}
		}
		return weight;
	}
	
	
	/**抓牌
	 * @param cardId
	 */
	public boolean zhuaPai(int cardId){
		cards.add(cardId);
		Collections.sort(cards);//抓完牌排序
		setLastChuPaiDate(new Date());//设置本次的出牌时间
		//计算他是否胡牌
		boolean win = Algorithm2.isWin(cards);
		if(win){
			System.out.println("胡牌了");
			//把游戏停止下来
			return true;
		}else{
			logger.fatal("未胡的牌是:"+cards+" "+Algorithm2.showPai(cards));
			//记录下当前用户抓的牌是什么
			logger.info(this.nickName+" 当前抓到的牌是:"+cardId);
			this.myGrabCard = cardId;//当前抓到的牌
		}
		return false;
	}
	
	
	
	
	
	
	/**在出去的牌的集合中添加一张牌
	 * @param card
	 */
	public void addMyPlays(int card){
		myPlays.add(card);
	}
	

	
	/**碰牌
	 * @param list 需要碰掉的牌
	 */
	public void userPengCards(List<Integer> list){
		for(int i=0;i<list.size();i++){
			Integer removeCard = list.get(i);
			cards.remove(removeCard);
		}
		setCanPeng(false);
	}
	
	
	/**添加用户碰出的牌
	 * @param cards
	 */
	public void addUserPengCards(List<Integer> cards){
		for(int i=0;i<cards.size();i++){
			Integer card = cards.get(i);
			pengCards.add(card);
		}
	}
	
	/**杠牌
	 * @param list 需要杠掉的牌
	 */
	public void userGangCards(List<Integer> list){
		for(int i=0;i<list.size();i++){
			Integer removeCard = list.get(i);
			cards.remove(removeCard);
		}
		setCanGang(false);
	}
	
	/**接杠加分
	 */
	public void addScoreForCommonGang(){
		Score score = scoreMap.get(currentGame);
		score.setJieGangTotal(score.getJieGangTotal()+1);//接杠的次数加1
		score.setFinalScore(score.getFinalScore()+ScoreType.ADD_SCORE_FOR_GANG);
	}
	
	/**公杠减分
	 */
	public void reduceScoreForFangGang(){
		Score score = scoreMap.get(currentGame);
		System.out.println(this);
		if(score==null){
			Score score2 = new Score();
			Map<Integer, Score> scoreMap = getScoreMap();
			scoreMap.put(getCurrentGame(), score2);
			score = score2;
			logger.error("用户的分数怎么会为空");
		}
		
		score.setFangGangTotal(score.getFangGangTotal()+1);//放杠的次数减1
		score.setFinalScore(score.getFinalScore()+ScoreType.REDUCE_SCORE_FOR_GANG);
	}
	
	
	/**
	 * 明杠加分
	 */
	public void addScoreForMingGang(){
		Score score = scoreMap.get(currentGame);
		score.setMingGangtotal(score.getMingGangtotal()+1);
		score.setFinalScore(score.getFinalScore()+ScoreType.ADD_SCORE_FOR_MINGGANG);
	}
	
	
	/**
	 * 明杠减分
	 */
	public void reduceScoreForMingGang(){
		Score score = scoreMap.get(currentGame);
		//score.setMingGangtotal(score.getMingGangtotal()+1);
		score.setFinalScore(score.getFinalScore()+ScoreType.REDUCE_SCORE_FOR_MINGGANG);
	}
	
	/**
	 * 胡牌得分
	 */
	public void addScoreForHuPai(){
		Score score = scoreMap.get(currentGame);
		score.setHuPaiTotal(score.getHuPaiTotal()+1);
		score.setFinalScore(score.getFinalScore()+ScoreType.ADD_SCORE_FOR_HUPAI);
	}
	
	/**
	 * 胡牌减分
	 */
	public void reduceScoreForHuPai(){
		Score score = scoreMap.get(currentGame);
		score.setFinalScore(score.getFinalScore()+ScoreType.REDUCE_SCORE_FOR_HUPAI);
	}
	
	
	/**中码加分
	 * @param totalZhongMa 中码的个数
	 */
	public void addScoreForZhongMa(int totalZhongMa){
		Score score = scoreMap.get(currentGame);
		score.setZhongMaTotal(score.getZhongMaTotal()+totalZhongMa);
		score.setFinalScore(score.getFinalScore() + totalZhongMa*3*2);
	}
	
	
	/**中码减分
	 * @param totalZhongMa
	 */
	public void reduceScoreForZhongMa(int totalZhongMa){
		Score score = scoreMap.get(currentGame);
		score.setFinalScore(score.getFinalScore() - totalZhongMa*2);
	}
	
	
	/**
	 * 暗杠得分
	 */
	public void addScoreForAnGang(){
		Score score = scoreMap.get(currentGame);
		score.setAnGangTotal(score.getAnGangTotal()+1);
		score.setFinalScore(score.getFinalScore()+ScoreType.ADD_SCORE_FOR_ANGANG);
	}
	
	/**
	 * 暗杠减分
	 */
	public void reduceScoreForAnGang(){
		Score score = scoreMap.get(currentGame);
		score.setFinalScore(score.getFinalScore()+ScoreType.REDUCE_SCORE_FOR_ANGANG);
	}
	
	
	/**记录用户杠出的牌
	 * @param type 0 放杠  1、暗杠  2、公杠 /明杠
	 * @param cards 杠出的牌
	 */
	public void recordUserGangCards(int type,List<Integer> cards){
		GangCard gangCard = new GangCard(type, cards);
		gangCards.add(gangCard);
	}
	
	/**得到本局的得分
	 * @return
	 */
	public int getCurrentGameSore(){
		Score score = scoreMap.get(currentGame);
		return score.getFinalScore();
	}
	
	/**从自己的牌中移除公杠的那张牌
	 * @param card
	 */
	public void removeCardFromGongGang(Integer cardId){
		cards.remove(cardId);
	}

	
	/**将牌转成数组
	 * @return
	 */
	public int[] getCardsArray(){
		int arr[]= new int[cards.size()];
		for(int i=0;i<cards.size();i++){
			arr[i] = cards.get(i);
		}
		return arr;
	}
	

	/**得到用户的IP
	 * @return
	 */
	public String getIp(){
		SocketAddress remoteAddress = this.ioSession.getRemoteAddress();
		return remoteAddress.toString().replaceAll("/", "");
	}
	
}

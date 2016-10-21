package com.zxz.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;




public class PaiXing {

	public static void main(String[] args) {
		Person person1 = new Person(1,"赵");
		Person person2 = new Person(2,"钱");
		Person person3 = new Person(3,"孙");
		Person person4 = new Person(4,"李");
		
		//进入房间
		Room room = new Room();
		room.addPerson(person1);
		room.addPerson(person2);
		room.addPerson(person3);
		room.addPerson(person4);
		
		//生成座次
		Map<Object, Person> seatMap = generateSeat(room); 
		
		Map<Integer, Person> safeCheckMap = enterRoom(person1, person2,
				person3, person4);
		// 生成牌
		List<Integer> paiList = creatMajiang2();
		
		//发牌
		List<Integer> remainCards = DealsCards(paiList, person1, person2, person3, person4);
		
		//游戏准备
		prepareGame(person1, person2, person3, person4);
		//游戏开始
		gameStart2(seatMap,remainCards,safeCheckMap);
		
	}



	private static Map<Integer, Person> enterRoom(Person person1,
			Person person2, Person person3, Person person4) {
		Map<Integer, Person> safeCheckMap = new HashMap<Integer, Person>();
		safeCheckMap.put(person1.getId(), person1);
		safeCheckMap.put(person2.getId(), person2);
		safeCheckMap.put(person3.getId(), person3);
		safeCheckMap.put(person4.getId(), person4);
		return safeCheckMap;
	}

	/**开始游戏
	 * @param seatMap 座次
	 * @param remainCards 未发出的牌
	 * @param safeCheckMap 
	 */
	private static void gameStart(Map<Object, Person> seatMap, List<Integer> remainCards, Map<Integer, Person> safeCheckMap) {
		//开始打牌
		Person dealerPerson = seatMap.get("庄");
		System.out.println("庄家为:"+ dealerPerson.getName()+"   id:    "+dealerPerson.getId());
		showZuoCi(seatMap);
		Scanner scan = new Scanner(System.in);
		String wicthPlay = dealerPerson.getDirection();
		int step =  1;//出牌的步数
		Stack<Integer> chuPaiStatic = new Stack<Integer>();//出牌的栈
		String dirctionToPlay = "";
		while(remainCards.size()>0){
			if(step==1){//庄家先出牌
				System.out.println("请 id为:"+dealerPerson.getId()+" 昵称为: "+dealerPerson.getName()+"  出牌");
				showPai(dealerPerson.getPaiList());
				System.out.println(dealerPerson.getPaiList());
				String paiHao = scan.nextLine(); //要打出的牌号
				System.out.println(seatMap.get(wicthPlay).getName()+"   打出的牌是:"+paiHao+" \t"+InitialPuKe.map.get(Integer.parseInt(paiHao)));
				boolean chuPaiSuccess = chuPai(Integer.parseInt(paiHao), dealerPerson.getPaiList());
				if(chuPaiSuccess){
					Integer removePaiId = remainCards.remove(0);
					chuPaiStatic.push(Integer.parseInt(paiHao));
					System.out.println("移除的牌是: id"+removePaiId+"\t"+InitialPuKe.map.get(removePaiId));
					dirctionToPlay = getNextDirectionToPlay(dealerPerson.getDirection());
					System.out.println("下一个出牌的玩家方向是:"+dirctionToPlay+"\t"+seatMap.get(dirctionToPlay).getName());
				}
			}else{//后续出牌
				showPai(seatMap.get(dirctionToPlay).getPaiList());
				System.out.println(seatMap.get(dirctionToPlay).getPaiList());
				String paiHao = scan.nextLine();
				boolean chuPaiSuccess = chuPai(Integer.parseInt(paiHao),seatMap.get(dirctionToPlay).getPaiList());
				System.out.println(seatMap.get(dirctionToPlay).getName()+" 打出的牌是:"+paiHao+"  "+InitialPuKe.map.get(Integer.parseInt(paiHao)));
				if(chuPaiSuccess){
					Integer removePaiId = remainCards.remove(0);
					chuPaiStatic.push(Integer.parseInt(paiHao));
					System.out.println("移除的牌是: id"+removePaiId+"\t"+InitialPuKe.map.get(removePaiId));
					dirctionToPlay = getNextDirectionToPlay(seatMap.get(dirctionToPlay).getDirection());
					System.out.println("下一个出牌的玩家方向是:"+dirctionToPlay+"\t"+seatMap.get(dirctionToPlay).getName());
				}
			}
			step ++;
			System.out.println(step);
		}
	}

	/**开始游戏
	 * @param seatMap 座次
	 * @param remainCards 未发出的牌
	 * @param safeCheckMap 
	 */
	private static void gameStart2(Map<Object, Person> seatMap, List<Integer> remainCards, Map<Integer, Person> safeCheckMap) {
		//开始打牌
		Person dealerPerson = seatMap.get("庄");
		System.out.println("庄家为:"+ dealerPerson.getName()+"   id:    "+dealerPerson.getId());
		showZuoCi(seatMap);
		Scanner scan = new Scanner(System.in);
		String dirctionToPlay = dealerPerson.getDirection();
		int step =  1;//出牌的步数
		Stack<Integer> chuPaiStatic = new Stack<Integer>();//出牌的栈
		while(remainCards.size()>0){
			showEachStepInfo(seatMap, dirctionToPlay, step);
			showPai(seatMap.get(dirctionToPlay).getPaiList());
			//System.out.println(seatMap.get(dirctionToPlay).getPaiList());
			System.out.println("请选择出牌的牌号:");
			String paiHao = scan.nextLine(); //要打出的牌号
			System.out.println(seatMap.get(dirctionToPlay).getName()+"   打出的牌是:"+paiHao+" \t"+InitialPuKe.map.get(Integer.parseInt(paiHao)));
			boolean chuPaiSuccess = chuPai(Integer.parseInt(paiHao), seatMap.get(dirctionToPlay).getPaiList());
			if(chuPaiSuccess){
				//计算出可以碰牌和杠牌的人
				String direction = "";//getWitchPersonCanPengPaiOrGangPai(seatMap,dirctionToPlay,Integer.parseInt(paiHao));
				if(!"".equals(direction)){
					String name = seatMap.get(direction).getName();
					System.out.println("姓名为:"+name+"的碰还是不碰?0:碰/1不碰");
					int xuanZe = scan.nextInt();
					if(xuanZe==0){//碰
						List<Integer> paiList = seatMap.get(direction).getPaiList();
						String removeType = InitialPuKe.map.get(Integer.parseInt(paiHao));
						for(int i=0;i<paiList.size();i++){
							Integer number = paiList.get(i);
							String type = InitialPuKe.map.get(number);
							if(type.equals(removeType)){
								paiList.remove(number);
							}
						}
					}else if(xuanZe==1){//不碰
						Integer zhuaLaiDePai = remainCards.remove(0);//抓来的牌
						dirctionToPlay = getNextDirectionToPlay(dirctionToPlay);
						seatMap.get(dirctionToPlay).getPaiList().add(zhuaLaiDePai);
						Collections.sort(seatMap.get(dirctionToPlay).getPaiList());
					}
				}else{//下一个人抓牌,先抓在打
					Integer zhuaLaiDePai = remainCards.remove(0);//抓来的牌
					dirctionToPlay = getNextDirectionToPlay(dirctionToPlay);
					seatMap.get(dirctionToPlay).getPaiList().add(zhuaLaiDePai);
					Collections.sort(seatMap.get(dirctionToPlay).getPaiList());
				}
			}
			step ++;
		}
	}
	
	
	/**开始游戏
	 * @param seatMap 座次
	 * @param remainCards 未发出的牌
	 * @param safeCheckMap 
	 */
	private static void gameStart3(Map<Object, Person> seatMap, List<Integer> remainCards) {
		Scanner scan = new Scanner(System.in);
		String dirctionToPlay = seatMap.get("庄").getDirection();
		int step =  1;//出牌的步数
		//判断是否直接胡牌
		
		
		
		while(remainCards.size()>0){
			showEachStepInfo(seatMap, dirctionToPlay, step);
			showPai(seatMap.get(dirctionToPlay).getPaiList());
			//System.out.println(seatMap.get(dirctionToPlay).getPaiList());
			System.out.println("请选择出牌的牌号:");
			String paiHao = scan.nextLine(); //要打出的牌号
			System.out.println(seatMap.get(dirctionToPlay).getName()+"   打出的牌是:"+paiHao+" \t"+InitialPuKe.map.get(Integer.parseInt(paiHao)));
			boolean chuPaiSuccess = chuPai(Integer.parseInt(paiHao), seatMap.get(dirctionToPlay).getPaiList());
			if(chuPaiSuccess){
				//计算出可以碰牌和杠牌的人
				ChuPaiType chuPaiType = getWitchPersonCanPengPaiOrGangPai(seatMap,dirctionToPlay,Integer.parseInt(paiHao));
				if(chuPaiType!=null){
					String name = seatMap.get(chuPaiType.getChupaiDirection()).getName();
					System.out.println("姓名为:"+name+"的碰还是不碰?0:碰/1不碰");
					String xuanZe = scan.nextLine();
					if(xuanZe.equals("0")){//碰或杠
						if(chuPaiType.getChupaiType()==2){//杠
							Integer zhuaLaiDePai = remainCards.remove(0);//抓来的牌
							seatMap.get(dirctionToPlay).getPaiList().add(zhuaLaiDePai);
							dirctionToPlay = getNextDirectionToPlay(chuPaiType.getChupaiDirection());
							Collections.sort(seatMap.get(dirctionToPlay).getPaiList());
						}else if(chuPaiType.getChupaiType()==1){//碰
							List<Integer> paiList = seatMap.get(chuPaiType.getChupaiDirection()).getPaiList();
							String removeType = InitialPuKe.map.get(Integer.parseInt(paiHao));
							for(int i=0;i<paiList.size();i++){
								Integer number = paiList.get(i);
								String type = InitialPuKe.map.get(number);
								if(type.equals(removeType)){
									paiList.remove(number);
								}
							}
						}
					}else if(xuanZe.equals("1")){//不碰
						dirctionToPlay = normalHit(seatMap, remainCards,
								dirctionToPlay);
					}
				}else{//下一个人抓牌,先抓在打
					dirctionToPlay = normalHit(seatMap, remainCards,
							dirctionToPlay);
				}
			}
			step ++;
		}
	}
	

	/**
	 * @param seatMap
	 * @param remainCards
	 * @param dirctionToPlay
	 * @return
	 */
	private static String normalHit(Map<Object, Person> seatMap,
			List<Integer> remainCards, String dirctionToPlay) {
		Integer zhuaLaiDePai = remainCards.remove(0);//抓来的牌
		dirctionToPlay = getNextDirectionToPlay(dirctionToPlay);
		seatMap.get(dirctionToPlay).getPaiList().add(zhuaLaiDePai);
		Collections.sort(seatMap.get(dirctionToPlay).getPaiList());
		return dirctionToPlay;
	}
	
	/**计算出可以碰牌和杠牌的人的方向
	 * @param seatMap
	 * @param paihao 
	 * @return
	 */
	private static ChuPaiType getWitchPersonCanPengPaiOrGangPai(
			Map<Object, Person> seatMap,String nowDirction, int paihao) {
//		System.out.println("getWitchPersonCanPengPaiOrGangPai");
		Iterator<Object> iterator = seatMap.keySet().iterator();
		String direction = "";
		ChuPaiType chuPaiType = null;
		while(iterator.hasNext()){
			Object key = iterator.next();
			Person person = seatMap.get(key);
			if(!person.getDirection().equals(nowDirction)){//不是当前的人
				Integer[] array = person.getPaiList().toArray(new Integer[person.getPaiList().size()]);
				int type = PengPai.pengPaiOrGangPai(array, paihao);
				if(type>0){
					direction = person.getDirection();
					chuPaiType = new ChuPaiType(direction, type);
					return chuPaiType;
				}
			}
		}
		return chuPaiType;
	}


	/**分析当前牌
	 * @param seatMap
	 * @param dirctionToPlay //出牌的方向
	 * @param paiHao //上一个人出的牌
	 */
	private static void analysis(Map<Object, Person> seatMap,
			String dirctionToPlay, String paiHao) {
		System.out.println("\n\n分析当前的牌,上一个人出的牌是:"+paiHao+"\t"+InitialPuKe.map.get(Integer.parseInt(paiHao)));
		Person person = seatMap.get(dirctionToPlay);
		
		System.out.println("要分析人的id是："+person.getId()+"要分析当前人的昵称是:"+person.getName());
		System.out.println("---------------当前人的牌是----------");
		System.out.print(person.getPaiList());
		showPai(person.getPaiList());
		System.exit(0);
	}



	/**每次出牌的信息
	 * @param seatMap
	 * @param dirctionToPlay
	 * @param step
	 */
	private static void showEachStepInfo(Map<Object, Person> seatMap,
			String dirctionToPlay, int step) {
		if(step==1){
			System.out.println("请 【庄家】id为:"+seatMap.get(dirctionToPlay).getId()+" 昵称为: "+seatMap.get(dirctionToPlay).getName()+"  出牌");
		}else{
			System.out.println("请 id为:"+seatMap.get(dirctionToPlay).getId()+" 昵称为: "+seatMap.get(dirctionToPlay).getName()+"  出牌");
		}
	}

	/**显示座次
	 * @param seatMap
	 */
	private static void showZuoCi(Map<Object, Person> seatMap) {
		System.out.println("----------座次表-----------");
		Person eastPerson = seatMap.get("东");
		Person northPerson = seatMap.get("北");
		Person westPerson = seatMap.get("西");
		Person southPerson = seatMap.get("南");
		System.out.println("东:"+eastPerson.getName()+"\t北:"+northPerson.getName()+"\t西:"+westPerson.getName()+"\t南:"+southPerson.getName());
	}

	/**得到下一个方向的玩家
	 * @param nowDirection
	 * @return
	 */
	public static String getNextDirectionToPlay(String nowDirection){
		String direction = "";//方向
		switch (nowDirection) {
			case "东":
				direction = "北";
				break;
			case "北":
				direction = "西";
				break;
			case "西":
				direction = "南";
				break;
			case "南":
				direction = "东";
				break;
			default:
				break;
		}
		return direction;
	}
	
	
	/**出牌
	 * @param paiId
	 * @param paiList
	 */
	public static boolean chuPai(int paiId,List<Integer> paiList){
		boolean result = false;;
		for(int i=0;i<paiList.size();i++){
			Integer eachPai = paiList.get(i);
			if(paiId==eachPai){
				paiList.remove(i);//把该张牌移除
				result = true;
				break;
			}
		}
		if(result==false){
			System.out.println("没有改张牌");
		}
		return result;
	}
	
	
	/**生成座次
	 * @param room
	 * @return 
	 */
	private static Map<Object, Person> generateSeat(Room room) {
		List<Person> persons = room.getPersons();
		Map<Object, Person> map = new HashMap<Object, Person>();
		for(int i=0;i<persons.size();i++){
			Person person = persons.get(i);
			if(i==0){//东
				person.setDirection("东");
				map.put("庄", person);
				map.put("东",person);
			}else if(i==1){//北
				person.setDirection("北");
				map.put("北",person);
			}else if(i==2){//西
				person.setDirection("西");
				map.put("西",person);
			}else if(i==3){//南
				person.setDirection("南");
				map.put("南",person);
			}
		}
		return map;
	}

	/**准备游戏
	 * @param person1
	 * @param person2
	 * @param person3
	 * @param person4
	 */
	private static void prepareGame(Person person1, Person person2,
			Person person3, Person person4) {
		//开始打牌
		System.out.println("-------------开始打牌准备----------------");
		Scanner scan = new Scanner(System.in);
		boolean p1Ready = person1.isReady();
		boolean p2Ready = person2.isReady();
		boolean p3Ready = person3.isReady();
		boolean p4Ready = person4.isReady();
//		while(p1Ready==false||p2Ready==false||p3Ready==false||p4Ready==false){//有一个没准备好
//			String read = scan.nextLine();
//			if(read.equals("p1")){//选手1 准备
//				p1Ready = true;
//				System.out.println("第一个人已准备好...");
//			}else if(read.equals("p2")){
//				p2Ready = true;
//				System.out.println("第二个人已准备好...");
//			}else if(read.equals("p3")){
//				p3Ready = true;
//				System.out.println("第三个人已准备好...");
//			}else if(read.equals("p4")){
//				p4Ready = true;
//				System.out.println("第四个人已准备好...");
//			}else{
//				System.out.println("无效的输入，请重新输入...");
//			}
//		}
//		System.out.println("---------已全部就绪，开始游戏--------------");
	}

	/**
	 * 发牌
	 * @param paiList
	 * @param person1
	 * @param person2
	 * @param person3
	 * @param person4
	 * @return 剩余的牌
	 */
	private static List<Integer> DealsCards(List<Integer> paiList, Person person1,
			Person person2, Person person3, Person person4) {
		Integer[] paiArray = paiList.toArray(new Integer[112]);
		List<Integer> remainList = new LinkedList<Integer>(); 
		// 发第一个人牌
		for (int i = 0; i < 14; i++) {
			person1.getPaiList().add(paiArray[i]);
		}
		
		Collections.sort(person1.getPaiList());
		System.out.println("第一个人的牌(庄家)：\n"+person1.getPaiList());
		showPai(person1.getPaiList());
		// 发第二个人牌
		for (int i = 14; i < 27; i++) {
			person2.getPaiList().add(paiArray[i]);
		}
		Collections.sort(person2.getPaiList());
		System.out.println("第二个人的牌 钱:" + person2.getPaiList());
		showPai(person2.getPaiList());
		// 发第三个人牌
		for (int i = 27; i < 40; i++) {
			person3.getPaiList().add(paiArray[i]);
		}
		Collections.sort(person3.getPaiList());
		System.out.println("第三个人的牌 孙:" + person3.getPaiList());
		showPai(person3.getPaiList());

		// 发第四个人牌
		for (int i = 40; i < 53; i++) {
			person4.getPaiList().add(paiArray[i]);
		}
		Collections.sort(person4.getPaiList());
		System.out.println("第四个人的牌 李:" + person4.getPaiList());
		showPai(person4.getPaiList());
		System.out.println("----------------------------------------------------------");
		//桌面上的牌
		for (int i=53;i<paiList.size();i++){
			remainList.add(paiList.get(i));
		}
		System.out.println("未发出的牌:"+remainList);
		showPai(remainList);
		return remainList;
	}

	public static void showPai(List<Integer> list){
		//System.out.println();
		for(int i=0;i<list.size();i++){
			int  key = list.get(i);
			System.out.print(InitialPuKe.map.get(key));
		}
		System.out.println();
		for(int i=0;i<list.size();i++){
			int  key = list.get(i);
			System.out.print(InitialPuKe.map.get(key)+" ["+key+"]  ");
		}
		System.out.println();
	}
	
	
	/**
	 * 随机生成112张牌
	 */
	private static List<Integer> creatMajiang() {
		List<Integer> paiList = new ArrayList<Integer>();
		Set<Integer> set = new HashSet<Integer>();
		//int total = 0;
		for (int i = 0; set.size() < 112;) {
			Random random = new Random();
			int nextInt = random.nextInt(112);
			//total ++;
			if (set.add(nextInt)) {
				paiList.add(nextInt);
			}
		}
		//System.out.println("total:"+total);
		System.out.println("----------生成的牌-------------");
		showPai(paiList);
		System.out.println(paiList);
		//Collections.sort(paiList);
		//showPai(paiList);
		System.out.println("----------生成的牌-------------");
		return paiList;
	}
	
	/**
	 * 随机生成112张牌
	 */
	private static List<Integer> creatMajiang2() {
		List<Integer> list = new LinkedList<Integer>();
		for (int i = 0; i < 112;i++) {
			list.add(i);
		}
		Collections.shuffle(list);
		return list;
	}
	
}

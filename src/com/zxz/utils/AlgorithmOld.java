package com.zxz.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.zxz.algorithm.InitialPuKe;


public class AlgorithmOld {
	private static Logger logger = Logger.getLogger(Algorithm.class);
	public static void main(String[] args) throws Exception {
		int tingpaiArray [] = {12,13,36,40,45,60,69,72,76,80,81,108,109};
//		int paiArray[] = {5, 6, 11, 15, 16, 62, 71, 72, 73, 75, 108};
		long currentTimeMillis1 = System.currentTimeMillis();
		System.out.println(System.currentTimeMillis());
		int paiArray[] = {24, 26, 39, 40, 47, 73, 75, 78, 82, 86, 108};
		showPai(paiArray);
		boolean win = isWin2(paiArray);
		if(win){
			System.out.println("hu");
		}
		long currentTimeMillis2 = System.currentTimeMillis();
		System.out.println(currentTimeMillis2-currentTimeMillis1);
	}
	
	public static boolean isWin2(int array[]) {
		if(array.length==2){
			boolean winWithDouble = isWinWithDouble(array);
			if(winWithDouble){
				return true;
			}else{
				return false;
			}
		}
		return otherCheck(array);
	}

	/**
	 * @param array
	 * @return
	 */
	private static boolean otherCheck(int[] array) {
		
		return checkWithDuiZiList(array);
	}


	/**
	 * @param array
	 * @param duiziList
	 * @return
	 */
	private static boolean checkWithDuiZiList(int[] array) {
			
		boolean isWin = check(array);
				
		return isWin;
	}
	
	public static boolean check(int[] array) {
		int[] arr = getArrayWithoutDuiZi(array);
		List<Integer> hongZhongList = getCanUseHongZhongList(arr);//得到可用的红中
		List<Integer> cards = getListWithoutHongZhong(arr);//去除掉所有的红中
		cards = getCardsWithoutSen(cards);// 345 5 567 233445-2345
		cards = getCardsWithoutCan(cards);
		boolean removeOneDuiZi = removeOneDuiZi(cards,hongZhongList);
		if(!removeOneDuiZi){
			return check3(array);
		}
		showPai(cards);
		if(cards.size()==0){
			return true;
		}else{
			int index = 0;
			int length = cards.size();
			boolean checkOver = false;
			while(!checkOver){
				if(index>=length){
					checkOver = true;
					break;
				}
				int n1 =  cards.get(index);
				int n2 = -1;
				if(index+1<length){
					n2 = cards.get(index+1);
				}
				//用红中来代替
				if(hongZhongList.size()<=0){//没有红中 
					return check3(array);//2万 2万 3万 4万 5万 7筒 9筒 1条 1条 1条 红中 
				}else{
					//看看消耗几个红中  1万 1万 或者是 1万 2万 或者是 1万 3万 消耗1 个
					String type1 = CardsMap.getCardType(n1);
					String type2 = CardsMap.getCardType(n2);
					int interval = getInterval(n1, n2);
					if(type1.equals(type2)){//消耗一个
						index = index +2;
						hongZhongList.remove(0);
						System.out.println("消耗一个来做坎");
					}else if(interval==1||interval==2){//如果间隔相差一个 消耗1个
						index = index +2;
						hongZhongList.remove(0);
						System.out.println("消耗一个来做句子");
					}else{//这里消耗两个
						
						if(index+1>=length&&hongZhongList.size()>=1){
							return true;
						}
						
						if(hongZhongList.size()<2){
							System.out.println("终止的地方:"+CardsMap.getCardType(n1));
							return false;
						}else{
							hongZhongList.remove(0);
							hongZhongList.remove(0);
							index = index +1;
							System.out.println("消耗两个:"+CardsMap.getCardType(n1));
						}
					}
				}
					
			}
		}
		return true;
	}
	
	/**		
	 *2万 2万 3万 4万 5万 5条 6条 7条 
	 * @param array
	 * @return
	 */
	public static boolean check4(int[] array){
		int[] arr = getArrayWithoutDuiZi(array);
		List<Integer> hongZhongList = getCanUseHongZhongList(arr);//得到可用的红中
		List<Integer> cards = getListWithoutHongZhong(arr);//去除掉所有的红中
		boolean removeOneDuiZi = removeOneDuiZi(cards,hongZhongList);
		if(!removeOneDuiZi){
			return false;
		}
		cards = getCardsWithoutSen(cards);// 345 5 567 233445-2345
		cards = getCardsWithoutCan(cards);
		showPai(cards);
		if(cards.size()==0){
			return true;
		}else{
			int index = 0;
			int length = cards.size();
			boolean checkOver = false;
			while(!checkOver){
				if(index>=length){
					checkOver = true;
					break;
				}
				int n1 =  cards.get(index);
				int n2 = -1;
				if(index+1<length){
					n2 = cards.get(index+1);
				}
			
				//用红中来代替
				if(hongZhongList.size()<=0){//没有红中 
					return false;
				}else{
					//看看消耗几个红中  1万 1万 或者是 1万 2万 或者是 1万 3万 消耗1 个
					String type1 = CardsMap.getCardType(n1);
					String type2 = CardsMap.getCardType(n2);
					int interval = getInterval(n1, n2);
					if(type1.equals(type2)){//消耗一个
						index = index +2;
						hongZhongList.remove(0);
						System.out.println("消耗一个来做坎");
					}else if(interval==1||interval==2){//如果间隔相差一个 消耗1个
						index = index +2;
						hongZhongList.remove(0);
						System.out.println("消耗一个来做句子");
					}else{//这里消耗两个
						
						if(index+1>=length&&hongZhongList.size()>=1){
							return true;
						}
						
						if(hongZhongList.size()<2){
							System.out.println("终止的地方:"+CardsMap.getCardType(n1));
							return false;
						}else{
							hongZhongList.remove(0);
							hongZhongList.remove(0);
							index = index +1;
							System.out.println("消耗两个:"+CardsMap.getCardType(n1));
						}
					}
				}
					
			}
		}
		return true;
	}
	
	
	/**		
	 *2万 2万 3万 4万 5万 5条 6条 7条 
	 * @param array
	 * @return
	 */
	public static boolean check3(int[] array){
		int[] arr = getArrayWithoutDuiZi(array);
		List<Integer> hongZhongList = getCanUseHongZhongList(arr);//得到可用的红中
		List<Integer> cards = getListWithoutHongZhong(arr);//去除掉所有的红中
		boolean removeOneDuiZi = removeOneDuiZi(cards,hongZhongList);
		if(!removeOneDuiZi){
			return false;
		}
		cards = getCardsWithoutSen(cards);// 345 5 567 233445-2345
		cards = getCardsWithoutCan(cards);
		showPai(cards);
		if(cards.size()==0){
			return true;
		}else{
			int index = 0;
			int length = cards.size();
			boolean checkOver = false;
			while(!checkOver){
				if(index>=length){
					checkOver = true;
					break;
				}
				int n1 =  cards.get(index);
				int n2 = -1;
				if(index+1<length){
					n2 = cards.get(index+1);
				}
			
				//用红中来代替
				if(hongZhongList.size()<=0){//没有红中 
					return false;
				}else{
					//看看消耗几个红中  1万 1万 或者是 1万 2万 或者是 1万 3万 消耗1 个
					String type1 = CardsMap.getCardType(n1);
					String type2 = CardsMap.getCardType(n2);
					int interval = getInterval(n1, n2);
					if(type1.equals(type2)){//消耗一个
						index = index +2;
						hongZhongList.remove(0);
						System.out.println("消耗一个来做坎");
					}else if(interval==1||interval==2){//如果间隔相差一个 消耗1个
						index = index +2;
						hongZhongList.remove(0);
						System.out.println("消耗一个来做句子");
					}else{//这里消耗两个
						
						if(index+1>=length&&hongZhongList.size()>=1){
							return true;
						}
						
						if(hongZhongList.size()<2){
							System.out.println("终止的地方:"+CardsMap.getCardType(n1));
							return false;
						}else{
							hongZhongList.remove(0);
							hongZhongList.remove(0);
							index = index +1;
							System.out.println("消耗两个:"+CardsMap.getCardType(n1));
						}
					}
				}
					
			}
		}
		return true;
	}
	
	
	//4万 4万 [1筒 2筒 3筒] [7筒  红中  9筒]  [1条 2条 3条] 1条 红中  3条 
		public static boolean check2(int[] array) {
			int[] arr = getArrayWithoutDuiZi(array);
			List<Integer> hongZhongList = getCanUseHongZhongList(array);//得到可用的红中
			List<Integer> cards = getListWithoutHongZhong(array);//去除掉所有的红中
			cards = getCardsWithoutSen(cards);// 345 5 567 233445-2345
			//showPai(cards);
			cards = getCardsWithoutCan(cards);
			//showPai(cards);
			removeOneDuiZi(cards,hongZhongList);
			showPai(cards);
			
			if(cards.size()==0){
				return true;
			}else{
				int index = 0;
				int length = cards.size();
				boolean checkOver = false;
				while(!checkOver){
					if(index>=length){
						checkOver = true;
						break;
					}
					int n1 =  cards.get(index);
					int n2 = -1;
					if(index+1<length){
						n2 = cards.get(index+1);
					}
				
					//用红中来代替
					if(hongZhongList.size()<=0){//没有红中 
						return false;
					}else{
						//看看消耗几个红中  1万 1万 或者是 1万 2万 或者是 1万 3万 消耗1 个
						String type1 = CardsMap.getCardType(n1);
						String type2 = CardsMap.getCardType(n2);
						int interval = getInterval(n1, n2);
						if(type1.equals(type2)){//消耗一个
							index = index +2;
							hongZhongList.remove(0);
							System.out.println("消耗一个来做坎");
						}else if(interval==1||interval==2){//如果间隔相差一个 消耗1个
							index = index +2;
							hongZhongList.remove(0);
							System.out.println("消耗一个来做句子");
						}else{//这里消耗两个
							
							if(index+1>=length&&hongZhongList.size()>=1){
								return true;
							}
							
							if(hongZhongList.size()<2){
								System.out.println("终止的地方:"+CardsMap.getCardType(n1));
								return false;
							}else{
								hongZhongList.remove(0);
								hongZhongList.remove(0);
								index = index +1;
								System.out.println("消耗两个:"+CardsMap.getCardType(n1));
							}
						}
					}
						
				}
			}
			
			boolean result = true;
			
			return result;
		}
	
	/**删除一个对子
	 * @param cards
	 * @param hongZhongList 
	 */
	private static boolean removeOneDuiZi(List<Integer> cards, List<Integer> hongZhongList) {
		List<Integer> list = new ArrayList<>();
		for(int i=0;i<cards.size();i++){
			String beforType = "";
			Integer cardId = cards.get(i);
			Integer beforeCardId = null;
			if(i>=1){
				beforeCardId = cards.get(i-1);
				beforType = CardsMap.getCardType(beforeCardId);
			}
			String currentType = CardsMap.getCardType(cardId);
			if(currentType.equals(beforType)){
				list.add(cardId);
				list.add(beforeCardId);
				break;
			}
		}
		
		if(list.size()>0){
			for(Integer card:list){
				cards.remove(card);
			}
			return true;
		}else{//说明没有对子,需要从红中里面借一张牌 
			cards.remove(0);
			if(hongZhongList.size()<1){
				return false;
			}
			hongZhongList.remove(0);
			return true;
		}
	}


	public static List<Integer> getCardsWithoutCan(List<Integer> list) {
		 List<Integer> canList = getCanList(list);
		 if(canList.size()>0){
			 list = removeListFromList(list, canList);
		 }
		 return list;
	}


	/**
	 * @param list
	 * @return
	 */
	private static List<Integer> getCanList(List<Integer> list) {
		List<Integer> result = new LinkedList<>();
		for(int i=0;i<list.size();){
			if(i>=list.size()){
				break;
			}
			int n1 = list.get(i);
			int n2 = -1;
			int n3 = -2;
			if(i+1<list.size()){
				n2 = list.get(i+1);
			}
			if(i+2<list.size()){
				n3 = list.get(i+2);
			}
			boolean oneSentence = isOneCan(n1, n2, n3);
			if(oneSentence){
				i = i+3;
				result.add(n1);
				result.add(n2);
				result.add(n3);
			}else{
				i = i+1;
			}
		}
		return result;
	}


	/**去除所有的句子
	 * @param cards
	 * @return 
	 * @return 
	 */
	public static List<Integer> getCardsWithoutSen(List<Integer> cards) {//233445  112233  2344456
		//showPai(cards);
		List<Integer> notDistinctList = getNotDistinctWithoutSen(cards);//2345
		//System.out.println("不重复的排");
		//showPai(notDistinctList);
		List<Integer> sentensList = getSentensList(notDistinctList);//234
		//showPai(sentensList);
		while(sentensList.size()>0){
			//System.out.println("需要剔除的排");
			//showPai(sentensList);
			cards = removeListFromList(cards,sentensList);//345
			//System.out.println("剔除后");
			//showPai(cards);
			notDistinctList = getNotDistinctWithoutSen(cards);//2345
			//System.out.println("不重复的排");
			//showPai(notDistinctList);
			sentensList = getSentensList(notDistinctList);
		}
		return cards;
	}

	/**从牌中移除句子
	 * @param cards
	 * @param sentensList
	 */
	private static List<Integer> removeListFromList(List<Integer> cards,
			List<Integer> sentensList) {
		List<Integer> list = new LinkedList<>();
		for(int i=0;i<cards.size();i++){
			Integer card = cards.get(i);
			boolean canAdd = true;
			for(int j=0;j<sentensList.size();j++){
				Integer remove = sentensList.get(j);
				if(card == remove){
					canAdd = false;
					break;
				}
			}
			if(canAdd){
				list.add(card);
			}
		}
		return list;
	}


	private static List<Integer> getSentensList(List<Integer> list) {
		List<Integer> result = new LinkedList<>();
		for(int i=0;i<list.size();){
			if(i>=list.size()){
				break;
			}
			int n1 = list.get(i);
			int n2 = -1;
			int n3 = -2;
			if(i+1<list.size()){
				n2 = list.get(i+1);
			}
			if(i+2<list.size()){
				n3 = list.get(i+2);
			}
			boolean oneSentence = isOneSentence(n1, n2, n3);
			if(oneSentence){
				i = i+3;
				result.add(n1);
				result.add(n2);
				result.add(n3);
			}else{
				i = i+1;
			}
		}
		return result;
	}
	
	
	
	/**去除所有的句子
	 * @param cards
	 */
	private static List<Integer> getNotDistinctWithoutSen(List<Integer> cards) {
		String type = "";
		List<Integer> list = new LinkedList<>();
		for(int i=0;i<cards.size();i++){
			Integer card = cards.get(i);
			String cardType = CardsMap.getCardType(card);
			if(!type.equals(cardType)){
				list.add(card);
				type = cardType;
			}
		}
		return list;
	}

	//4万 4万 [1筒 2筒 3筒] [7筒  红中  9筒]  [1条 2条 3条] 1条 红中  3条 
	public static boolean check2(int[] array,int num1,int num2) {
		int[] arr = getArrayWithoutDuiZi(array);//得到除对子以外的牌
		List<Integer> hongZhongList = getCanUseHongZhongList(arr);//得到可用的红中
		List<Integer> cards = getListWithoutHongZhong(arr);//去除掉所有的红中
		boolean result = true;
		List<Integer> removeList = getAllCanOrSenList(cards);//得到所有的坎或者是句子的集合
		cards = removeList(cards, removeList);//移除后的牌
		showPai(cards);
		//7筒 9筒 1条 2条 2条 3条 3条 
		cards = getSecondRemoveList(cards);
		showPai(cards);
		boolean checkOver = false;
		int index = 0;
		int length = cards.size();
		while(!checkOver){
			if(index>=length){
				checkOver = true;
				break;
			}
			int n1 =  cards.get(index);
			int n2 = -1;
			if(index+1<length){
				n2 = cards.get(index+1);
			}
			int n3 = -2;
			if(index+2<length){
				n3 = cards.get(index+2);
			}
			boolean oneCan = isOneCan(n1, n2, n3);
			boolean oneSentence = isOneSentence(n1, n2, n3);
			if(oneCan||oneSentence){//最好的情况
				index = index +3;
				if(oneCan){
					System.out.println("坎是:"+CardsMap.getCardType(n1));
				}else if(oneSentence){
					System.out.println("句子:"+CardsMap.getCardType(n1)+CardsMap.getCardType(n2)+CardsMap.getCardType(n3));
				}
			}else{
				//用红中来代替
				if(hongZhongList.size()<=0){//没有红中 
					return false;
				}else{
					//看看消耗几个红中  1万 1万 或者是 1万 2万 或者是 1万 3万 消耗1 个
					String type1 = CardsMap.getCardType(n1);
					String type2 = CardsMap.getCardType(n2);
					int interval = getInterval(n1, n2);
					if(type1.equals(type2)){//消耗一个
						index = index +2;
						hongZhongList.remove(0);
						System.out.println("消耗一个来做坎");
					}else if(interval==1||interval==2){//如果间隔相差一个 消耗1个
						index = index +2;
						hongZhongList.remove(0);
						System.out.println("消耗一个来做句子");
					}else{//这里消耗两个
						if(hongZhongList.size()<2){
							System.out.println("终止的地方:"+CardsMap.getCardType(n1));
							return false;
						}else{
							hongZhongList.remove(0);
							hongZhongList.remove(0);
							index = index +1;
							System.out.println("消耗两个:"+CardsMap.getCardType(n1));
						}
					}
				}
			}
		}
		return result;
	}
	
	
	public static List<Integer> getSecondRemoveList(List<Integer> cards){
		List<Integer> result = new LinkedList<Integer>();
		System.out.println("cards");
		showPai(cards);
		System.out.println("cards");
		String type = "";
		for(int i=0;i<cards.size();i++){
			Integer card = cards.get(i);
			String cardType = CardsMap.getCardType(card);
			if(!cardType.equals(type)){
				result.add(card);
				type = cardType;
			}
		}
		showPai(result);
		showPai(result);
		List<Integer> firstRemoveList = getAllCanOrSenList(result);
		if(firstRemoveList.size()>0){
			List<Integer> removeList = removeList(cards, firstRemoveList);
			return removeList;
		}else{
			return cards;
		}
	}
	
	public static List<Integer> removeList(List<Integer> cards,List<Integer> removeList){
		List<Integer> result = new LinkedList<Integer>();
		//int lastRemove = -1;
		for(int i=0;i<cards.size();i++){
			Integer oneCard= cards.get(i);
            boolean canAdd = true;
			for(int j=0;j<removeList.size();j++){
				if(removeList.get(j)==oneCard){
					canAdd = false;
					break;
//					if(lastRemove!=oneCard){
//						lastRemove = oneCard;
//					}
				}
			}
			if(canAdd){
				result.add(oneCard);
			}
		}
		return result;
	}
	
	
	
	public static List<Integer> getAllCanOrSenList(List<Integer> list){
		int len = list.size();
		List<Integer> listResult = new LinkedList<Integer>();
		for(int i=0;i<list.size();){
			int n1 =  list.get(i);
			int n2 = -1;
			int n3 = -2;
			if(i+1<len){
				n2 = list.get(i+1);
			}
			if(i+2<len){
				n3 = list.get(i+2);
			}
			boolean oneCan = isOneCan(n1, n2, n3);
			boolean oneSentence = isOneSentence(n1, n2, n3);
			if(oneCan||oneSentence){
				listResult.add(n1);
				listResult.add(n2);
				listResult.add(n3);
				System.out.println(CardsMap.getCardType(n1)+"----"+CardsMap.getCardType(n2)+"------"+CardsMap.getCardType(n3));
				i = i+3;
			}else{
				i++;
			}
		}
		return listResult;
	}
	
	
	
	
	/**得到红中list
	 * @param array
	 * @return
	 */
	public static List<Integer> getCanUseHongZhongList(int []array){
		List<Integer> hongZhongList =  new LinkedList<Integer>();
		for(int i=0;i<array.length;i++){
			if(array[i]>=108&&array[i]<=111){
				hongZhongList.add(array[i]);
			}
		}
		return hongZhongList;
	}
	
	
	/**
	 * 判断用户是否胡牌
	 * @throws Exception 
	 */
	public static boolean isWin(List<Integer> list){
		int array[] = new int[list.size()];
		for(int i=0;i<list.size();i++){
			array[i] = list.get(i);
		}
		return isWin2(array);
	}
	
	


	/**判断是否胡牌
	 * @param array 用数组判断该用户是否胡牌
	 * @return
	 * @throws Exception 
	 */
	public static boolean isWin(int array[]) {
		showPai(array);
		if(array.length==2){
			boolean winWithDouble = isWinWithDouble(array);
			if(winWithDouble){
				logger.info("用对子胡牌");
				return true;
			}else{
				logger.info("用对子都没胡牌");
				return false;
			}
		}
		List<Integer> duiziList = getDuiZiList(array);
		
		if(duiziList.size()==0){
			logger.info("连对子都没有");
			return false;
		}
		
		int size = duiziList.size();
		if(duiziList.size()>0){
			for(int i=0;i<size/2;i++){
				int dui1 = duiziList.get(i*2);
				int dui2 = duiziList.get(i*2+1);
				boolean isWin = checkWinWithDuizi3(array, dui1,dui2);
				System.out.println("对子是:"+InitialPuKe.map.get(dui1)+" "+InitialPuKe.map.get(dui2));
				if(isWin){
					System.out.println("++胡牌:++");
					return isWin;
				}
				System.out.println("---------------------------------------------");
			}
		}
		return false;
	}
	
	/**两个子胡牌
	 * @param array
	 * @return
	 */
	public static boolean isWinWithDouble(int array[]){
		if(array.length==2){
			if(CardsMap.getCardType(array[0]).equals(CardsMap.getCardType(array[1]))){
				return true;
			}else if(array[0]>=108&&array[0]<=111){
				return true;
			}else if(array[1]>=108&&array[1]<=111){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	/**是否含有一个对子
	 * @param array
	 * @return 对子的类型 如:1万
	 */
	public static List<Integer> getDuiZiList(int array[]){
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < array.length-1; i++) {
			String previousType = "";
			if(i>0){
				previousType = InitialPuKe.map.get(array[i-1]);//得到前一张的牌型 
			}
			String type = InitialPuKe.map.get(array[i]);//得到该张牌型
			String nextPukeType = InitialPuKe.map.get(array[i+1]);//下一张牌的类型
			String nextTwoType = "";
			if(i==array.length-2){
				nextTwoType = "";//下二张牌的类型
			}else{
				nextTwoType = InitialPuKe.map.get(array[i+2]);//下二张牌的类型
			}
			if(type.equals(nextPukeType)){//&&!type.equals(nextTwoType)&&!type.equals(previousType)){//该张牌和下一张相等不和下二张相等并且和前一张不相等
				result.add(array[i]);
				result.add(array[i+1]);
				//System.out.println("对子是:"+type);
//				resultArray[0] = array[i];
//				resultArray[1] = array[i+1];
				//break;
			//1万 1万 1万 2万 3万 7万 7万 7万 8筒 8筒 8筒 8条 8条 8条 
			}else if(type.equals(nextPukeType)&&type.equals(nextTwoType)){//该张牌和第一张牌相等和第三张牌也相等
				boolean oneSentence = false;
				if(i+4<array.length-1){
					 oneSentence = isOneSentence(array[i+2],array[i+3],array[i+4]);
				}
				if(oneSentence){//看看第三张牌和后面的牌是否是一个句子
					result.add(array[i]);
					result.add(array[i+1]);
				}
			}else if(nextPukeType.equals("红中")||type.equals("红中")){
				result.add(array[i]);
				result.add(array[i+1]);
			}
		}
		return result;
	}
	
	
	public static String showPai(int array[]){
		System.out.println("\t\t");
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<array.length;i++){
			System.out.print(InitialPuKe.map.get(array[i])+" ");
			sb.append(InitialPuKe.map.get(array[i])+" ");
		}
		System.out.println();
		return sb.toString();
	}
	
	
	public static String showPai(List<Integer> cards){
		int []array = new int[cards.size()];
		for(int i=0;i<cards.size();i++){
			array[i] = cards.get(i);
		}
		String result = showPai(array);
		return result;
	}
	
	
	public static boolean checkWinWithDuizi3(int[] array,int num1,int num2) {
		int[] arr = getArrayWithoutDuiZi(array);//得到除对子以外的牌
		List<Integer> hongZhongList = getHongZhongList(array,num1,num2);//得到可用的红中
		List<Integer> listWithoutHongZhong = getListWithoutHongZhong(arr);
		boolean checkOver = false;
		boolean result = true;
		int index = 0;
		int length = listWithoutHongZhong.size();
		while(!checkOver){
			if(index>=listWithoutHongZhong.size()){
				checkOver = true;
				break;
			}
			//9, 29, 35, 36, 45, 48, 67,[ 69, 70], 89, 98, 102, 104,        <108>
			//3万 8万   9万     1筒    3筒    4筒    8筒      9筒 9筒        5条     7条     8条     9条                         红中 
			//待分析的牌
			//9, 29, 35, 36, 45, 48, 67, 89, 98, 102, 104
			//3万 8万   9万     1筒    3筒    4筒    8筒   5条    7条    8条       9条   
			int n1 =  listWithoutHongZhong.get(index);
			int n2 = -1;
			int n3 = -2;
			if(index+1<length){
				n2 = listWithoutHongZhong.get(index+1);
			}
			if(index+2<length){
				n3 = listWithoutHongZhong.get(index+2);
			}
			boolean oneCan = isOneCan(n1, n2, n3);
			boolean oneSentence = isOneSentence(n1, n2, n3);
			if(oneCan||oneSentence){//最好的情况
				index = index +3;
				
				if(oneCan){
					System.out.println("坎是:"+CardsMap.getCardType(n1));
				}else if(oneSentence){
					System.out.println("句子:"+CardsMap.getCardType(n1)+CardsMap.getCardType(n2)+CardsMap.getCardType(n3));
				}
				
			}else{//用红中来代替
				if(hongZhongList.size()<=0){//没有红中 
					return false;
				}else{
					//看看消耗几个红中  1万 1万 或者是 1万 2万 或者是 1万 3万 消耗1 个
					String type1 = CardsMap.getCardType(n1);
					String type2 = CardsMap.getCardType(n2);
					int interval = getInterval(n1, n2);
					if(type1.equals(type2)){//消耗一个
						index = index +2;
						hongZhongList.remove(0);
						System.out.println("消耗一个来做坎");
					}else if(interval==1||interval==2){//如果间隔相差一个 消耗1个
						index = index +2;
						hongZhongList.remove(0);
						System.out.println("消耗一个来做句子");
					}else{//这里消耗两个
						if(hongZhongList.size()<2){
							System.out.println("终止的地方:"+CardsMap.getCardType(n1));
							return false;
						}else{
							hongZhongList.remove(0);
							hongZhongList.remove(0);
							index = index +1;
							System.out.println("消耗两个:"+CardsMap.getCardType(n1));
						}
					}
				}
			}
			
		}
		
		//查看剩余的红中数
		if(hongZhongList.size()%3!=0){
			System.out.println("红中最后剩:"+hongZhongList.size()+"没有胡牌");
			return false;
		}
		
		return result;
	}
	
	
	
	
	
	public static boolean checkWinWithDuizi2(int[] array,int num1,int num2) {
		int[] arr = getArrayWithoutDuiZi(array);
		List<Integer> hongZhongList = getHongZhongList(array,num1,num2);
		List<Integer> listWithoutHongZhong = getListWithoutHongZhong(arr);
		boolean checkOver = false;
		int index = 0;
		while(!checkOver){
			if(index>=listWithoutHongZhong.size()){
				checkOver = true;
				break;
			}
			Integer card1= listWithoutHongZhong.get(index);
			int card2 = -1;
			int card3 = -2;
			if(index+1<listWithoutHongZhong.size()){
				card2 = listWithoutHongZhong.get(index+1);
			}
			if(index+2<listWithoutHongZhong.size()){
				card3 = listWithoutHongZhong.get(index+2);
			}
			boolean oneSentence = isOneSentence(card1, card2, card3);
			boolean oneCan = isOneCan(card1, card2, card3);
			if(oneSentence||oneCan){
				index += 3;
			}else{
				int size = hongZhongList.size();
				if(size>0){
					boolean oneSentenceWithHongZhongList = isOneSentenceWithHongZhongList(card1, card2, hongZhongList);
					boolean oneCanWithHongZhongList = isOneCanWithHongZhongList(card1, card2, hongZhongList);
					if(oneSentenceWithHongZhongList||oneCanWithHongZhongList){
						hongZhongList.remove(0);
						index += 2;
					}else if(hongZhongList.size()>=2){
						hongZhongList.remove(0);
						hongZhongList.remove(0);
						index ++;
					}else{
						return false;
					}
				}
			}
		}
		return true;
	}
	
	
	
	
	
	
	/**得到没有红中的的集合
	 * @param arr
	 */
	public static List<Integer> getListWithoutHongZhong(int[] arr) {
		List<Integer> list = new LinkedList<Integer>();
		for (int i = 0; i < arr.length; i++) {
			if(arr[i]<108){
				list.add(arr[i]);
			}
		}
		return list;
	}

	/**得到红中list
	 * @param array
	 * @return
	 */
	public static List<Integer> getHongZhongList(int []array,int num1,int num2){
		List<Integer> hongZhongList =  new LinkedList<Integer>();
		for(int i=0;i<array.length;i++){
			if(array[i]>=108&&array[i]<=111){
				if(array[i]!=num1&&array[i]!=num2){
					hongZhongList.add(array[i]);
				}
			}
		}
		return hongZhongList;
	}
	
	/**看看是不是一个坎
	 * @param num1
	 * @param num2
	 * @param num3
	 * @return
	 */
	public static boolean isOneCanWithHongZhongList(int num1,int num2,List<Integer> hongzhongList){
		String type1 = CardsMap.getCardType(num1);
		String type2 = CardsMap.getCardType(num2);
		if(type1.equals(type2)){
			if(hongzhongList.size()>0){
				//hongzhongList.remove(0);
				return true;
			}
		}
		return false;
	}
	
	
	/**看看是不是一个坎
	 * @param num1
	 * @param num2
	 * @param num3
	 * @return
	 */
	public static boolean isOneCan(int num1,int num2,int num3){
		String type1 = CardsMap.getCardType(num1);
		String type2 = CardsMap.getCardType(num2);
		String type3 = CardsMap.getCardType(num3);
		
		if(type1.equals(type2)&&type1.equals(type3)){
			//System.out.println("坎:"+CardsMap.getCardType(num1));
			return true;
		}
		
		return false;
	}
	
	
	public static int[] getArrayWithoutDuiZi(int[] array){
		
		if(array.length<=2){
			throw new RuntimeException("数组长度不能小于2");
		}
		
		int arr[] = new int[array.length];
		
		for(int i=0;i<array.length;i++){
				arr[i] = array[i];
		}
		
		return arr;
	}
	
	
	
	
	/**
	 * @param array
	 * @param isWin
	 * @param duiziList
	 * @return
	 */
	private static boolean checkWinWithDuizi(int[] array,
			int dui1,int dui2) {
		boolean isWin = true;
		boolean isCheckOver = false;
		int begin = 0;
		while(!isCheckOver){
			if(array[begin]!=dui1&&array[begin]!=dui2){
				String currentPaiHao = InitialPuKe.map.get(array[begin]);
				String nextPaiHao = InitialPuKe.map.get(array[begin+1]);
				String nextTwoHao = InitialPuKe.map.get(array[begin+2]);
				if(currentPaiHao.equals(nextPaiHao)&&currentPaiHao.equals(nextTwoHao)){//看看是否是一个坎
					System.out.println("坎:"+currentPaiHao);
					if(begin+2<array.length-1){
						begin=begin+2;
					}
				}else{//是否是句子
					int a1 = array[begin];
					int a2 = array[begin+1];
					int a3 = array[begin+2];
					boolean isOneSentence = isOneSentence(a1,a2,a3);
					if(isOneSentence){
						System.out.println("句子:"+InitialPuKe.map.get(a1)+" "+InitialPuKe.map.get(a2)+" "+InitialPuKe.map.get(a3));
						begin=begin+2;
					}else{
						System.out.println("不是句子:"+InitialPuKe.map.get(a1)+" "+InitialPuKe.map.get(a2)+" "+InitialPuKe.map.get(a3));
						isWin = false;//既不是坎也不是句子，不胡牌
						isCheckOver =  true;
					}
				}
				begin+=1;
			}else{
				begin+=2;
			}
			if(begin==array.length-2){
				isCheckOver = true;
			}
		}
		return isWin;
	}
	
	/**看看是不是一个句子
	 * @param a1
	 * @param a2
	 * @param a3
	 * @return
	 */
	public static boolean isOneSentence(int a1,int a2,int a3){
		String typeA1 = getTypeString(a1);
		String typeA2 = getTypeString(a2);
		String typeA3 = getTypeString(a3);
		if(!typeA1.equals(typeA2)||!typeA1.equals(typeA3)||!typeA2.equals(typeA3)){
			return false;
		}
		int typeA1Int = getTypeInt(a1);
		int typeA2Int = getTypeInt(a2);
		int typeA3Int = getTypeInt(a3);
		if(typeA2Int-typeA1Int!=1){
			return false;
		}
		if(typeA3Int-typeA2Int!=1){
			return false;
		}
		return true;
	}
	
	/**
	 * @param a1
	 * @param a2
	 * @return 如果牌型不相等则返回-1,否则返回它们之间的间隔
	 */
	public static int getInterval(int a1,int a2){
		String typeA1 = getTypeString(a1);
		String typeA2 = getTypeString(a2);
		
		if(!typeA1.equals(typeA2)){
			return -1;
		}
		
		int typeA1Int = getTypeInt(a1);
		int typeA2Int = getTypeInt(a2);
		
		return typeA2Int-typeA1Int;
		
	}
	
	/**看看是不是一个句子
	 * @param a1
	 * @param a2
	 * @param a3
	 * @return
	 */
	public static boolean isOneSentenceWithHongZhongList(int a1,int a2,List<Integer> hongzhongList){
		String typeA1 = getTypeString(a1);
		String typeA2 = getTypeString(a2);
		if(!typeA1.equals(typeA2)){
			return false;
		}
		int typeA1Int = getTypeInt(a1);
		int typeA2Int = getTypeInt(a2);
		if(typeA2Int-typeA1Int==1){
			if(hongzhongList.size()>0){
				//hongzhongList.remove(0);
				return true;
			}
		}else if(typeA2Int-typeA1Int==2){
			if(hongzhongList.size()>0){
				//hongzhongList.remove(0);
				return true;
			}
		}
		return false;
	}
	
	
	
	
	
	/**根据牌号计算出牌的类型
	 * @param paiHao
	 * @return
	 */
	public static int getTypeInt(int paiHao) {
		int type = 0;
		if(paiHao>=0&&paiHao<=35){//万
			type=(paiHao/4)+1;
		}else if(paiHao>=36&&paiHao<=71){
			type=((paiHao/4)-9)+1;
		}else if(paiHao>=72&&paiHao<=107){
			type=((paiHao/4)-18)+1;
		}
		return type;
	}
	
	/**根据牌号计算出牌的类型
	 * @param paiHao
	 * @return
	 */
	public static String getTypeString(int paiHao) {
		String type = "";
		if(paiHao>=0&&paiHao<=35){//万
			type="万";
		}else if(paiHao>=36&&paiHao<=71){
			type="筒";
		}else if(paiHao>=72&&paiHao<=107){
			type="条";
		}
		return type;
	}
}

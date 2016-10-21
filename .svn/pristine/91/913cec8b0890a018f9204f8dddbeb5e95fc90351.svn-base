package com.zxz.dao;

import java.util.HashMap;

import org.junit.Test;

import com.zxz.algorithm.Room;
import com.zxz.domain.OneRoom;


public class OneRoomDao extends BaseDao{
	
	
	public static int roomNumber = 10000;
	static OneRoomDao oneRoomDao;
	
	private OneRoomDao() {
	}
	
	public static OneRoomDao getInstance(){
		if(oneRoomDao!=null){
			return oneRoomDao;
		}else{
			synchronized (OneRoomDao.class) {
				oneRoomDao = new OneRoomDao();
				return oneRoomDao;
			}
		}
	}
	
	/**添加一个房间
	 * @param room
	 * @return
	 */
	public int saveRoom(OneRoom room){
//		insert("OneRoom.save", room);
//		return room.getId();
		return (int)super.queryForObject("OneRoom.createOneRoom",room);
	}
	
	
	public static void main(String[] args) {
//		OneRoom oneRoom = new OneRoom();
//		oneRoom.setTotal(8);
//		oneRoom.setZhama(3);
//		OneRoomDao oneRoomDao = new OneRoomDao();
//		oneRoomDao.saveRoom(oneRoom);
		OneRoomDao oneRoomDao = new OneRoomDao();
		HashMap<String, Object> map = new HashMap<>();
		map.put("roomId", 100589);//房间号
		map.put("userId", 19);//用户id
		map.put("totalGame", 16);//总局数
		map.put("type", 1);//消费 1，充值0,会员充值 3
		map.put("total",0);//充值的房卡数量
		int userCard = oneRoomDao.userConsumeCard(map);
		System.out.println("还剩:"+userCard);
	}
	
	
	/**用户消费房卡 
	 * @param map
	 * @return
	 */
	public int userConsumeCard(HashMap<String, Object> map){
		System.out.println(map);
		return (int)super.queryForObject("OneRoom.userConsumeRoomCard", map);
	}
	
	@Test
	public void testConsumeCard(){
		HashMap<String, Object> map = new HashMap<>();
		map.put("roomId", 100589);//房间号
		map.put("userId", 19);//用户id
		map.put("totalGame", 16);//总局数
		map.put("type", 1);//消费 1，充值0,会员充值 3
		map.put("total",0);//充值的房卡数量
		int userConsumeCard = userConsumeCard(map);
		System.out.println("剩余房卡数:"+userConsumeCard);
	}
	@Test
	public void testSave(){
		OneRoom oneRoom = new OneRoom();
		oneRoom.setTotal(8);
		oneRoom.setZhama(3);
		OneRoomDao oneRoomDao = new OneRoomDao();
		oneRoomDao.saveRoom(oneRoom);
	}
	
}

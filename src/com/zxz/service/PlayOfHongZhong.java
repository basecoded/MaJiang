package com.zxz.service;

import org.apache.mina.common.IoSession;
import org.json.JSONObject;

import com.zxz.domain.User;

public interface PlayOfHongZhong {

	/**登录
	 * @param jsonObject
	 * @param session
	 * @return
	 */
	public boolean login(JSONObject jsonObject, IoSession session);
	
	/**创建房间
	 * @param jsonObject
	 * @param session
	 */
	public void createRoom(JSONObject jsonObject, IoSession session);
	
	
	/**进入房间
	 * @param jsonObject
	 * @param session
	 */
	public void enterRoom(JSONObject jsonObject, IoSession session);
	
	
	/**准备游戏
	 * @param jsonObject
	 * @param session
	 */
	public void readyGame(JSONObject jsonObject, IoSession session);
	
	
	/**开始玩游戏
	 * @param jsonObject
	 * @param session
	 */
	public void playGame(JSONObject jsonObject, IoSession session);
	
	
	/**房主解散房间
	 * @param jsonObject
	 * @param session
	 */
	public void disbandRoom(JSONObject jsonObject, IoSession session);
	
	
	/**房主解散房间 
	 * @param user
	 */
	public void disbandRoom(User user);
	
	
	/**得到我自己的信息
	 * @param jsonObject
	 * @param session
	 */
	public void getMyInfo(JSONObject jsonObject, IoSession session);
	
	/**离开房间
	 * @param jsonObject
	 * @param session
	 */
	public void leaveRoom(JSONObject jsonObject, IoSession session);
	
	
	/**离开房间
	 * @param user
	 */
	
	public void leaveRoom(User user);
	/**得到自己的设置信息
	 * @param jsonObject
	 * @param session
	 */
	public void getSetting(JSONObject jsonObject, IoSession session);
	
	
	/**得到剩余的牌
	 * @param jsonObject
	 * @param session
	 */
	public void getServerInfo(JSONObject jsonObject, IoSession session);
	
	
	/**继续游戏
	 * @param jsonObject
	 * @param session
	 */
	public void continueGame(JSONObject jsonObject, IoSession session);
	
	
	/**设置托管
	 * @param jsonObject
	 * @param session
	 */
	public void settingAuto(JSONObject jsonObject, IoSession session);
	
	
	/**取消托管
	 * @param jsonObject
	 * @param session
	 */
	public void cancelAuto(JSONObject jsonObject, IoSession session);

	/**断线重连
	 * @param jsonObject
	 * @param session
	 */
	public void downGameInfo(JSONObject jsonObject, IoSession session);

	/**得到用户的战绩 
	 * @param jsonObject
	 * @param session
	 */
	public void getUserScore(JSONObject jsonObject, IoSession session);

	/**修改用户推荐号
	 * @param jsonObject
	 * @param session
	 */
	public void recommend(JSONObject jsonObject, IoSession session);

	/**发送消息,聊天的消息,快点啊，我等的花都谢了 
	 * @param jsonObject
	 * @param session
	 */
	public void playAudio(JSONObject jsonObject, IoSession session);
}

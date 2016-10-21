package com.zxz.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mina.common.IoSession;
import org.json.JSONObject;

import com.mysql.jdbc.StringUtils;
import com.zxz.controller.GameManager;
import com.zxz.controller.RoomManager;
import com.zxz.dao.MessageDao;
import com.zxz.dao.UserDao;
import com.zxz.domain.Game;
import com.zxz.domain.Message;
import com.zxz.domain.OneRoom;
import com.zxz.domain.User;
import com.zxz.utils.Constant;
import com.zxz.utils.DateUtils;
import com.zxz.utils.NotifyTool;

/**�õ��û���������Ϣ
 * @author Administrator
 *
 */
public class SettingService implements Constant{
	
	UserDao userDao = UserDao.getInstance();
	MessageDao messageDao = new MessageDao();
	
	
	public void getSetting(JSONObject jsonObject, IoSession session){
		String type = jsonObject.getString("type");
		if(type.equals("getIsOwner")){//�õ��Ƿ��Ƿ���
			getIsOwner(jsonObject,session);
		}else if(type.equals("checkUserIsInRoomBuild")){//�����������û��Ƿ��ڷ�����
			checkUserIsInRoomBuild(jsonObject,session);
		}else if(type.equals("checkUserIsInRoomEnter")){//���뷿�����û��Ƿ��ڷ�������
			checkUserIsInRoomEnter(jsonObject,session);
		}else if(type.equals("getBuyCardMessage")){
			getBuyCardMessage(jsonObject,session);//�õ����򷿿���֪ͨ 
		}
	}

	

	/**�õ�����ŷ�����֪ͨ
	 * @param jsonObject
	 * @param session
	 */
	private void getBuyCardMessage(JSONObject jsonObject, IoSession session) {
		Map<String, Object> map = new HashMap<>();
		map.put("type", 0);
		map.put("rowStart", 0);
		map.put("pageSize", 1);
		List<Message> list = messageDao.selectListByMap(map);
		if(list.size()>0){
			Message message = list.get(0);
			JSONObject outJsonObejct = new JSONObject();
			outJsonObejct.put(method, "getSettingInfo");
			outJsonObejct.put(type, "getBuyCardMessage");
			outJsonObejct.put("message", message.getMessage());
			outJsonObejct.put("createDate", DateUtils.getFormatDate(message.getCreateDate(), "yyyy/MM/dd hh:mm:ss"));
			session.write(outJsonObejct);
		}
	}



	/**���뷿��
	 * @param jsonObject
	 * @param session
	 */
	private void checkUserIsInRoomEnter(JSONObject jsonObject, IoSession session) {
		User user = (User) session.getAttribute("user");
		boolean isInRoom = cheekUserInRoom(user);
		notifyUserIsInRoom(user,session,isInRoom,"checkUserIsInRoomEnter");
	}



	/**����û��Ƿ��ڷ�����
	 * @param jsonObject
	 * @param session
	 */
	private void checkUserIsInRoomBuild(JSONObject jsonObject, IoSession session) {
		User user = (User) session.getAttribute("user");
		boolean isInRoom = cheekUserInRoom(user);
		notifyUserIsInRoom(user,session,isInRoom,"checkUserIsInRoomBuild");
//		if(isInRoom){//����ڷ�������
//			UserService.getRoomInfoAndReplaceUserSession(user);
//		}
	}



	private boolean cheekUserInRoom(User user) {
		boolean isInRoom = false;
		String roomId = user.getRoomId();
		if(roomId!=null&&!"".equals(roomId)){
			OneRoom room = RoomManager.getRoomWithRoomId(roomId);
			//�����Ϸδ��ʼ�������ڷ�������game==null
			//���game!=null,��ʾ��Ϸ�Ѿ���ʼ����
			if(room!=null){
				isInRoom = true;
			}
		}
		return isInRoom;
	}

	/**�û�û���ڷ������棬֪ͨ�û����Խ��뷿��
	 * @param user
	 * @param isInRoom 
	 */
	private void notifyUserIsInRoom(User user,IoSession session, boolean isInRoom,String type) {
		JSONObject outJSONObject = new JSONObject();
		outJSONObject.put(method, "getSettingInfo");
		outJSONObject.put(SettingService.type, type);
		outJSONObject.put("isInRoom", isInRoom);
		outJSONObject.put(discription, "�Ƿ���ߵĽӿ�");
		NotifyTool.notify(session, outJSONObject);;
	}

	/**�õ��Ƿ��Ƿ���
	 * @param jsonObject
	 * @param session
	 */
	private void getIsOwner(JSONObject jsonObject, IoSession session) {
		User user = (User)session.getAttribute("user");
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put(method, "getSettingInfo");
		outJsonObject.put(type, "getIsBank");
		OneRoom oneRoom = RoomManager.getRoomMap().get(user.getRoomId());
		//��ֹ�����ڷ�������,�û�������õ�ʱ���ָ��
		if(oneRoom==null){
			outJsonObject.put("isOwner", false);
			NotifyTool.notify(session, outJsonObject);
			return;
		}
		int createUserId = oneRoom.getCreateUserId();
		if(user.getId()==createUserId){
			outJsonObject.put("isOwner", true);
		}else{
			outJsonObject.put("isOwner", false);
		}
		NotifyTool.notify(session, outJsonObject);
	}



	/**�û��޸��Ƽ���
	 * @param jsonObject
	 * @param session
	 */
	public void recommend(JSONObject jsonObject, IoSession session) {
		String recommendNumber = jsonObject.getString("number");//�Ƽ���
		boolean nullOrEmpty = StringUtils.isNullOrEmpty(recommendNumber);
		if(nullOrEmpty){
			return;
		}
		User user = (User) session.getAttribute("user");
		int id = user.getId();
		User modifyUser = new User();
		modifyUser.setId(id);
		modifyUser.setRecommendId(Integer.parseInt(recommendNumber));
		int result = userDao.modifyUser(modifyUser);
		JSONObject outJSONObject = new JSONObject();
		outJSONObject.put(method, "recommend");
		if(result==1){
			outJSONObject.put(code, true);
		}else{
			outJSONObject.put(code, false);
		}
		NotifyTool.notify(session, outJSONObject);
	}
	
}
package com.zxz.service;

import org.apache.mina.common.IoSession;
import org.json.JSONObject;

import com.zxz.controller.RoomManager;
import com.zxz.domain.OneRoom;
import com.zxz.domain.User;
import com.zxz.utils.Constant;
import com.zxz.utils.NotifyTool;

public class MessageService implements Constant{

	/**发送消息
	 * @param jsonObject
	 * @param session
	 */
	public void playAudio(JSONObject jsonObject, IoSession session) {
		String type = jsonObject.getString("type");
		int messageId = jsonObject.getInt("messageId");
		playAudio(messageId,session,type);
	}

	/**发送文字
	 * @param messageId
	 * @param session
	 */
	private void playAudio(int messageId, IoSession session,String type) {
		User user = (User) session.getAttribute("user");
		OneRoom oneRoom = RoomManager.getRoomWithRoomId(user.getRoomId());
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put(method, "playAudio");
		outJsonObject.put(Constant.type, type);
		outJsonObject.put("messageId", messageId);
		outJsonObject.put("userId", user.getId());
		outJsonObject.put(direction, user.getDirection());
		NotifyTool.notifyIoSessionList(oneRoom.getUserIoSessionList(), outJsonObject);
	}
}

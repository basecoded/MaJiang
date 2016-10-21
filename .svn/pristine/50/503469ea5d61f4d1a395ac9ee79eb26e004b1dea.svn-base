package com.zxz.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Scanner;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.json.JSONObject;

import com.zxz.protobuf.Response.responseStr;

import domain.TestUser;

public class ClientHandler extends IoHandlerAdapter implements IoHandler {

	SendMessageThread sendMessageThread;

	@Override
	public void sessionOpened(IoSession session) throws Exception {
//		sendMessageThread = new SendMessageThread(session);
//		Thread thread = new Thread(sendMessageThread);
//		thread.start();
		System.out.println("-------------------------");
		session.write("我是安全的吗?");
		session.write("我是安全的吗?");
		session.write("我是安全的吗?");
		session.write("我是安全的吗?");
		
	}

	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		System.out.println(message);
	}
	
	 /**  
     * 对象转数组  
     * @param obj  
     * @return  
     */  
    public byte[] toByteArray (Object obj) {      
        byte[] bytes = null;      
        ByteArrayOutputStream bos = new ByteArrayOutputStream();      
        try {        
            ObjectOutputStream oos = new ObjectOutputStream(bos);         
            oos.writeObject(obj);        
            oos.flush();         
            bytes = bos.toByteArray ();      
            oos.close();         
            bos.close();        
        } catch (IOException ex) {        
            ex.printStackTrace();   
        }      
        return bytes;    
    }   
	
	static class SendMessageThread implements Runnable {
		IoSession session;
		public static boolean isLogin = false;
		int roomId;
		
		public SendMessageThread(IoSession session) {
			super();
			this.session = session;
		}
		
		@Override
		public void run() {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("method", "login");
			jsonObject.put("unionid", "obhqFxEwG8ikLFKdoRGR_us81Hig");
			session.write(jsonObject.toString());
			Scanner scanner = new Scanner(System.in);
			while (true) {
				boolean closing = session.isClosing();
				System.out.println("请输入发送的命令 session是否关闭:"+closing);
				String send = scanner.nextLine();
				session.write(send);
			}
		}
	}
}


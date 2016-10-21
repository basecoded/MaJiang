package com.zxz.filter;

import org.apache.log4j.Logger;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;

public class MessageFilter extends IoFilterAdapter {

	private static Logger logger = Logger.getLogger(MessageFilter.class);  
	
	//只会调用一遍
	@Override
	public void sessionOpened(NextFilter nextFilter, IoSession session)
			throws Exception {
		System.out.println("我的过滤器开始");
		nextFilter.sessionOpened(session);
		System.out.println("我的过滤器结束");
	}

	@Override
	public void messageReceived(NextFilter nextFilter, IoSession session,
			Object message) throws Exception {
		//System.out.println("过滤器前:" + "messageReceived");
		super.messageReceived(nextFilter, session, message);
	}

	
	@Override
	public void messageSent(NextFilter nextFilter, IoSession session, Object message) throws Exception {
		super.messageSent(nextFilter, session, message);
		logger.info("返回的时间:"+System.currentTimeMillis()+"返回的值是:"+session.getRemoteAddress()+":"+message+" "+Thread.currentThread().getId());
//		System.out.println("返回的时间:"+System.currentTimeMillis()+"返回的值是:"+session.getRemoteAddress()+":"+message+" "+Thread.currentThread().getId());
//		System.out.println("返回的值是:"+session.getId()+":"+writeRequest);
	}

	/*
	 * 在消息发送之前
	 */
	@Override
	public void filterWrite(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
//		//MyWriteRequest myWriteRequest = new MyWriteRequest(writeRequest);
	//	super.filterWrite(nextFilter, session, writeRequest);
		MyWriteRequest myWriteRequest = new MyWriteRequest(writeRequest);
		super.filterWrite(nextFilter, session, myWriteRequest);
	}
	
	class MyWriteRequest extends WriteRequest{

		public MyWriteRequest(Object message) {
			super(message);
		}

		@Override
		public Object getMessage() {
			return super.getMessage()+"end";
		}
		
	}
	
}

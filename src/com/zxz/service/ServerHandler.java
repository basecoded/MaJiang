package com.zxz.service;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoAcceptorConfig;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.example.echoserver.ssl.BogusSSLContextFactory;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;
import org.json.JSONObject;

import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import com.zxz.domain.User;
import com.zxz.filter.MessageFilter;
import com.zxz.protobuf.Response;
import com.zxz.protobuf.Response.responseStr;

import domain.TestUser;
import javassist.bytecode.ByteArray;

public class ServerHandler extends IoHandlerAdapter implements IoHandler {

	private static final int PORT = 8080;
	/** Set this to true if you want to make the server SSL */
	private static final boolean USE_SSL = false;
	DateServiceImpl dateService = new DateServiceImpl();
	private static Logger logger = Logger.getLogger(ServerHandler.class);  
	
	public static void main(String[] args) throws Exception {
		// SocketAcceptor acceptor = new NioSocketAcceptor();
		// DefaultIoFilterChainBuilder chain= acceptor.getFilterChain();
		// AuthFilter authFilter = new AuthFilter();
		// chain.addLast("myChin", new MyProtocolCodecFilter(new
		// TextLineCodecFactory()));
		// MessageFilter messageFilter = new MessageFilter();//��Ϣ������
		// chain.addLast( "codec", new MyProtocolCodecFilter(new
		// TextLineCodecFactory( Charset.forName( "UTF-8" ))));
		// chain.addLast("authFilter", authFilter);
		// chain.addLast("message", messageFilter);
		// acceptor.setHandler(new ServerHandler());
		// acceptor.bind(new InetSocketAddress(PORT));

		IoAcceptor acceptor = new SocketAcceptor();
		IoAcceptorConfig config = new SocketAcceptorConfig();
		DefaultIoFilterChainBuilder chain = config.getFilterChain();

		// Add SSL filter if SSL is enabled.
		if (USE_SSL) {
			addSSLSupport(chain);
		}
		chain.addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory()));
		addLogger(chain);
		MessageFilter messageFilter = new MessageFilter();//��Ϣ������
		chain.addLast("message", messageFilter);
		// Bind
		acceptor.bind(new InetSocketAddress(PORT), new ServerHandler(), config);
		logger.info("�����������������Ķ˿ں���:" + PORT);
	}

	private static void addLogger(DefaultIoFilterChainBuilder chain) throws Exception {
		chain.addLast("logger", new LoggingFilter());
		System.out.println("Logging ON");
	}

	private static void addSSLSupport(DefaultIoFilterChainBuilder chain) throws Exception {
		SSLFilter sslFilter = new SSLFilter(BogusSSLContextFactory.getInstance(true));
		chain.addLast("sslFilter", sslFilter);
		System.out.println("SSL ON");
	}

	// ���ͻ������ӽ���ʱ
	@SuppressWarnings("static-access")
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		nofityUserConn(session);
		dateService.addOnLineUser();
//		sendProtobuf(session);
		
//		TestUser testUser = new TestUser("gushuang");
//		session.write(testUser);
		
	}

	private void sendProtobuf(IoSession session) throws InvalidProtocolBufferException {
		System.out.println("sessionOpened");
		Response.responseStr.Builder builder = Response.responseStr.newBuilder();
		builder.setMethod("connection");
		builder.setDescription("�������ӳɹ�");
		responseStr responseStr = builder.build();
		System.out.println(responseStr.toByteArray());
		
		responseStr responseStr2 = responseStr.parseFrom(responseStr.toByteArray());
		System.out.println("byteArrayLenth:"+responseStr.toByteArray().length);
		System.out.println("1��"+responseStr2);
		String sresponse = JsonFormat.printToString(responseStr2);
		System.out.println(sresponse);
		//System.out.println("�ı���:"+sresponse);
		byte[] byteArray = responseStr.toByteArray();
		System.out.println(byteArray);
		ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
		System.out.println(byteBuffer);
		session.write(byteBuffer);
	}

	
	/**֪ͨ�û��Ѿ��Ѿ��������ӳɹ�
	 * @param session
	 */
	private void nofityUserConn(IoSession session) {
		JSONObject outJsonObject = new JSONObject();
		outJsonObject.put("method", "connection");
		outJsonObject.put("discription", "�����������");
		session.write(outJsonObject.toString());
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		super.exceptionCaught(session, cause);
		//session.close();
		// new UserDroppedService(session);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		dateService.subOnLineUser();//���ߵ�ʱ��,���������û���
		session.close();
		User user = (User) session.getAttribute("user");
		if(user!=null){
			String roomId = user.getRoomId();
			if(roomId!=null&&!"".equals(roomId)){
				logger.error("�������Ĺ����е���..." + "���ߵ����IP��ַ��:"+session.getRemoteAddress()+"��ҵ��ǳ���:"+user.getNickName());
				new UserDroppedService(session).userDropLine();;
			}
			session.removeAttribute("user");//���û���session���Ƴ�
			dateService.subLoginUser();//���ߵ�¼��-1
		}
		logger.info("�ͻ��������˶϶Ͽ�����" + session.getRemoteAddress());
	}

	class MyThread implements Runnable {
		IoSession session;

		public MyThread(IoSession session) {
			super();
			this.session = session;
		}

		@Override
		public void run() {
			Scanner scan = new Scanner(System.in);
			while (true) {
				System.out.println("������...:");
				String nextLine = scan.nextLine();
				try {
					nextLine = URLEncoder.encode(nextLine, "UTF-8");
					session.write(nextLine);
					nextLine = URLDecoder.decode(nextLine, "UTF-8");
					System.out.println(nextLine);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		/*System.out.println("sessionCreated");
		MyThread myThread = new MyThread(session);
		Thread thread = new Thread(myThread);
		thread.start();*/
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		super.sessionIdle(session, status);
		logger.info("����.................");
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, message);
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		logger.info("���յ���Ϣ:" + session.getRemoteAddress() + "\t" + message);
		JSONObject jsonObject = null;
		boolean isFormat = true;
		try {
			jsonObject = new JSONObject(message.toString());
		} catch (Exception e) {
			e.printStackTrace();
			isFormat = false;
			session.write("���ݴ����ʽ������ʹ��json�ַ�������");
			session.close();
			logger.error("json�ַ�������error:" + message.toString()+" ip:"+session.getRemoteAddress());
		}
		if (isFormat) {
			new ServiceMaster().serviceStart(session, jsonObject);
		}
	}

}

package com.zxz.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import org.apache.log4j.Logger;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoAcceptorConfig;
import org.apache.mina.example.echoserver.ssl.BogusSSLContextFactory;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.codec.MyProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import com.gs.rpc.RpcProvider;
import com.zxz.filter.AuthFilter;
import com.zxz.filter.MessageFilter;
import com.zxz.filter.MyBogusSslContextFactory;
import com.zxz.filter.SSLContextGenerator;
import com.zxz.service.ServerHandler;


public class StartServer {

	private static final int PORT = 58585;
	/** Set this to true if you want to make the server SSL */
	private static final boolean USE_SSL = false;
	private static Logger logger = Logger.getLogger(StartServer.class);

	public static void main(String[] args) throws IOException, Exception {
		startServer();
		RpcProvider.startProvider(9999);
	}

	private static void startServer() throws Exception, IOException {
		IoAcceptor acceptor = new SocketAcceptor();
		IoAcceptorConfig config = new SocketAcceptorConfig();
		DefaultIoFilterChainBuilder chain = config.getFilterChain();
		if (USE_SSL) {
			//addSSLSupport(chain);
			addSSLMe(chain);
		}
		chain.addLast("codec", new MyProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
		// addLogger(chain);
		MessageFilter messageFilter = new MessageFilter();// 信息过滤器
		AuthFilter authFilter = new AuthFilter();// 权限
		chain.addLast("authFilter", authFilter);
		chain.addLast("message", messageFilter);
		acceptor.bind(new InetSocketAddress(PORT), new ServerHandler(), config);
		logger.info("服务器启动，监听的端口号是:" + PORT);
	}

	private static void addSSLMe(DefaultIoFilterChainBuilder chain) throws GeneralSecurityException, IOException {
		// 设置加密过滤器
//		SSLFilter sslFilter = new SSLFilter(MyBogusSslContextFactory.getInstance(true));
		SSLFilter sslFilter = new SSLFilter(new SSLContextGenerator().getSslContext());
		// 设置客户连接时需要验证客户端证书
		sslFilter.setNeedClientAuth(true);
		chain.addLast("sslFilter", sslFilter);
		System.out.println("SSL ON");
	}

	private static void addSSLSupport(DefaultIoFilterChainBuilder chain) throws Exception {
     	SSLFilter sslFilter = new SSLFilter(BogusSSLContextFactory.getInstance(true));
		chain.addLast("sslFilter", sslFilter);
		System.out.println("SSL ON");
	}

	private static void addLogger(DefaultIoFilterChainBuilder chain) throws Exception {
		chain.addLast("logger", new LoggingFilter());
		System.out.println("Logging ON");
	}
}

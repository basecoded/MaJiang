package com.zxz.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.SSLFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;

import com.zxz.filter.SSLContextGenerator;

public class StartClient {
	public static String Address = "192.168.1.105";
//	public static String Address = "101.201.115.208";
	//public static String Address = "localhost";
	public static void main(String[] args) throws GeneralSecurityException, IOException {
		SocketConnectorConfig config = new SocketConnectorConfig();
		DefaultIoFilterChainBuilder chain = config.getFilterChain();
		addSSLFilter(chain);
		//System.out.println("SSL ON");
		chain.addLast( "codec", new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName( "UTF-8" ))));
		SocketConnector connector = new SocketConnector();
		ClientHandler clientHandler = new ClientHandler();
		SocketAddress address = new InetSocketAddress(Address, 8081);
		ConnectFuture future = connector.connect(address, clientHandler, config);
	}
	
	//认证
	private static void addSSLFilter(DefaultIoFilterChainBuilder chain) throws GeneralSecurityException, IOException {
//		SSLFilter sslFilter = new SSLFilter(MyBogusSslContextFactory.getInstance(true));
		SSLFilter sslFilter = new SSLFilter(new SSLContextGenerator().getSslContext());
		// 设置客户连接时需要验证客户端证书
		sslFilter.setNeedClientAuth(true);
		chain.addLast("sslFilter", sslFilter);
	}
}

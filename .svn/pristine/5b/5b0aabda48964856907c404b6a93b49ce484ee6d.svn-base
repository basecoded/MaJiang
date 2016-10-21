package com.gs.rpc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.zxz.service.HelloService;
import com.zxz.service.HelloServiceImpl;

public class RpcProvider {

	/**
	 * 是否开启服务
	 */
	public static boolean isProvideSerice = true;

	public static void main(String[] args) {
		 startProvider(1234);
	}
	
	
	/**
	 * 开启服务
	 */
	public static void startProvider(int port) {
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(port);
			System.out.println("RPC服务器启动,监听的端口号是:"+port);
			while (isProvideSerice) {
				Socket socket = serverSocket.accept();
				ProviderThread providerThread = new ProviderThread(socket);
				Thread thread = new Thread(providerThread);
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

package com.gs.rpc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.zxz.service.HelloService;
import com.zxz.service.HelloServiceImpl;

public class RpcProvider {

	/**
	 * �Ƿ�������
	 */
	public static boolean isProvideSerice = true;

	public static void main(String[] args) {
		 startProvider(1234);
	}
	
	
	/**
	 * ��������
	 */
	public static void startProvider(int port) {
		try {
			@SuppressWarnings("resource")
			ServerSocket serverSocket = new ServerSocket(port);
			System.out.println("RPC����������,�����Ķ˿ں���:"+port);
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

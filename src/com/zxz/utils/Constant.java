package com.zxz.utils;

public interface Constant {

	public static final String method="method";//方法名
	public static final String code ="code";
	public static final String success ="success";
	public static final String error = "error";//错误
	public static final String discription = "discription";//描述
	public static final String direction = "dirction";//方向
	public static final String type = "type";//类型
	public static final int GONG_GANG = 0;//可以公杠
	public static final int AN_GANG = 1;//可以暗杠 
	
	/**
	 * 出牌的状态
	 */
	public static final int GAGME_STATUS_OF_CHUPAI = 0;//出牌的状态
	
	/**
	 * 听牌的状态
	 */
	public static final int GAGME_STATUS_OF_PENGPAI = 1;//碰牌的状态
	
	/**
	 * 杠牌的状态 (接杠)
	 */
	public static final int GAGME_STATUS_OF_GANGPAI = 2;//普通杠牌
	
	/**
	 * 暗杠的状态 
	 */
	public static final int GAGME_STATUS_OF_ANGANG = 3;//暗杠
	
	/**
	 * 公杠的状态 
	 */
	public static final int GAGME_STATUS_OF_GONG_GANG = 4;//公杠
	
	
	/**
	 * 游戏等待开始
	 */
	public static final int GAGME_STATUS_OF_WAIT_START = 0;
	
	
	/**
	 * 游戏进行中
	 */
	public static final int GAGME_STATUS_OF_IS_GAMING = 1;
	
	/**
	 * 游戏是否结束 
	 */
	public static final String GAME_END = "GAME_END";//游戏是否结束
	
	
	/**
	 * 用户注册的时候默认的房卡数量
	 */
	public static final int DEFAULT_USER_REGIST_ROOMCARD = 10;
	
	/**
	 * 游戏结束倒计时
	 */
	public static final int TIME_TO_START_GAME = 30000;//2分钟
	
	
	/**
	 * 测试的游戏局数  1000
	 */
	public static final int TEST_TOTAL_GAME = 1000;
	
	
	
	
}

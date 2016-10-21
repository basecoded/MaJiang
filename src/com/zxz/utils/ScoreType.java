package com.zxz.utils;

public interface ScoreType {

	/**
	 * 杠得分
	 */
	public static final int ADD_SCORE_FOR_GANG = 3;
	
	/**
	 * 放杠减分
	 */
	public static final int REDUCE_SCORE_FOR_GANG = -3;
	
	/**
	 * 明杠加分
	 */
	public static final int ADD_SCORE_FOR_MINGGANG = 3;
	
	
	/**
	 * 明杠加分
	 */
	public static final int REDUCE_SCORE_FOR_MINGGANG = -1;
	
	/**
	 * 胡牌得分
	 */
	public static final int ADD_SCORE_FOR_HUPAI = 6;
	
	/**
	 * 暗杠得分
	 */
	public static final int ADD_SCORE_FOR_ANGANG = 6;
	
	
	/**
	 * 胡牌减分
	 */
	public static final int REDUCE_SCORE_FOR_HUPAI = -2;
	
	
	/**
	 * 暗杠减分
	 */
	public static final int REDUCE_SCORE_FOR_ANGANG = -2;
	
}

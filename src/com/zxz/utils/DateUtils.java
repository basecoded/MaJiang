package com.zxz.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	/**得到指定格式的时间
	 * @param date
	 * @param pattern  yyyy/MM/dd hh:mm:ss
	 * @return
	 */
	public static String getFormatDate(Date date,String pattern) {
		DateFormat sdf2 = new SimpleDateFormat(pattern);		
		String sdate = sdf2.format(date);
		return sdate;
	}
	
}

package dao.test;

import java.util.Date;

import org.junit.Test;

import com.zxz.dao.UserScoreDao;
import com.zxz.domain.UserScore;

public class UserScoreDaoTest {

	UserScoreDao userScoreDao = UserScoreDao.getInstance();
	
	@Test
	public void testSave(){
		Date createDate = new Date();
		UserScore userScore = new UserScore(1, 10, 100,99, createDate);
		userScoreDao.saveUserScore(userScore);
	}
	
}

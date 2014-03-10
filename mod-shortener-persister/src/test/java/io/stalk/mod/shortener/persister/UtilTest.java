package io.stalk.mod.shortener.persister;

import org.junit.Assert;
import org.junit.Test;

public class UtilTest {

	@Test
	public void test_bijective(){
		
		int longId = 2147483647;
		
		String shortId = BijectiveUtil.encode(longId);
		
		int originalId = BijectiveUtil.decode(shortId); // longId
		
		System.out.println("Short Id :"+shortId);
		System.out.println("Long  Id :"+BijectiveUtil.decode(shortId));
		
		Assert.assertEquals("Must be the original id", longId, originalId);
		
	}
}

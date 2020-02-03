package com.cjy.crawler;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.cjy.crawler.service.ProductService;

@TestPropertySource(properties = "app.scheduling.enable=false")
@RunWith(SpringRunner.class)
@SpringBootTest
public class productServiceTest {
	@Autowired
	ProductService productService;

	@Test
	public void testKeywords() {
		String kws=productService.getKeywords();
		//System.out.println(kws);
        Assert.assertEquals(kws,"酒精 消毒,口罩,hottoys");

	}

}

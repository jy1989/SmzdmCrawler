package com.cjy.crawler.schedule;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cjy.crawler.service.ProductService;

@Component
public class ProductJob {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	Map<String, JSONObject> keyMap = new HashMap<String, JSONObject>();
	boolean firstRun = true;
	@Value("${search.url}")
	String searchUrl;
	int counter = 0;
	boolean debug = false;
	
	@Autowired
	ProductService productService;

	@Scheduled(fixedDelayString = "${search.delay.milliseconds:20000}")
	public void cron() throws Exception {
		doJob();
	}

	private void doJob() {
		String keywords = productService.getKeywords();
		String[] kws = keywords.split(",");
		logger.info("关键词:{}", keywords);
		for (int i = 0; i < kws.length; i++) {
			String kw = kws[i];

			try {
				search(kw);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if (counter > kws.length * 500) {
			resetCounter();
		} else {
			firstRun = false;
		}

	}

	public void search(String key) throws Exception {
		String url = searchUrl.replace("{key}", key);
		Document doc = Jsoup.connect(url).get();

		Element mainList = doc.selectFirst("#feed-main-list");
		if (mainList == null) {
			throw new Exception("搜索结果为空");
		}
		Elements contents = mainList.select(".z-feed-content");

		for (Element content : contents) {
			String title = content.selectFirst(".feed-block-title a").attr("title");
			String price = content.selectFirst(".feed-block-title a .z-highlight").text();
			String desc = content.selectFirst(".feed-block-descripe").text();
			String href = content.selectFirst(".feed-block-title a").attr("href");
			String link = content.selectFirst(".z-feed-foot .feed-link-btn a").attr("href");

			JSONObject jo = productService.setItem(title, price, desc, href, link);
			if (!keyMap.containsKey(title)) {
				if (!firstRun) {
					productService.sendMsg(jo);
				}
				if (debug && counter == 0) {
					productService.sendMsg(jo);
				}
				keyMap.put(title, jo);
			}
			counter++;

		}
	}

	private void resetCounter() {
		keyMap.clear();
		counter = 0;
		firstRun = true;

	}

	 

}

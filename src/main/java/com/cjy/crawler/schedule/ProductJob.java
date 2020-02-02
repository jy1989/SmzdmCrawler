package com.cjy.crawler.schedule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.cjy.crawler.service.MailService;

@Component
public class ProductJob {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	Map<String, JSONObject> keyMap = new HashMap<String, JSONObject>();
	boolean firstRun = true;
	@Value("${search.url}")
	String searchUrl;
	@Value("${search.keywords}")
	String keywords;
	int counter = 0;
	boolean debug = false;
	@Autowired
	MailService mailService;

	@Scheduled(fixedDelayString = "${search.delay.milliseconds:20000}")
	public void cron() throws Exception {
		logger.info("---------执行时间:{}--------", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		doJob();
	}

	private void doJob() {
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

	private void search(String key) throws Exception {
		String url = searchUrl.replace("{key}", key);
		Document doc;

		doc = Jsoup.connect(url).get();

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

			JSONObject jo = setItem(title, price, desc, href, link);
			if (!keyMap.containsKey(title)) {
				if (!firstRun) {
					sendMsg(jo);
				}
				if (debug && counter == 0) {
					sendMsg(jo);
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

	private void sendMsg(JSONObject jo) throws IOException {

		String content = "<div>" + jo.getString("desc") + "</div><div><b>" + jo.getString("price") + "</b></div>"
				+ "<div><a href=\"" + jo.getString("link") + "\">" + jo.getString("link") + "</a></div>";
		String title = jo.getString("title");
		mailService.sendHtmlMail(title, content);

		logger.info("发送了通知！{}", jo.getString("title"));
	}

	private JSONObject setItem(String title, String price, String desc, String href, String link) {
		JSONObject jo = new JSONObject();
		jo.put("title", title);
		jo.put("price", price);
		jo.put("desc", desc);
		jo.put("href", href);
		jo.put("link", link);
		return jo;
	}

}

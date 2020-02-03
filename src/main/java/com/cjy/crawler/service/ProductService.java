package com.cjy.crawler.service;

import java.io.IOException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Value("${search.keywords.url}")
	String keywordsUrl;
	@Autowired
	MailService mailService;

	public String getKeywords() {
		// https://github.com/jy1989/SmzdmCrawler/issues/2
		try {
			Document doc = Jsoup.connect(keywordsUrl).get();
			String keywords = doc.selectFirst(
					"task-lists table tr td p")
					.text();
			return keywords;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public JSONObject setItem(String title, String price, String desc, String href, String link) {
		JSONObject jo = new JSONObject();
		jo.put("title", title);
		jo.put("price", price);
		jo.put("desc", desc);
		jo.put("href", href);
		jo.put("link", link);
		return jo;
	}

	public void sendMsg(JSONObject jo) throws IOException {

		String content = "<div>" + jo.getString("desc") + "</div><div><b>" + jo.getString("price") + "</b></div>"
				+ "<div><a href=\"" + jo.getString("link") + "\">" + jo.getString("link") + "</a></div>";
		String title = jo.getString("title");
		mailService.sendHtmlMail(title, content);

		logger.info("发送了通知！{}", jo.getString("title"));
	}
}

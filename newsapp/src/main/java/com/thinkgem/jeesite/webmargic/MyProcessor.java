package com.thinkgem.jeesite.webmargic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
 
public class MyProcessor implements PageProcessor {
    // 抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(100);
    {
    }
    private static int count =0;
     
    @Override
    public Site getSite() {
    	site.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");

        return site;
    }
    
    private Map<String,String> hasCheck = new HashMap<String,String>();
 
    @Override
    public void process(Page page) {
        //判断链接是否符合http://www.cnblogs.com/任意个数字字母-/p/7个数字.html格式
    	//System.out.println(page.getUrl());
    	//System.out.println(page.getUrl().regex("http://www.zaowannews.com/a/[a-z 0-9 -]+/[0-9]{5}.html").match());
    	
    	if(!page.getUrl().regex("http://www.zaowannews.cn").match()) return;
    	
        if(!page.getUrl().regex("http://www.zaowannews.cn/a/[a-z 0-9 -]+/[0-9]{5,10}.html").match()){
            //加入满足条件的链接
        	List<String> okurls = new ArrayList<String>();
        	List<String> urls = page.getHtml().xpath("//a/@href").all();
        	//System.out.println(urls);
        	for(String url:urls){
        		if(url.startsWith("http://www.zaowannews.cn")){
        			if(hasCheck.get(url)==null){
            			okurls.add(url);

        			}
        		}
        	}

            page.addTargetRequests(okurls);
        }else{                  
        	hasCheck.put(page.getUrl().get(),"1");
        	
            //获取页面需要的内容
            System.out.println("抓取的内容："+
                    page.getHtml().xpath("//div[@class='arc']").get()
                    );
            count ++;
        }
    }
 
    public static void main(String[] args) {
        long startTime, endTime;
        System.out.println("开始爬取...");
        startTime = System.currentTimeMillis();
        //http://www.zaowannews.cn/a/zaojian/114691.html
        Spider.create(new MyProcessor()).addUrl("http://www.zaowannews.cn").thread(5).run();
        endTime = System.currentTimeMillis();
        System.out.println("爬取结束，耗时约" + ((endTime - startTime) / 1000) + "秒，抓取了"+count+"条记录");
    }
 
}
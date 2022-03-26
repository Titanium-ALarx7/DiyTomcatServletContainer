package individual.wangtianyao.diytomcat.util;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import individual.wangtianyao.diytomcat.http.Header;
import individual.wangtianyao.diytomcat.http.Request;
import individual.wangtianyao.diytomcat.http.Response;
import individual.wangtianyao.diytomcat.http.StandardSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.util.*;

public class SessionManager {
    private static Map<String, StandardSession> sessionMap = new HashMap<>();
    private static int defaultTimeout = getTimeout();

    static {
        startSessionOutdateCheckThread();
    }

    private static int getTimeout(){
        int defaultTimeout = 30;
        try{
            Document d = Jsoup.parse(Header.webXMLFile, "utf-8");
            Elements es = d.select("session-config session-timeout");
            if(!es.isEmpty()) return Integer.parseInt(es.get(0).text());
        }catch(Exception e){e.printStackTrace();}
        return defaultTimeout;
    }

    private static void startSessionOutdateCheckThread(){
        new Thread(
                ()->{
            while(true){
                checkOutDateSession();
                ThreadUtil.sleep(1000 * 30);
            }
        }
        ).start();
    }

    private static void checkOutDateSession(){
        Set<String> jsessionids = sessionMap.keySet();
        List<String> outdatedJessionIds = new ArrayList<>();
        for(String jid: jsessionids){
            StandardSession session = sessionMap.get(jid);
            long interval = System.currentTimeMillis() - session.getLastAccessedTime();
            if(interval > session.getMaxInactiveInterval()* 1000L) outdatedJessionIds.add(jid);
        }
        for(String jid: outdatedJessionIds) sessionMap.remove(jid);
    }

    public static synchronized String generateSessionId(){
        String result = null;
        byte[] bytes = RandomUtil.randomBytes(16);
        result = new String(bytes);
        result = SecureUtil.md5(result);
        result = result.toUpperCase();
        return result;
    }

    public static HttpSession getSession(String jsessionid, Request reqs, Response resp){
        if(jsessionid==null) return newSession(reqs, resp);
        else{
            StandardSession currentSession = sessionMap.get(jsessionid);
            if(currentSession==null) return newSession(reqs, resp);
            else{
                currentSession.setLastAccessedTime(System.currentTimeMillis());
                createCookieBySession(currentSession, reqs, resp);
                return currentSession;
            }
        }
    }

    private static HttpSession newSession(Request reqs, Response resp){
        ServletContext servletContext=  reqs.getServletContext();
        String sid = generateSessionId();
        StandardSession session = new StandardSession(sid, servletContext);
        session.setMaxInactiveInterval(defaultTimeout);
        sessionMap.put(sid, session);
        createCookieBySession(session, reqs, resp);
        return  session;
    }

    private static void createCookieBySession(HttpSession session, Request reqs, Response resp){
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setMaxAge(session.getMaxInactiveInterval());
        cookie.setPath(reqs.getContext().getPath());
        resp.addCookie(cookie);
    }
}

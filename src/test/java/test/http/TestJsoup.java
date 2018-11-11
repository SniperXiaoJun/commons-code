/* __________              _____                                          *\
** \______   \____   _____/ ____\____   ____        Ponfee's code         **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \       (c) 2017-2018, MIT    **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/       http://www.ponfee.cn  **
**  |____|   \____/|___|  /__|  \___  >\___  >                            **
**                      \/          \/     \/                             **
\*                                                                        */

package test.http;

import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import code.ponfee.commons.http.Http;

/**
 * 
 * @author Ponfee
 */
public class TestJsoup {

    private static final String COOKIE =
        "preLG_45769077=2018-08-28+20%3A38%3A33; sid=FCCHLBeiXXYY7ebc24JQ; popFlag520#45769077=1; isFirstLoadPage=1; JSESSIONID=abck6fPzeeZYt-r-9WbCw; Hm_lvt_2c8ad67df9e787ad29dbd54ee608f5d2=1541922829; zxr_index_45769077=1; validate_45769077=yes; smail_45769077=yes; hasShowTip=1; dialogSec=10; isSafeId=%2C45769077; isShowSafeDialog=-1; _pc_login_sec=1; login_health=58786d1c9338cc82d46e03c51c7363e8cb0d95c8b7c5360fce6a0da1928f1b8abaf9d9ea8e6ca0082e59ed6c77e6649a72ed3289875eb4a505d244a77f287171; token=1382287360.1541929686159.d38fdc1fc4758fd4fbf0a15d0f0d494f; p=%5E%7Eworkcity%3D10101206%5E%7Esex%3D0%5E%7Emt%3D1%5E%7Enickname%3D%E9%9A%8F%E7%BC%98%5E%7Edby%3D8d14b74c8de0aa52%5E%7Elh%3D1382287360%5E%7Eage%3D30%5E%7E; isSignOut=%5E%7ElastLoginActionTime%3D1541929686159%5E%7E; mid=%5E%7Emid%3D1382287360%5E%7E; loginactiontime=%5E%7Eloginactiontime%3D1541929686159%5E%7E; logininfo=%5E%7Elogininfo%3D1382287360%5E%7E; live800=%5E%7EinfoValue%3DuserId%253D1382287360%2526name%253D1382287360%2526memo%253D%5E%7E; preLG_1382287360=2018-11-11+15%3A50%3A47; dgpw=1; _pc_login_isWeakPwd=1; Hm_lpvt_2c8ad67df9e787ad29dbd54ee608f5d2=1541929675; clientp=4223";

    // ========================================================see
    @Test(timeout = 999999999)
    public void viewSeeme() {
        viewSee(0);
    }

    @Test(timeout = 999999999)
    public void deleteSeeme() { // 谁看过我
        deleteSee(0);
    }

    @Test(timeout = 999999999)
    public void viewMesee() {
        viewSee(1);
    }

    @Test(timeout = 999999999)
    public void deleteMesee() { // 我看过谁
        deleteSee(1);
    }

    // ========================================================send
    @Test(timeout = 999999999)
    public void viewSendme() { // 查看收件箱
        viewSend(1);
    }

    @Test(timeout = 999999999)
    public void deleteSendme() { // 删除收件箱
        delSend(1);
    }

    @Test(timeout = 999999999)
    public void viewMesend() { // 查看发件箱
        viewSend(4);
    }

    @Test(timeout = 999999999)
    public void deleteMesend() { // 删除发件箱
        delSend(4);
    }

    // ==================================================================
    private void viewSee(int type) {
        Http page = Http.get("http://profile.zhenai.com/v2/visit/ajax.do").addHeader("COOKIE", COOKIE).addParam("type", type);
        System.out.println(page.addParam("page", "1").request());

        Http delete = Http.get("http://profile.zhenai.com/v2/visit/delete.do").addHeader("COOKIE", COOKIE).addParam("type", type);
        System.out.println(delete.addParam("memberid", "999999999").request());
    }

    @SuppressWarnings("unchecked")
    private void deleteSee(int type) {
        Http page = Http.get("http://profile.zhenai.com/v2/visit/ajax.do").addHeader("COOKIE", COOKIE).addParam("type", type);
        Http delete = Http.get("http://profile.zhenai.com/v2/visit/delete.do").addHeader("COOKIE", COOKIE).addParam("type", type);
        for (;;) {
            try {
                page.addParam("page", 1);
                Map<String, Object> map = page.request(Map.class);
                if ((int) map.get("code") == 0) {
                    System.err.println("page fail");
                    break;
                }
                List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("data");
                for (Map<String, Object> item : list) {
                    System.out.print(item.get("memberId") + ", ");
                    Thread.sleep(50);
                    Map<String, Object> delRes = delete.addParam("memberid", item.get("memberId")).request(Map.class);
                    if ((int) delRes.get("data") == 0) {
                        System.err.println("del fail");
                    }
                }
                System.out.println();
                Thread.sleep(200);
            } catch (Exception e) {}
        }
    }

    // ==================================================================
    private void viewSend(int type) {
        Http page = Http.get("http://profile.zhenai.com/v2/mail/list.do").addHeader("COOKIE", COOKIE).addParam("showType", type);
        String html = page.addParam("pageNo", 1).request();
        Document doc = Jsoup.parse(html, "UTF-8");
        Elements mes = doc.select("section[class='mod-msg-item exp-mail-item'] > a[class='new-icon-close deleteMail-js']");
        for (Element elem : mes) {
            System.out.print(elem.attr("memberid") + ", ");
        }
        System.out.println();
        Http delete = Http.post("http://profile.zhenai.com/v2/mail/deleteMemberNew.do").addHeader("COOKIE", COOKIE);
        System.out.println(delete.data("memberId=999999999").request());
    }

    private void delSend(int type) {
        Http page = Http.get("http://profile.zhenai.com/v2/mail/list.do").addHeader("COOKIE", COOKIE).addParam("showType", type);
        for (;;) {
            try {
                String html = page.addParam("pageNo", 1).request();
                Document doc = Jsoup.parse(html, "UTF-8");
                Elements mes = doc.select("section[class='mod-msg-item exp-mail-item'] > a[class='new-icon-close deleteMail-js']");
                boolean found = false;
                for (Element elem : mes) {
                    found = true;
                    Http delete = Http.post("http://profile.zhenai.com/v2/mail/deleteMemberNew.do").addHeader("COOKIE", COOKIE);
                    String memberid = elem.attr("memberid");
                    System.out.print(memberid + ", ");
                    delete.data("memberId=" + memberid).request();
                    Thread.sleep(50);
                }
                System.out.println();
                if (!found) {
                    break;
                }
                Thread.sleep(200);
            } catch (Exception e) {}
        }
    }

}

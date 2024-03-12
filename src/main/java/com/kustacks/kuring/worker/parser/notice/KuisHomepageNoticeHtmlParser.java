package com.kustacks.kuring.worker.parser.notice;

import com.kustacks.kuring.worker.scrap.deptinfo.DeptInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class KuisHomepageNoticeHtmlParser extends NoticeHtmlParserTemplate {

    @Override
    public boolean support(DeptInfo deptInfo) {
        return true;
    }

    @Override
    protected Elements selectImportantRows(Document document) {
        return document.select(".board-table > tbody > tr").select(".notice");
    }

    @Override
    protected Elements selectNormalRows(Document document) {
        return document.select(".board-table > tbody > tr").not(".notice");
    }

    @Override
    protected String[] extractNoticeFromRow(Element row) {
        Elements tds = row.getElementsByTag("td"); // jf_viewArtcl('job', '4083', '1116603')

        // articleId, postedDate, subject
        String[] splitResults = tds.get(1).select("a").attr("onclick")
                .replace("jf_viewArtcl('", "")
                .replace("')", "")
                .split("', '");

        String number = splitResults[2];
        String date = tds.get(3).text();
        String title = tds.get(1).select("strong").text();

        return new String[]{number, date, title};
    }
}

package com.kustacks.kuring.worker.parser.notice;

import com.kustacks.kuring.common.exception.InternalLogicException;
import com.kustacks.kuring.common.exception.code.ErrorCode;
import com.kustacks.kuring.worker.scrap.deptinfo.DeptInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class NoticeHtmlParserTemplate {

    public RowsDto parse(Document document) {
        try {
            Elements importantRows = selectImportantRows(document);
            Elements normalRows = selectNormalRows(document);

            List<String[]> importantRowList = extractNoticeListFromRows(importantRows);
            List<String[]> normalRowList = extractNoticeListFromRows(normalRows);

            return new RowsDto(importantRowList, normalRowList);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
            throw new InternalLogicException(ErrorCode.NOTICE_SCRAPER_CANNOT_PARSE, e);
        }
    }

    private List<String[]> extractNoticeListFromRows(Elements rows) {
        if(rows.isEmpty()) {
            return Collections.emptyList();
        }

        return rows.stream()
                .map(this::extractNoticeFromRow)
                .collect(Collectors.toList());
    }

    protected abstract boolean support(DeptInfo deptInfo);
    protected abstract Elements selectImportantRows(Document document);
    protected abstract Elements selectNormalRows(Document document);
    protected abstract String[] extractNoticeFromRow(Element row);
}

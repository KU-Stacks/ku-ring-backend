package com.kustacks.kuring.worker.scrap;

import com.kustacks.kuring.common.error.ErrorCode;
import com.kustacks.kuring.common.error.InternalLogicException;
import com.kustacks.kuring.worker.scrap.deptinfo.DeptInfo;
import com.kustacks.kuring.worker.scrap.dto.ScrapingResultDto;
import com.kustacks.kuring.worker.scrap.parser.notice.RowsDto;
import com.kustacks.kuring.worker.update.notice.dto.response.CommonNoticeFormatDto;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
public class DepartmentNoticeScraperTemplate {

    public List<CommonNoticeFormatDto> scrap(DeptInfo deptInfo, Function<DeptInfo, List<ScrapingResultDto>> decisionMaker) throws InternalLogicException {
        List<ScrapingResultDto> requestResults = requestWithDeptInfo(deptInfo, decisionMaker);

        log.info("[{}] HTML 파싱 시작", deptInfo.getDeptName());
        List<CommonNoticeFormatDto> noticeDtoList = htmlParsingFromScrapingResult(deptInfo, requestResults);
        log.info("[{}] HTML 파싱 완료", deptInfo.getDeptName());

        log.info("[{}] 공지 개수 = {}", deptInfo.getDeptName(), noticeDtoList.size());
        if (noticeDtoList.size() == 0) {
            throw new InternalLogicException(ErrorCode.NOTICE_SCRAPER_CANNOT_SCRAP);
        }

        return noticeDtoList;
    }

    private List<ScrapingResultDto> requestWithDeptInfo(DeptInfo deptInfo, Function<DeptInfo, List<ScrapingResultDto>> decisionMaker) {
        long startTime = System.currentTimeMillis();

        log.info("[{}] HTML 요청", deptInfo.getDeptName());
        List<ScrapingResultDto> reqResults = decisionMaker.apply(deptInfo);
        log.info("[{}] HTML 수신", deptInfo.getDeptName());

        long endTime = System.currentTimeMillis();
        log.info("[{}] 파싱에 소요된 초 = {}", deptInfo.getDeptName(), (endTime - startTime) / 1000.0);

        return reqResults;
    }

    private List<CommonNoticeFormatDto> htmlParsingFromScrapingResult(DeptInfo deptInfo, List<ScrapingResultDto> requestResults) {
        List<CommonNoticeFormatDto> noticeDtoList = new LinkedList<>();
        for (ScrapingResultDto reqResult : requestResults) {
            Document document = reqResult.getDocument();
            String viewUrl = reqResult.getUrl();

            RowsDto rowsDto = deptInfo.parse(document);
            noticeDtoList.addAll(rowsDto.buildImportantRowList(viewUrl));
            noticeDtoList.addAll(rowsDto.buildNormalRowList(viewUrl));
        }

        return noticeDtoList;
    }
}

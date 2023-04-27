package com.kustacks.kuring.worker.scrap.client.notice;

import com.kustacks.kuring.common.error.ErrorCode;
import com.kustacks.kuring.common.error.InternalLogicException;
import com.kustacks.kuring.common.utils.converter.DTOConverter;
import com.kustacks.kuring.common.utils.converter.KuisNoticeDTOToCommonFormatDTOConverter;
import com.kustacks.kuring.category.domain.CategoryName;
import com.kustacks.kuring.worker.scrap.client.auth.KuisAuthManager;
import com.kustacks.kuring.worker.update.notice.dto.request.BachelorKuisNoticeRequestBody;
import com.kustacks.kuring.worker.update.notice.dto.request.EmploymentKuisNoticeRequestBody;
import com.kustacks.kuring.worker.update.notice.dto.request.IndustryUnivKuisNoticeRequestBody;
import com.kustacks.kuring.worker.update.notice.dto.request.KuisNoticeRequestBody;
import com.kustacks.kuring.worker.update.notice.dto.request.KuisRequestBody;
import com.kustacks.kuring.worker.update.notice.dto.request.NationalKuisNoticeRequestBody;
import com.kustacks.kuring.worker.update.notice.dto.request.NormalKuisNoticeRequestBody;
import com.kustacks.kuring.worker.update.notice.dto.request.ScholarshipKuisNoticeRequestBody;
import com.kustacks.kuring.worker.update.notice.dto.request.StudentKuisNoticeRequestBody;
import com.kustacks.kuring.worker.update.notice.dto.response.CommonNoticeFormatDto;
import com.kustacks.kuring.worker.update.notice.dto.response.KuisNoticeDTO;
import com.kustacks.kuring.worker.update.notice.dto.response.KuisNoticeResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KuisNoticeApiClient implements NoticeApiClient<CommonNoticeFormatDto, CategoryName> {

    @Value("${notice.referer}")
    private String noticeReferer;

    @Value("${notice.request-url}")
    private String noticeUrl;

    private final RestTemplate restTemplate;
    private final DTOConverter dtoConverter;
    private final KuisAuthManager kuisAuthManager;
    private final Map<CategoryName, KuisNoticeRequestBody> noticeRequestBodies;

    public KuisNoticeApiClient(KuisAuthManager parsingKuisAuthManager,
                               KuisNoticeDTOToCommonFormatDTOConverter dtoConverter,

                               BachelorKuisNoticeRequestBody bachelorNoticeRequestBody,
                               ScholarshipKuisNoticeRequestBody scholarshipNoticeRequestBody,
                               EmploymentKuisNoticeRequestBody employmentKuisNoticeRequestBody,
                               NationalKuisNoticeRequestBody nationalKuisNoticeRequestBody,
                               StudentKuisNoticeRequestBody studentKuisNoticeRequestBody,
                               IndustryUnivKuisNoticeRequestBody industryUnivKuisNoticeRequestBody,
                               NormalKuisNoticeRequestBody normalKuisNoticeRequestBody,

                               RestTemplate restTemplate) {

        this.dtoConverter = dtoConverter;
        this.kuisAuthManager = parsingKuisAuthManager;

        this.restTemplate = restTemplate;

        noticeRequestBodies = new HashMap<>();
        noticeRequestBodies.put(CategoryName.BACHELOR, bachelorNoticeRequestBody);
        noticeRequestBodies.put(CategoryName.SCHOLARSHIP, scholarshipNoticeRequestBody);
        noticeRequestBodies.put(CategoryName.EMPLOYMENT, employmentKuisNoticeRequestBody);
        noticeRequestBodies.put(CategoryName.NATIONAL, nationalKuisNoticeRequestBody);
        noticeRequestBodies.put(CategoryName.STUDENT, studentKuisNoticeRequestBody);
        noticeRequestBodies.put(CategoryName.INDUSTRY_UNIV, industryUnivKuisNoticeRequestBody);
        noticeRequestBodies.put(CategoryName.NORMAL, normalKuisNoticeRequestBody);
    }

    @Override
    @Retryable(value = {InternalLogicException.class})
    public List<CommonNoticeFormatDto> request(CategoryName categoryName) throws InternalLogicException {

        // sessionId 획득
        String sessionId = kuisAuthManager.getSessionId();

        // 공지 요청 헤더
        HttpHeaders noticeRequestHeader = createKuisNoticeRequestHeader(sessionId);

        // 공지 요청
        KuisNoticeRequestBody kuisNoticeRequestBody = noticeRequestBodies.get(categoryName);

        String encodedNoticeRequestBody = KuisRequestBody.toUrlEncodedString(kuisNoticeRequestBody);
        HttpEntity<String> noticeRequestEntity = new HttpEntity<>(encodedNoticeRequestBody, noticeRequestHeader);
        ResponseEntity<KuisNoticeResponseDTO> noticeResponse;
        try {
            noticeResponse = restTemplate.exchange(noticeUrl, HttpMethod.POST, noticeRequestEntity, KuisNoticeResponseDTO.class);
        } catch(RestClientException e) {
            log.warn("세션 갱신이 필요합니다.");
            kuisAuthManager.forceRenewing();
            throw new InternalLogicException(ErrorCode.KU_LOGIN_BAD_RESPONSE, e);
        }

        KuisNoticeResponseDTO body = noticeResponse.getBody();
        if(body == null) {
            kuisAuthManager.forceRenewing();
            throw new InternalLogicException(ErrorCode.KU_NOTICE_CANNOT_PARSE_JSON);
        }

        List<KuisNoticeDTO> kuisNoticeDTOList = body.getKuisNoticeDTOList();
        if(kuisNoticeDTOList == null) {
            kuisAuthManager.forceRenewing();
            throw new InternalLogicException(ErrorCode.KU_NOTICE_CANNOT_PARSE_JSON);
        } else {
            return convertToCommonFormatDTO(kuisNoticeDTOList);
        }
    }

    @Override
    public List<CommonNoticeFormatDto> requestAll(CategoryName categoryName) throws InternalLogicException {
        return Collections.emptyList();
    }

    private HttpHeaders createKuisNoticeRequestHeader(String sessionId) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Referer", noticeReferer);
        httpHeaders.add("Cookie", sessionId);
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return httpHeaders;
    }

    private List<CommonNoticeFormatDto> convertToCommonFormatDTO(List<KuisNoticeDTO> kuisNoticeDTOList) {

        List<CommonNoticeFormatDto> converted = new LinkedList<>();
        for (KuisNoticeDTO kuisNoticeDTO : kuisNoticeDTOList) {
            converted.add((CommonNoticeFormatDto) dtoConverter.convert(kuisNoticeDTO));
        }

        return converted;
    }
}

package com.kustacks.kuring.worker.client.notice;

import com.kustacks.kuring.common.error.ErrorCode;
import com.kustacks.kuring.common.error.InternalLogicException;
import com.kustacks.kuring.common.utils.converter.KuisNoticeDtoToCommonFormatDtoConverter;
import com.kustacks.kuring.config.JsonConfig;
import com.kustacks.kuring.worker.scrap.client.auth.ParsingKuisAuthManager;
import com.kustacks.kuring.worker.scrap.client.notice.KuisNoticeApiClient;
import com.kustacks.kuring.worker.scrap.client.notice.property.KuisNoticeProperties;
import com.kustacks.kuring.worker.update.notice.dto.request.BachelorKuisNoticeInfo;
import com.kustacks.kuring.worker.update.notice.dto.request.EmploymentKuisNoticeInfo;
import com.kustacks.kuring.worker.update.notice.dto.request.IndustryUnivKuisNoticeInfo;
import com.kustacks.kuring.worker.update.notice.dto.request.NationalKuisNoticeInfo;
import com.kustacks.kuring.worker.update.notice.dto.request.NormalKuisNoticeInfo;
import com.kustacks.kuring.worker.update.notice.dto.request.ScholarshipKuisNoticeInfo;
import com.kustacks.kuring.worker.update.notice.dto.request.StudentKuisNoticeInfo;
import com.kustacks.kuring.worker.update.notice.dto.response.CommonNoticeFormatDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

@SpringJUnitConfig({
        KuisNoticeApiClient.class, RestTemplate.class, KuisNoticeDtoToCommonFormatDtoConverter.class,
        BachelorKuisNoticeInfo.class,
        ScholarshipKuisNoticeInfo.class,
        EmploymentKuisNoticeInfo.class,
        NationalKuisNoticeInfo.class,
        StudentKuisNoticeInfo.class,
        IndustryUnivKuisNoticeInfo.class,
        NormalKuisNoticeInfo.class,
        JsonConfig.class})
@EnableConfigurationProperties(value = KuisNoticeProperties.class)
@TestPropertySource("classpath:test-constants.properties")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class KuisNoticeApiClientTest {

    private final KuisNoticeProperties kuisNoticeProperties;

    @InjectMocks
    private final KuisNoticeApiClient kuisNoticeAPIClient;
    private final RestTemplate restTemplate;

    @MockBean
    private ParsingKuisAuthManager kuisAuthManager;
    private MockRestServiceServer server;
    private String testSession;

    public KuisNoticeApiClientTest(KuisNoticeApiClient kuisNoticeAPIClient, RestTemplate restTemplate, KuisNoticeProperties kuisNoticeProperties) {
        this.kuisNoticeAPIClient = kuisNoticeAPIClient;
        this.restTemplate = restTemplate;
        this.kuisNoticeProperties = kuisNoticeProperties;
    }

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.createServer(restTemplate);
        testSession = "JSESSIONID=SESSION_ID";
    }

    @Test
    @DisplayName("성공")
    void success() {

        // given
        String notice1ArticleId = "5b49a79";
        String notice1PostedDate = "20211220";
        String notice1Subject = "2022학년도 다,부,연계,연합,융합전공 신청(포기) 안내 (2021. 12. 20. 수정)";

        String notice2ArticleId = "5b49a74";
        String notice2PostedDate = "20211219";
        String notice2Subject = "2022학년도 전과 신청 안내 (2021. 12. 20. 수정)";

        List<CommonNoticeFormatDto> expectedNotices = new ArrayList<>(2);
        CommonNoticeFormatDto notice1 = CommonNoticeFormatDto.builder()
                .articleId(notice1ArticleId)
                .postedDate(notice1PostedDate)
                .subject(notice1Subject)
                .build();
        CommonNoticeFormatDto notice2 = CommonNoticeFormatDto.builder()
                .articleId(notice2ArticleId)
                .postedDate(notice2PostedDate)
                .subject(notice2Subject)
                .build();
        expectedNotices.add(notice1);
        expectedNotices.add(notice2);

        String expectedResponseBody = "{\"DS_LIST\": [" +
                "{\"POSTED_DT\":\"" + notice1PostedDate + "\",\"SUBJECT\":\"" + notice1Subject + "\",\"ARTICLE_ID\":\"" + notice1ArticleId + "\"}," +
                "{\"POSTED_DT\":\"" + notice2PostedDate + "\",\"SUBJECT\":\"" + notice2Subject + "\",\"ARTICLE_ID\":\"" + notice2ArticleId + "\"}" +
                "]}";

        given(kuisAuthManager.getSessionId()).willReturn(testSession);
        server.expect(requestTo(kuisNoticeProperties.getRequestUrl())).andRespond(withSuccess().contentType(MediaType.APPLICATION_JSON).body(expectedResponseBody));

        // when
        List<CommonNoticeFormatDto> notices = new BachelorKuisNoticeInfo(kuisNoticeAPIClient).scrapLatestPageHtml();

        // then
        for(int i=0; i<2 ;++i) {
            assertEquals(expectedNotices.get(i).getArticleId(), notices.get(i).getArticleId());
            assertEquals(expectedNotices.get(i).getPostedDate(), notices.get(i).getPostedDate());
            assertEquals(expectedNotices.get(i).getSubject(), notices.get(i).getSubject());
        }
    }


    @Test
    @DisplayName("실패 - 응답 오류")
    void failByBadResponse() {

        // given
        given(kuisAuthManager.getSessionId()).willReturn(testSession);
        server.expect(requestTo(kuisNoticeProperties.getRequestUrl())).andRespond(withUnauthorizedRequest());
        BachelorKuisNoticeInfo bachelorKuisNoticeInfo = new BachelorKuisNoticeInfo(kuisNoticeAPIClient);

        // when, then
        InternalLogicException e = assertThrows(InternalLogicException.class, bachelorKuisNoticeInfo::scrapLatestPageHtml);
        assertEquals(ErrorCode.KU_LOGIN_BAD_RESPONSE, e.getErrorCode());
    }

    @Test
    @DisplayName("실패 - 응답 body가 없거나 JSON 파싱이 잘못됨")
    void failByNoResponseBody() {

        // given
        given(kuisAuthManager.getSessionId()).willReturn(testSession);
        server.expect(requestTo(kuisNoticeProperties.getRequestUrl())).andRespond(withSuccess().contentType(MediaType.APPLICATION_JSON));
        BachelorKuisNoticeInfo bachelorKuisNoticeInfo = new BachelorKuisNoticeInfo(kuisNoticeAPIClient);

        //when, then
        InternalLogicException e = assertThrows(InternalLogicException.class, bachelorKuisNoticeInfo::scrapLatestPageHtml);
        assertEquals(ErrorCode.KU_NOTICE_CANNOT_PARSE_JSON, e.getErrorCode());
    }

    @Test
    @DisplayName("실패 - 응답 body에 DS_LIST 필드가 없음")
    void failByNoDsListField() {

        // given
        String badResponseBody = "{\"UNKNOWN\": []}";
        given(kuisAuthManager.getSessionId()).willReturn(testSession);
        server.expect(requestTo(kuisNoticeProperties.getRequestUrl())).andRespond(withSuccess().contentType(MediaType.APPLICATION_JSON).body(badResponseBody));
        BachelorKuisNoticeInfo bachelorKuisNoticeInfo = new BachelorKuisNoticeInfo(kuisNoticeAPIClient);

        //when, then
        InternalLogicException e = assertThrows(InternalLogicException.class, bachelorKuisNoticeInfo::scrapLatestPageHtml);
        assertEquals(ErrorCode.KU_NOTICE_CANNOT_PARSE_JSON, e.getErrorCode());
    }

    @DisplayName("실패 - 로그인 세션 획득 3회 실패")
    @Test
    void failAfterRetry() {
        // given
        given(kuisAuthManager.getSessionId()).willThrow(new InternalLogicException(ErrorCode.KU_LOGIN_BAD_RESPONSE, new RestClientException("로그인 세션 획득 실패")));
        BachelorKuisNoticeInfo bachelorKuisNoticeInfo = new BachelorKuisNoticeInfo(kuisNoticeAPIClient);

        // when, then
        InternalLogicException e = assertThrows(InternalLogicException.class, bachelorKuisNoticeInfo::scrapLatestPageHtml);
        assertEquals(ErrorCode.KU_LOGIN_BAD_RESPONSE, e.getErrorCode());
    }
}

package com.kustacks.kuring.controller;

import com.kustacks.kuring.domain.category.Category;
import com.kustacks.kuring.error.APIException;
import com.kustacks.kuring.error.ErrorCode;
import com.kustacks.kuring.service.CategoryServiceImpl;
import com.kustacks.kuring.service.FirebaseService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static com.kustacks.kuring.ApiDocumentUtils.getDocumentRequest;
import static com.kustacks.kuring.ApiDocumentUtils.getDocumentResponse;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {
    // gradle을 기반으로 디렉토리로 자동 구성 하는 역할
    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();

    private MockMvc mockMvc;

    @MockBean
    private CategoryServiceImpl categoryService;

    @MockBean
    private FirebaseService firebaseService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @DisplayName("서버에서 제공하는 공지 카테고리 목록 제공 API - 성공")
    @Test
    public void getSupportedCategoriesSuccessTest() throws Exception {

        List<Category> categories = new LinkedList<>();
        categories.add(new Category("bachelor"));
        categories.add(new Category("employment"));

        List<String> categoryNames = new LinkedList<>();
        categoryNames.add("bachelor");
        categoryNames.add("employment");

        // given
        given(categoryService.getCategories()).willReturn(categories);
        given(categoryService.getCategoryNamesFromCategories(categories)).willReturn(categoryNames);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/notice/categories")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(true))
                .andExpect(jsonPath("resultMsg").value("성공"))
                .andExpect(jsonPath("resultCode").value(200))
                .andExpect(jsonPath("categories", hasSize(2)))
                .andExpect(jsonPath("categories[0]").value(categoryNames.get(0)))
                .andExpect(jsonPath("categories[1]").value(categoryNames.get(1)))
                .andDo(document("category-get-all-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(),
                        responseFields(
                                fieldWithPath("isSuccess").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("resultMsg").type(JsonFieldType.STRING).description("결과 메세지"),
                                fieldWithPath("resultCode").type(JsonFieldType.NUMBER).description("결과 코드"),
                                fieldWithPath("categories").type(JsonFieldType.ARRAY).description("서버에서 지원하는 공지 카테고리 목록")
                        ))
                );
    }

    @DisplayName("특정 회원이 구독한 카테고리 목록 제공 API - 성공")
    @Test
    public void getUserCategoriesSuccessTest() throws Exception {
        String token = "TEST_TOKEN";

        List<Category> categories = new LinkedList<>();
        categories.add(new Category("bachelor"));
        categories.add(new Category("employment"));

        List<String> categoryNames = new LinkedList<>();
        categoryNames.add("bachelor");
        categoryNames.add("employment");

        // given
        given(categoryService.getUserCategories(token)).willReturn(categories);
        given(categoryService.getCategoryNamesFromCategories(categories)).willReturn(categoryNames);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/notice/subscribe")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("id", token));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(true))
                .andExpect(jsonPath("resultMsg").value("성공"))
                .andExpect(jsonPath("resultCode").value(200))
                .andExpect(jsonPath("categories", hasSize(2)))
                .andExpect(jsonPath("categories[0]").value(categoryNames.get(0)))
                .andExpect(jsonPath("categories[1]").value(categoryNames.get(1)))
                .andDo(document("category-get-user-categories-success",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("id").description("유효한 FCM 토큰")
                                        .attributes(key("Constraints").value(""))
                        ),
                        responseFields(
                                fieldWithPath("isSuccess").type(JsonFieldType.BOOLEAN).description("성공 여부"),
                                fieldWithPath("resultMsg").type(JsonFieldType.STRING).description("결과 메세지"),
                                fieldWithPath("resultCode").type(JsonFieldType.NUMBER).description("결과 코드"),
                                fieldWithPath("categories").type(JsonFieldType.ARRAY).description("해당 회원이 구독한 카테고리 목록")
                        ))
                );
    }

    @DisplayName("특정 회원이 구독한 카테고리 목록 제공 API - 실패 - 유효하지 않은 FCM 토큰")
    @Test
    public void getUserCategoriesFailByInvalidTokenTest() throws Exception {
        String token = "INVALID_TOKEN";

        // given
        doThrow(new APIException(ErrorCode.API_FB_INVALID_TOKEN)).when(firebaseService).verifyToken(token);

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/notice/subscribe")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("id", token));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(false))
                .andExpect(jsonPath("resultMsg").value(ErrorCode.API_FB_INVALID_TOKEN.getMessage()))
                .andExpect(jsonPath("resultCode").value(ErrorCode.API_FB_INVALID_TOKEN.getHttpStatus().value()))
                .andExpect(jsonPath("categories").doesNotExist())
                .andDo(document("category-get-user-categories-fail-invalid-token",
                        getDocumentRequest(),
                        getDocumentResponse()
                ));
    }

    @DisplayName("특정 회원이 구독한 카테고리 목록 제공 API - 실패 - 필수 파라미터 누락")
    @Test
    public void getUserCategoriesFailByMissingParamTest() throws Exception {

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/notice/subscribe")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("isSuccess").value(false))
                .andExpect(jsonPath("resultMsg").value(ErrorCode.API_MISSING_PARAM.getMessage()))
                .andExpect(jsonPath("resultCode").value(ErrorCode.API_MISSING_PARAM.getHttpStatus().value()))
                .andExpect(jsonPath("categories").doesNotExist())
                .andDo(document("category-get-user-categories-fail-missing-param",
                        getDocumentRequest(),
                        getDocumentResponse()
                ));
    }
}
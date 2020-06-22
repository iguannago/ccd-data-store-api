package uk.gov.hmcts.ccd.endpoint.std;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.ccd.ElasticsearchBaseTest;
import uk.gov.hmcts.ccd.MockUtils;
import uk.gov.hmcts.ccd.data.casedetails.SecurityClassification;
import uk.gov.hmcts.ccd.domain.model.search.CaseSearchResult;
import uk.gov.hmcts.ccd.test.ElasticsearchTestHelper;

import javax.inject.Inject;

import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.ccd.test.ElasticsearchTestHelper.*;

class CaseSearchEndpointESSecurityIT extends ElasticsearchBaseTest {

    private static final String POST_SEARCH_CASES = "/searchCases";
    private static final String SECURITY_CASE_2 = "1589460125872336";

    @Inject
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Nested
    class CrudTest {

        // AuthorisationCaseField

        @Test
        void shouldReturnCaseFieldsWithCrud() throws Exception {
            ElasticsearchTestRequest searchRequest = caseReferenceRequest(SECURITY_CASE_2);

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_PUBLIC);

            Map<String, JsonNode> data = getFirstCaseData(caseSearchResult);
            assertAll(
                () -> assertThat(data.containsKey(EMAIL_FIELD), is(true))
            );
        }

        @Test
        void shouldReturnCaseFieldsWithCrudWithoutRead() throws Exception {
            ElasticsearchTestRequest searchRequest = caseReferenceRequest(SECURITY_CASE_2);

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_PUBLIC);

            Map<String, JsonNode> data = getFirstCaseData(caseSearchResult);
            assertAll(
                () -> assertThat(data.containsKey(YES_OR_NO_FIELD), is(false))
            );
        }

        @Test
        void shouldNotReturnCaseFieldsWithoutCrud() throws Exception {
            ElasticsearchTestRequest searchRequest = caseReferenceRequest(SECURITY_CASE_2);

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_PRIVATE);

            Map<String, JsonNode> data = getFirstCaseData(caseSearchResult);
            assertAll(
                () -> assertThat(caseSearchResult.getTotal(), is(1L)),
                () -> assertThat(data.containsKey(YES_OR_NO_FIELD), is(false))
            );
        }

        // AuthorisationCaseState

        @Test
        void shouldReturnCasesWithStateCrud() throws Exception {
            ElasticsearchTestRequest searchRequest = ElasticsearchTestRequest.builder()
                .query(matchQuery(STATE, IN_PROGRESS_STATE))
                .build();

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_RESTRICTED);

            assertAll(
                () -> assertThat(caseSearchResult.getTotal(), is(1L)),
                () -> assertThat(caseSearchResult.getCases().get(0).getReference(), is(1589460099608690L))
            );
        }

        @Test
        void shouldNotReturnCasesWithStateCrudWithoutRead() throws Exception {
            ElasticsearchTestRequest searchRequest = ElasticsearchTestRequest.builder()
                .query(matchQuery(STATE, IN_PROGRESS_STATE))
                .build();

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_PUBLIC);

            assertAll(
                () -> assertThat(caseSearchResult.getTotal(), is(0L))
            );
        }

        @Test
        void shouldNotReturnCasesWithoutStateCrud() throws Exception {
            ElasticsearchTestRequest searchRequest = ElasticsearchTestRequest.builder()
                .query(matchQuery(STATE, IN_PROGRESS_STATE))
                .build();

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_PRIVATE);

            assertAll(
                () -> assertThat(caseSearchResult.getTotal(), is(0L))
            );
        }

        // AuthorisationCaseType

        @Test
        void shouldReturnCaseTypesWithCrud() throws Exception {
            ElasticsearchTestRequest searchRequest = matchAllRequest();

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_PUBLIC);

            assertAll(
                () -> assertThat(caseSearchResult.getTotal() > 0L, is(true))
            );
        }

        @Test
        void shouldNotReturnCaseTypesWithCrudWithoutRead() throws Exception {
            ElasticsearchTestRequest searchRequest = ElasticsearchTestRequest.builder()
                .query(matchAllQuery())
                .build();

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_A, AUTOTEST1_PRIVATE);

            assertAll(
                () -> assertThat(caseSearchResult.getTotal(), is(0L))
            );
        }

        @Test
        void shouldNotReturnCaseTypesWithoutCrud() throws Exception {
            ElasticsearchTestRequest searchRequest = ElasticsearchTestRequest.builder()
                .query(matchAllQuery())
                .build();

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_A, AUTOTEST1_RESTRICTED);

            assertAll(
                () -> assertThat(caseSearchResult.getTotal(), is(0L))
            );
        }

        // AuthorisationComplexType

        @Test
        void shouldReturnComplexNestedFieldsWithCrud() throws Exception {
            ElasticsearchTestRequest searchRequest = caseReferenceRequest(SECURITY_CASE_2);

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_PUBLIC);

            assertAll(
                () -> assertThat(getFirstCaseData(caseSearchResult).get(COMPLEX_FIELD).has(COMPLEX_TEXT_FIELD), is(true))
            );
        }

        @Test
        void shouldNotReturnComplexNestedFieldsWithCrudWithoutRead() throws Exception {
            ElasticsearchTestRequest searchRequest = caseReferenceRequest(SECURITY_CASE_2);

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_RESTRICTED);

            assertAll(
                () -> assertThat(getFirstCaseData(caseSearchResult).get(COMPLEX_FIELD).has(COMPLEX_TEXT_FIELD), is(false))
            );
        }

        @Test
        void shouldReturnComplexNestedFieldsWithoutCrud() throws Exception {
            ElasticsearchTestRequest searchRequest = caseReferenceRequest(SECURITY_CASE_2);

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_PRIVATE);

            assertAll(
                () -> assertThat(getFirstCaseData(caseSearchResult).get(COMPLEX_FIELD).has(COMPLEX_TEXT_FIELD), is(true))
            );
        }
    }

    @Nested
    class SecurityClassificationTest {

        // Case

        @Test
        void shouldReturnCasesWithLowerCaseSC() throws Exception {
            ElasticsearchTestRequest searchRequest = ElasticsearchTestRequest.builder()
                .query(matchQuery(STATE, STATE_VALUE))
                .sort(CREATED_DATE)
                .build();

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_RESTRICTED);

            assertAll(
                () -> assertThat(caseSearchResult.getTotal(), is(2L)),
                () -> assertThat(caseSearchResult.getCases().get(0).getSecurityClassification(), is(SecurityClassification.PRIVATE)),
                () -> assertThat(caseSearchResult.getCases().get(1).getSecurityClassification(), is(SecurityClassification.PUBLIC))
            );
        }

        @Test
        void shouldNotReturnCasesWithHigherCaseSC() throws Exception {
            ElasticsearchTestRequest searchRequest = matchAllRequest();

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_PUBLIC);

            assertAll(
                () -> assertThat(caseSearchResult.getTotal(), is(1L)),
                () -> assertThat(caseSearchResult.getCases().get(0).getSecurityClassification(), is(SecurityClassification.PUBLIC))
            );
        }

        // CaseField

        @Test
        void shouldReturnCaseFieldsWithLowerSC() throws Exception {
            ElasticsearchTestRequest searchRequest = caseReferenceRequest(SECURITY_CASE_2);

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_RESTRICTED);

            Map<String, JsonNode> data = getFirstCaseData(caseSearchResult);
            assertAll(
                () -> assertThat(data.containsKey(MULTI_SELECT_LIST_FIELD), is(true)), // RESTRICTED
                () -> assertThat(data.containsKey(PHONE_FIELD), is(true)), // PRIVATE
                () -> assertThat(data.containsKey(DATE_FIELD), is(true)) // PUBLIC
            );
        }

        @Test
        void shouldNotReturnCaseFieldsWithHigherSC() throws Exception {
            ElasticsearchTestRequest searchRequest = caseReferenceRequest(SECURITY_CASE_2);

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_PUBLIC);

            Map<String, JsonNode> data = getFirstCaseData(caseSearchResult);
            assertAll(
                () -> assertThat(data.containsKey(MULTI_SELECT_LIST_FIELD), is(false)), // RESTRICTED
                () -> assertThat(data.containsKey(PHONE_FIELD), is(false)), // PRIVATE
                () -> assertThat(data.containsKey(COLLECTION_FIELD), is(true)) // PUBLIC
            );
        }

        // CaseType

        @Test
        void shouldReturnCasesWithLowerCaseTypeSC() throws Exception {
            ElasticsearchTestRequest searchRequest = matchAllRequest();

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, caseTypesParam(CASE_TYPE_C, CASE_TYPE_D), AUTOTEST1_RESTRICTED);

            assertAll(
                () -> assertThat(caseSearchResult.getCases().get(0).getCaseTypeId(), is(CASE_TYPE_C)), // PUBLIC
                () -> assertThat(caseSearchResult.getCases().get(3).getCaseTypeId(), is(CASE_TYPE_D)) // RESTRICTED
            );
        }

        @Test
        void shouldNotReturnCasesWithHigherCaseTypeSC() throws Exception {
            ElasticsearchTestRequest searchRequest = matchAllRequest();

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, caseTypesParam(CASE_TYPE_C, CASE_TYPE_D), AUTOTEST1_PUBLIC);

            assertAll(
                () -> assertThat(caseSearchResult.getTotal(), is(1L)),
                () -> assertThat(caseSearchResult.getCases().get(0).getCaseTypeId(), is(CASE_TYPE_C)) // PUBLIC
            );
        }

        // ComplexTypes

        @Test
        void shouldReturnComplexNestedFieldsWithLowerSC() throws Exception {
            ElasticsearchTestRequest searchRequest = caseReferenceRequest(SECURITY_CASE_2);

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_RESTRICTED);

            Map<String, JsonNode> data = getFirstCaseData(caseSearchResult);
            assertAll(
                () -> assertThat(data.get(COMPLEX_FIELD).get(COMPLEX_NESTED_FIELD).has(NESTED_COLLECTION_TEXT_FIELD), is(true)), // RESTRICTED
                () -> assertThat(data.get(ADDRESS_FIELD).has(POST_CODE_FIELD), is(true)), // PRIVATE
                () -> assertThat(data.get(ADDRESS_FIELD).has(ADDRESS_LINE_1), is(true)) // PUBLIC
            );
        }

        @Test
        void shouldNotReturnComplexNestedFieldsWithHigherSC() throws Exception {
            ElasticsearchTestRequest searchRequest = caseReferenceRequest(SECURITY_CASE_2);

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_PUBLIC);

            Map<String, JsonNode> data = getFirstCaseData(caseSearchResult);
            assertAll(
                () -> assertThat(data.get(COMPLEX_FIELD).get(COMPLEX_NESTED_FIELD).has(NESTED_COLLECTION_TEXT_FIELD), is(false)), // RESTRICTED
                () -> assertThat(data.get(COMPLEX_FIELD).has(POST_CODE_FIELD), is(false)), // PRIVATE
                () -> assertThat(data.get(COMPLEX_FIELD).has(COMPLEX_TEXT_FIELD), is(true)) // PUBLIC
            );
        }
    }

    @Nested
    class GeneralAccessTest {

        @Test
        void shouldOnlyReturnCasesFromCaseTypesWithJurisdictionRole() throws Exception {
            ElasticsearchTestRequest searchRequest = matchAllRequest();

            CaseSearchResult caseSearchResult = executeRequest(searchRequest, caseTypesParam(CASE_TYPE_A, CASE_TYPE_B, CASE_TYPE_C), AUTOTEST2_PUBLIC);

            assertAll(
                () -> assertThat(caseSearchResult.getTotal(), is(1L)),
                () -> assertThat(caseSearchResult.getCases().get(0).getCaseTypeId(), is(CASE_TYPE_B))
            );
        }
    }

    // The following tests require the Spring @SQL annotation, which does not work in @Nested classes (see SPR-15366)

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_elasticsearch_cases.sql"})
    void shouldOnlyReturnCasesSolicitorHasBeenGrantedAccessTo() throws Exception {
        ElasticsearchTestRequest searchRequest = matchAllRequest();

        CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_SOLICITOR);

        assertAll(
            () -> assertThat(caseSearchResult.getTotal(), is(1L)),
            () -> assertThat(caseSearchResult.getCases().get(0).getReference(), is(1589460125872336L))
        );
    }

    @Test
    @Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"classpath:sql/insert_elasticsearch_cases.sql"})
    void shouldReturnAllCasesForCaseworker() throws Exception {
        ElasticsearchTestRequest searchRequest = matchAllRequest();

        CaseSearchResult caseSearchResult = executeRequest(searchRequest, CASE_TYPE_C, AUTOTEST1_RESTRICTED);

        assertAll(
            () -> assertThat(caseSearchResult.getTotal(), is(3L))
        );
    }

    private CaseSearchResult executeRequest(ElasticsearchTestRequest searchRequest, String caseTypeParam, String... roles) throws Exception {
        MockUtils.setSecurityAuthorities(authentication, roles);
        MockHttpServletRequestBuilder postRequest = createPostRequest(POST_SEARCH_CASES, searchRequest, caseTypeParam, null);

        return ElasticsearchTestHelper.executeRequest(postRequest, 200, mapper, mockMvc, CaseSearchResult.class);
    }

    private Map<String, JsonNode> getFirstCaseData(CaseSearchResult caseSearchResult) {
        return caseSearchResult.getCases().get(0).getData();
    }
}

package uk.gov.hmcts.ccd.domain.service.callbacks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.data.SecurityUtils;
import uk.gov.hmcts.ccd.domain.model.callbacks.CallbackResponse;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseEvent;
import uk.gov.hmcts.ccd.endpoint.exceptions.CallbackException;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ccd.domain.service.common.TestBuildersUtil.CallbackResponseBuilder.aCallbackResponse;
import static uk.gov.hmcts.ccd.domain.service.common.TestBuildersUtil.CaseDetailsBuilder.newCaseDetails;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties =
    {
        "http.client.read.timeout=500"
    })
@AutoConfigureWireMock(port = 0)
@DirtiesContext
public class CallbackServiceWireMockTest {
    private static final JsonNodeFactory JSON_NODE_FACTORY = new JsonNodeFactory(false);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Inject
    private CallbackService callbackService;

    @Value("${wiremock.server.port}")
    protected Integer wiremockPort;

    private CallbackResponse callbackResponse;
    private CaseDetails caseDetails = new CaseDetails();
    private final CaseEvent caseEvent = new CaseEvent();
    private String testUrl;

    @Before
    public void setUp() throws Exception {
        // IDAM
        callbackResponse = aCallbackResponse().build();
        caseDetails = newCaseDetails().build();
        final SecurityUtils securityUtils = Mockito.mock(SecurityUtils.class);
        Mockito.when(securityUtils.authorizationHeaders()).thenReturn(new HttpHeaders());
        ReflectionTestUtils.setField(callbackService, "securityUtils", securityUtils);
        testUrl = "http://localhost:" + wiremockPort + "/test-callbackGrrrr";
        WireMock.resetAllScenarios();
        WireMock.resetAllRequests();
    }

    @Test
    public void shouldRetryOnErrorWithIgnoreWarningFalseAndDefaultRetryContext() throws Exception {

        stubFor(post(urlMatching("/test-callbackGrrrr.*"))
            .inScenario("CallbackRetry")
            .willReturn(okJson(mapper.writeValueAsString(callbackResponse)).withStatus(500).withFixedDelay(501))
            .willSetStateTo("FirstFailedAttempt"));
        stubFor(post(urlMatching("/test-callbackGrrrr.*"))
            .inScenario("CallbackRetry")
            .whenScenarioStateIs("FirstFailedAttempt")
            .willReturn(okJson(mapper.writeValueAsString(callbackResponse)).withStatus(500).withFixedDelay(501))
            .willSetStateTo("SecondFailedAttempt"));
        stubFor(post(urlMatching("/test-callbackGrrrr.*"))
            .inScenario("CallbackRetry")
            .whenScenarioStateIs("SecondFailedAttempt")
            .willReturn(okJson(mapper.writeValueAsString(callbackResponse)).withStatus(200).withFixedDelay(490)));

        Instant start = Instant.now();
        callbackService.send(testUrl, null, caseEvent, null, caseDetails, CallbackResponse.class, false);

        final Duration between = Duration.between(start, Instant.now());
        // 0s retryInterval + 0.5s readTimeout + 1s retryInterval + 0.5s readTimeout + 3s retryInterval + 0.49s readTimeout
        assertThat((int) between.toMillis(), greaterThan(5500));
        verify(exactly(3), postRequestedFor(urlMatching("/test-callbackGrrrr.*")));
    }

    @Test
    public void shouldNotRetryWhenCallbackRetriesDisabled() throws Exception {

        stubFor(post(urlMatching("/test-callbackGrrrr.*"))
            .inScenario("CallbackRetry")
            .willReturn(okJson(mapper.writeValueAsString(callbackResponse)).withStatus(500).withFixedDelay(501))
            .willSetStateTo("FirstFailedAttempt"));

        List<Integer> disabledRetries = Lists.newArrayList(0);
        Instant start = Instant.now();

        CallbackException callbackException = assertThrows(CallbackException.class, () -> callbackService.send(testUrl, disabledRetries, caseEvent, null, caseDetails, CallbackResponse.class, false));
        Assert.assertThat(callbackException.getMessage(), is("Unsuccessful callback to url=http://localhost:" + wiremockPort + "/test-callbackGrrrr"));
        final Duration between = Duration.between(start, Instant.now());
        // 0s retryInterval + 0.5s readTimeout and no follow up retries
        assertThat((int) between.toMillis(), lessThan(1500));
        verify(exactly(1), postRequestedFor(urlMatching("/test-callbackGrrrr.*")));
    }

}

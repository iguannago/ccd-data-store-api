package uk.gov.hmcts.ccd.domain.service.stdapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ccd.data.SecurityUtils;
import uk.gov.hmcts.ccd.data.casedetails.CachedCaseDetailsRepository;
import uk.gov.hmcts.ccd.data.casedetails.CaseDetailsRepository;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseType;
import uk.gov.hmcts.ccd.domain.model.definition.Document;
import uk.gov.hmcts.ccd.domain.service.common.CaseTypeService;
import uk.gov.hmcts.ccd.domain.service.common.UIDService;
import uk.gov.hmcts.ccd.endpoint.exceptions.BadRequestException;
import uk.gov.hmcts.ccd.endpoint.exceptions.ServiceException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

@Named
@Singleton
public class DocumentsOperation {
    private static final Logger LOG = LogManager.getLogger(DocumentsOperation.class);

    private final SecurityUtils securityUtils;
    private final CaseDetailsRepository caseDetailsRepository;
    private final CaseTypeService caseTypeService;
    private final UIDService uidService;

    @Inject
    public DocumentsOperation(final SecurityUtils securityUtils,
                              final CaseTypeService caseTypeService,
                              @Qualifier(CachedCaseDetailsRepository.QUALIFIER) final CaseDetailsRepository caseDetailsRepository,
                              final UIDService uidService) {
        this.securityUtils = securityUtils;
        this.caseDetailsRepository = caseDetailsRepository;
        this.caseTypeService = caseTypeService;
        this.uidService = uidService;
    }

    public List<Document> getPrintableDocumentsForCase(final String jurisdictionId,
                                                       final String caseTypeId,
                                                       final String caseReference) {
        if (!uidService.validateUID(caseReference)) {
            throw new BadRequestException("Invalid Case Reference");
        }

        try {
            final CaseType caseType = caseTypeService.getCaseTypeForJurisdiction(caseTypeId, jurisdictionId);
            final CaseDetails caseDetails = caseDetailsRepository.findByReference(Long.valueOf(caseReference));
            final String documentListUrl = caseType.getPrintableDocumentsUrl();
            final RestTemplate restTemplate = new RestTemplate();
            final HttpHeaders headers = securityUtils.authorizationHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            final HttpEntity<CaseDetails> requestEntity = new HttpEntity<>(caseDetails, headers);
            final Document[] documents = restTemplate.exchange(documentListUrl, HttpMethod.POST, requestEntity, Document[].class).getBody();

            return Arrays.asList(documents);
        } catch (Exception e) {
            LOG.error(String.format(
                "Cannot get documents for the Jurisdiction:%s, Case Type Id:%s, Case Reference:%s",
                jurisdictionId, caseTypeId, caseReference), e);
            throw new ServiceException(String.format("Cannot get documents for the Jurisdiction:%s, Case Type Id:%s, Case Reference:%s, because of %s",
                jurisdictionId, caseTypeId, caseReference, e));
        }
    }
}


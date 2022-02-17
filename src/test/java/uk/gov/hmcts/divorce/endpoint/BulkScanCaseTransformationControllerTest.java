package uk.gov.hmcts.divorce.endpoint;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.divorce.bulkscan.transformation.BulkScanService;
import uk.gov.hmcts.divorce.endpoint.model.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.CaseCreationDetails;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.SuccessfulTransformationResponse;

import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerCreatePaperCase.CREATE_PAPER_CASE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class BulkScanCaseTransformationControllerTest {
    @Mock
    private BulkScanService bulkScanService;

    @InjectMocks
    private BulkScanCaseTransformationController controller;

    @Test
    void shouldSuccessfullyTransformD8Form() {
        var exceptionRecord = ExceptionRecord.builder().build();
        Map<String, Object> transformedData =
            Map.of(
                "applicationForDivorce", "true",
                "applicationForDissolution", "false",
                "aSoleApplication", "true",
                "aJointApplication", "false",
                "marriageOrCivilPartnershipCertificate", "true",
                "translation", "false"
            );

        when(bulkScanService.transformBulkScanForm(exceptionRecord)).thenReturn(transformedData);

        ResponseEntity<SuccessfulTransformationResponse> response =
            controller.transformExceptionRecordIntoCase(TEST_SERVICE_AUTH_TOKEN, exceptionRecord);

        SuccessfulTransformationResponse successfulTransformationResponse = SuccessfulTransformationResponse.builder()
            .caseCreationDetails(
                new CaseCreationDetails(
                    CASE_TYPE,
                    CREATE_PAPER_CASE,
                    transformedData
                )
            )
            .build();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        CaseCreationDetails caseCreationDetails = Objects.requireNonNull(response.getBody()).getCaseCreationDetails();
        assertThat(caseCreationDetails.getCaseData()).isEqualTo(transformedData);
        assertThat(caseCreationDetails.getCaseTypeId()).isEqualTo(CASE_TYPE);
        assertThat(caseCreationDetails.getEventId()).isEqualTo(CREATE_PAPER_CASE);
    }
}

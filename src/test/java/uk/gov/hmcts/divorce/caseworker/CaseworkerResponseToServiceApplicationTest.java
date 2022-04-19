package uk.gov.hmcts.divorce.caseworker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.event.CaseworkerResponseToServiceApplication;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerResponseToServiceApplication.CASEWORKER_RESPONSE_TO_SERVICE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
class CaseworkerResponseToServiceApplicationTest {

    @InjectMocks
    private CaseworkerResponseToServiceApplication caseworkerResponseToServiceApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerResponseToServiceApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_RESPONSE_TO_SERVICE_APPLICATION);
    }

    @Test
    void shouldMoveStateToAwaitingServiceConsiderationWhenDeemed() {

        CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService
                .builder()
                .alternativeServiceType(DEEMED)
                .build()
            ).build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResponseToServiceApplication.aboutToSubmit(
            updatedCaseDetails,
            CaseDetails.<CaseData, State>builder().build()
        );

        assert(response.getState().equals(State.AwaitingServiceConsideration));
    }

    @Test
    void shouldMoveStateToAwaitingServiceConsiderationWhenDispensed() {

        CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService
                .builder()
                .alternativeServiceType(DISPENSED)
                .build()
            ).build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResponseToServiceApplication.aboutToSubmit(
            updatedCaseDetails,
            CaseDetails.<CaseData, State>builder().build()
        );

        assert(response.getState().equals(State.AwaitingServiceConsideration));
    }

    @Test
    void shouldMoveStateToAwaitingBailiffReferralWhenBailiff() {

        CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService
                .builder()
                .alternativeServiceType(BAILIFF)
                .build()
            ).build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResponseToServiceApplication.aboutToSubmit(
            updatedCaseDetails,
            CaseDetails.<CaseData, State>builder().build()
        );

        assert(response.getState().equals(State.AwaitingBailiffReferral));
    }

    @Test
    void shouldNotMoveStateWhenAlternativeServiceTypeIsNull() {

        CaseData caseData = CaseData.builder()
            .alternativeService(AlternativeService
                .builder()
                .alternativeServiceType(null)
                .build()
            ).build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        updatedCaseDetails.setState(AosOverdue);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerResponseToServiceApplication.aboutToSubmit(
            updatedCaseDetails,
            CaseDetails.<CaseData, State>builder().build()
        );

        assert(response.getState().equals(AosOverdue));
    }
}

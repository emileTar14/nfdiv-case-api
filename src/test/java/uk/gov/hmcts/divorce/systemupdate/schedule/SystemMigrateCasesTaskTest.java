package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RetiredFields;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.NOT_FOUND;
import static org.springframework.cloud.contract.spec.internal.HttpStatus.REQUEST_TIMEOUT;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCase.SYSTEM_MIGRATE_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SystemMigrateCasesTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SystemMigrateCasesTask systemMigrateCasesTask;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldRunBaseAndJointAppMigrations() throws Exception {
        final CaseDetails caseDetails = mock(CaseDetails.class);

        when(ccdSearchService.searchForCasesWithVersionLessThan(RetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(singletonList(caseDetails));

        when(ccdSearchService.searchJointApplicationsWithAccessCodePostIssueApplication(user, SERVICE_AUTHORIZATION))
            .thenReturn(singletonList(caseDetails));

        withEnvironmentVariable("MIGRATE_JOINT_APP_ENABLED", "true")
            .execute(() -> systemMigrateCasesTask.run());

        verify(ccdUpdateService, times(2)).submitEvent(caseDetails, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldNotSubmitEventIfSearchFailsForBaseMigration() {
        when(ccdSearchService.searchForCasesWithVersionLessThan(3, user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemMigrateCasesTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotRemoveAccessCodeWhenSearchFailsForJointAppMigration() throws Exception {
        final CaseDetails caseDetails = mock(CaseDetails.class);

        when(ccdSearchService.searchForCasesWithVersionLessThan(RetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(singletonList(caseDetails));

        when(ccdSearchService.searchJointApplicationsWithAccessCodePostIssueApplication(user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        withEnvironmentVariable("MIGRATE_JOINT_APP_ENABLED", "true")
            .execute(() -> systemMigrateCasesTask.run());

        verify(ccdUpdateService, times(1)).submitEvent(caseDetails, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueProcessingIfThereIsConflictDuringSubmissionForBaseMigration() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForCasesWithVersionLessThan(RetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        systemMigrateCasesTask.run();

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueProcessingIfThereIsConflictDuringSubmissionForJointAppMigration() throws Exception {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);

        when(ccdSearchService.searchForCasesWithVersionLessThan(RetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(singletonList(caseDetails1));

        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseDetails caseDetails3 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails2, caseDetails3);

        when(ccdSearchService.searchJointApplicationsWithAccessCodePostIssueApplication(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        withEnvironmentVariable("MIGRATE_JOINT_APP_ENABLED", "true")
            .execute(() -> systemMigrateCasesTask.run());

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails3, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCaseForBaseMigration() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForCasesWithVersionLessThan(RetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        systemMigrateCasesTask.run();

        verify(ccdUpdateService, times(2)).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCaseForJointAppMigration() throws Exception {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);

        when(ccdSearchService.searchForCasesWithVersionLessThan(RetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(singletonList(caseDetails1));

        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final CaseDetails caseDetails3 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails2, caseDetails3);

        when(ccdSearchService.searchJointApplicationsWithAccessCodePostIssueApplication(user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails2, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        withEnvironmentVariable("MIGRATE_JOINT_APP_ENABLED", "true")
            .execute(() -> systemMigrateCasesTask.run());

        verify(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(caseDetails3, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSetDataVersionToZeroIfExceptionIsThrownWhileDeserializingCaseForBaseMigration() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .build();
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForCasesWithVersionLessThan(RetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        when(objectMapper.convertValue(eq(caseDetails1.getData()), eq(CaseData.class)))
            .thenThrow(new IllegalArgumentException("Failed to deserialize"));

        systemMigrateCasesTask.run();

        assertThat(caseDetails1.getData()).isEqualTo(Map.of("dataVersion", 0));
    }

    @Test
    void shouldSetDataVersionToZeroIfExceptionIsThrownWhilstSubmittingCcdUpdateEventForBaseMigration() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForCasesWithVersionLessThan(RetiredFields.getVersion(), user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(REQUEST_TIMEOUT, "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        systemMigrateCasesTask.run();

        assertThat(caseDetails1.getData()).isEqualTo(Map.of("dataVersion", 0));
    }

    @Test
    void shouldNotSetDataVersionToZeroIfExceptionIsThrownWhilstSubmittingCcdUpdateEventAndStatusIsNotFoundForBaseMigration() {
        final CaseDetails caseDetails1 =
            CaseDetails.builder()
                .data(new HashMap<>())
                .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);
        final int latestVersion = RetiredFields.getVersion();

        when(ccdSearchService.searchForCasesWithVersionLessThan(latestVersion, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsList);

        doThrow(new CcdManagementException(NOT_FOUND, "Failed processing of case", mock(FeignException.class)))
            .doNothing()
            .when(ccdUpdateService).submitEvent(caseDetails1, SYSTEM_MIGRATE_CASE, user, SERVICE_AUTHORIZATION);

        systemMigrateCasesTask.run();

        assertThat(caseDetails1.getData()).isEqualTo(Map.of("dataVersion", latestVersion));
    }
}

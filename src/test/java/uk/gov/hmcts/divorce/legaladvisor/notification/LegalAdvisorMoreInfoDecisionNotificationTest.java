package uk.gov.hmcts.divorce.legaladvisor.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.legaladvisor.service.printer.ConditionalOrderRefusedPrinter;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.CITIZEN_CONDITIONAL_ORDER_REFUSED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLICITOR_CO_REFUSED_SOLE_JOINT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConditionalOrderTemplateVars;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant2CaseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorMoreInfoDecisionNotificationTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private CommonContent commonContent;

    @Mock
    private ConditionalOrderRefusedPrinter conditionalOrderRefusedPrinter;

    @InjectMocks
    private LegalAdvisorMoreInfoDecisionNotification notification;

    @Test
    void shouldSendConditionalOrderRefusedEmailToApplicant1IfNotRepresented() {

        final var data = validApplicant1CaseData();

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(SOLE_APPLICATION));

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.NO)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendConditionalOrderRefusedEmailToApplicant1IfJoint() {

        final var data = validJointApplicant1CaseData();

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2()))
            .thenReturn(getConditionalOrderTemplateVars(JOINT_APPLICATION));

        notification.sendToApplicant1(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), data.getApplicant2());
    }

    @Test
    void shouldSendConditionalOrderRefusedEmailToApplicant2IfJoint() {

        final var data = validJointApplicant1CaseData();

        when(commonContent.conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1()))
            .thenReturn(getConditionalOrderTemplateVars(JOINT_APPLICATION));

        notification.sendToApplicant2(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL),
            eq(CITIZEN_CONDITIONAL_ORDER_REFUSED),
            argThat(allOf(
                hasEntry(APPLICATION_REFERENCE, formatId(TEST_CASE_ID)),
                hasEntry(IS_DIVORCE, CommonContent.YES),
                hasEntry(IS_DISSOLUTION, CommonContent.NO),
                hasEntry(JOINT_CONDITIONAL_ORDER, CommonContent.YES)
            )),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).conditionalOrderTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), data.getApplicant1());
    }

    @Test
    void shouldNotSendConditionalOrderRefusedEmailToApplicant2IfSole() {

        final var data = validApplicant2CaseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.getApplicant2().setEmail(TEST_APPLICANT_2_USER_EMAIL);

        notification.sendToApplicant2(data, TEST_CASE_ID);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendConditionalOrderRefusedEmailToApplicant1Solicitor() {

        final var data = validApplicant1CaseData();
        data.setConditionalOrder(ConditionalOrder.builder()
            .refusalDecision(MORE_INFO)
            .build());
        data.getApplicant1().setSolicitor(Solicitor.builder()
            .name("applicant solicitor")
            .reference("sol1")
            .email("sol1@gm.com")
            .build());

        notification.sendToApplicant1Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq("sol1@gm.com"),
            eq(SOLICITOR_CO_REFUSED_SOLE_JOINT),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );

        verify(commonContent).getCoRefusedSolicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant1(), MORE_INFO);
    }

    @Test
    void shouldSendConditionalOrderRefusedEmailToApplicant2Solicitor() {

        final var data = validApplicant2CaseData();
        data.setApplicationType(JOINT_APPLICATION);
        data.setConditionalOrder(ConditionalOrder.builder()
            .refusalDecision(MORE_INFO)
            .build());
        data.getApplicant2().setSolicitor(Solicitor.builder()
            .name("applicant2 solicitor")
            .reference("sol2")
            .email("sol2@gm.com")
            .build());

        notification.sendToApplicant2Solicitor(data, TEST_CASE_ID);

        verify(notificationService).sendEmail(
            eq("sol2@gm.com"),
            eq(SOLICITOR_CO_REFUSED_SOLE_JOINT),
            anyMap(),
            eq(ENGLISH),
            eq(TEST_CASE_ID)
        );
        verify(commonContent).getCoRefusedSolicitorTemplateVars(data, TEST_CASE_ID, data.getApplicant2(), MORE_INFO);
    }

    @Test
    void shouldSendConditionalOrderClarificationLettersToApplicant1IfOffline() {
        final var caseData = validApplicant1CaseData();

        notification.sendToApplicant1Offline(caseData, TEST_CASE_ID);

        verify(conditionalOrderRefusedPrinter).sendLetters(caseData, TEST_CASE_ID, caseData.getApplicant1());
    }

    @Test
    void shouldSendConditionalOrderClarificationLettersToApplicant2IfOfflineAndJointApplication() {
        final var caseData = validJointApplicant1CaseData();

        notification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verify(conditionalOrderRefusedPrinter).sendLetters(caseData, TEST_CASE_ID, caseData.getApplicant2());
    }

    @Test
    void shouldNotSendConditionalOrderClarificationLettersToApplicant2IfOfflineAndSoleApplication() {
        final var caseData = validApplicant1CaseData();

        notification.sendToApplicant2Offline(caseData, TEST_CASE_ID);

        verifyNoInteractions(conditionalOrderRefusedPrinter);
    }
}

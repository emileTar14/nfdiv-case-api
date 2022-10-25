package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.PartnerNotAppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemNotifyApplicantPartnerNotAppliedForFinalOrder.SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class SystemNotifyApplicantPartnerNotAppliedForFinalOrderTest {

    @Mock
    private PartnerNotAppliedForFinalOrderNotification partnerNotAppliedForFinalOrderNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SystemNotifyApplicantPartnerNotAppliedForFinalOrder systemNotifyApplicantPartnerNotAppliedForFinalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemNotifyApplicantPartnerNotAppliedForFinalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_PARTNER_NOT_APPLIED_FOR_FINAL_ORDER);
    }

    @Test
    void shouldSetReminderNotificationSentToYes() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(1L);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemNotifyApplicantPartnerNotAppliedForFinalOrder.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(partnerNotAppliedForFinalOrderNotification, caseData, details.getId());
        assertThat(response.getData().getFinalOrder().getFinalOrderFirstInTimeNotifiedOtherApplicantNotApplied()).isEqualTo(YesOrNo.YES);
    }
}

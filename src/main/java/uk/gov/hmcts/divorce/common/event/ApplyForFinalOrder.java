package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.ApplyForFinalOrderDetails;
import uk.gov.hmcts.divorce.common.notification.Applicant1AppliedForFinalOrderNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.task.ProgressFinalOrderState;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class ApplyForFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String FINAL_ORDER_REQUESTED = "final-order-requested";

    public static final String APPLY_FOR_FINAL_ORDER = "Apply for final order";

    @Autowired
    private Applicant1AppliedForFinalOrderNotification applicant1AppliedForFinalOrderNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private ProgressFinalOrderState progressFinalOrderState;

    private static final List<CcdPageConfiguration> pages = List.of(
        new ApplyForFinalOrderDetails()
    );

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(FINAL_ORDER_REQUESTED)
            .forStates(AwaitingFinalOrder, AwaitingJointFinalOrder, FinalOrderOverdue)
            .name(APPLY_FOR_FINAL_ORDER)
            .description(APPLY_FOR_FINAL_ORDER)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CREATOR, APPLICANT_1_SOLICITOR)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                APPLICANT_2_SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("Apply for Final Order about to submit callback invoked for Case Id: {}", details.getId());

        CaseData data = details.getData();
        State state = details.getState();

        var applicant1AppliedForFinalOrderFirst = data.getFinalOrder().getApplicant1AppliedForFinalOrderFirst();
        var applicant2AppliedForFinalOrderFirst = data.getFinalOrder().getApplicant1AppliedForFinalOrderFirst();

        if (applicant2AppliedForFinalOrderFirst == null && applicant1AppliedForFinalOrderFirst == null) {
            data.getFinalOrder().setApplicant2AppliedForFinalOrderFirst(NO);
            data.getFinalOrder().setApplicant1AppliedForFinalOrderFirst(YES);
        }

        if (state == AwaitingFinalOrder) {
            notificationDispatcher.send(applicant1AppliedForFinalOrderNotification, data, details.getId());
        }

        details.setData(data);
        var updatedDetails = progressFinalOrderState.apply(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(updatedDetails.getData())
            .state(updatedDetails.getState())
            .build();
    }
}

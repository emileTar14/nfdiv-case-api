package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDateTime;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class SolicitorSubmitConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_SUBMIT_CONDITIONAL_ORDER = "solicitor-submit-conditional-order";

    @Autowired
    private Clock clock;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SOLICITOR_SUBMIT_CONDITIONAL_ORDER)
            .forStateTransition(ConditionalOrderDrafted, AwaitingLegalAdvisorReferral)
            .name("Submit Conditional Order")
            .description("Submit Conditional Order")
            .endButtonLabel("Save Conditional Order")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grant(READ,
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR))
            .page("ConditionalOrderSoT")
            .pageLabel("Statement of Truth - submit conditional order")
            .label("LabelConditionalOrderSoT-SoTStatement",
                "The applicant believes that the facts stated in the application for a conditional order are true")
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getSolicitorName)
                .mandatory(ConditionalOrder::getSolicitorFirm)
                .optional(ConditionalOrder::getSolicitorAdditionalComments)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Solicitor submit conditional order about to submit callback invoked for case id: {}", details.getId());

        details.getData().getConditionalOrder().setDateSubmitted(LocalDateTime.now(clock));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }
}

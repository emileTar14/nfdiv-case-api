package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.service.SubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.*;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CitizenAddPayment implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_ADD_PAYMENT = "citizen-add-payment";

    @Autowired
    private SubmissionService submissionService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(CITIZEN_ADD_PAYMENT)
            .forState(AwaitingPayment)
            .name("Payment reference generated")
            .description("Payment reference generated")
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .grantHistoryOnly(SUPER_USER, CASE_WORKER);
    }
}


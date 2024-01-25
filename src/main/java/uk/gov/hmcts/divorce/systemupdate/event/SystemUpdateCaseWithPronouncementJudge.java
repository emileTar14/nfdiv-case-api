package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InBulkActionCase;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemUpdateCaseWithPronouncementJudge implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE = "system-update-case-pronouncement-judge";

    // TODO NFDIV-3824: We've introduced a new state (InBulkActionCase) for cases which are linked to bulk lists. They will no longer
    //  use the AwaitingPronouncement state. AwaitingPronouncement state should be removed once all cases using this state
    //  (pre- new state InBulkActionCase) have been pronounced and final order granted.
    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE)
            .forStates(AwaitingPronouncement, InBulkActionCase, OfflineDocumentReceived)
            .name("Update pronouncement judge")
            .description("Update case with pronouncement judge")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR));
    }
}

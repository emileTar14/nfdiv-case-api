package uk.gov.hmcts.divorce.common.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.Applicant2HowToRespondToApplication;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolAosJurisdiction;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolAosOtherProceedings;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolConfirmContactDetails;
import uk.gov.hmcts.divorce.common.event.page.Applicant2SolReviewApplicant1Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Component
public class DraftAos implements CCDConfig<CaseData, State, UserRole> {

    public static final String DRAFT_AOS = "draft-aos";

    @Autowired
    private AddMiniApplicationLink addMiniApplicationLink;

    private final List<CcdPageConfiguration> pages = asList(
        new Applicant2SolConfirmContactDetails(),
        new Applicant2SolReviewApplicant1Application(),
        new Applicant2HowToRespondToApplication(),
        new Applicant2SolAosJurisdiction(),
        new Applicant2SolAosOtherProceedings()
    );

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(DRAFT_AOS)
            .forStateTransition(AwaitingAos, AosDrafted)
            .name("Draft AoS")
            .description("Draft Acknowledgement of Service")
            .aboutToStartCallback(this::aboutToStart)
            .showSummary()
            .endButtonLabel("Save AoS Response")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR, APPLICANT_2)
            .grant(READ,
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseTasks(addMiniApplicationLink)
                .run(details)
                .getData())
            .build();
    }
}
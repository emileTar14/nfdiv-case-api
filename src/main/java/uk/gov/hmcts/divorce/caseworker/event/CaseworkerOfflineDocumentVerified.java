package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.SubmitAosService;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService.OfflineDocumentReceived.AOS_D10;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerOfflineDocumentVerified implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private SubmitAosService submitAosService;

    public static final String CASEWORKER_OFFLINE_DOCUMENT_VERIFIED = "caseworker-offline-document-verified";
    private static final String ALWAYS_HIDE = "typeOfDocumentAttached=\"ALWAYS_HIDE\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_OFFLINE_DOCUMENT_VERIFIED)
            .forState(OfflineDocumentReceived)
            .name("Offline Document Verified")
            .description("Offline Document Verified")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showEventNotes()
            .showSummary()
            .grant(CREATE_READ_UPDATE, CASE_WORKER_BULK_SCAN, CASE_WORKER, SUPER_USER))
            .page("documentTypeReceived")
            .readonlyNoSummary(CaseData::getApplicationType, ALWAYS_HIDE)
            .showCondition("applicationType=\"soleApplication\"")
            .complex(CaseData::getAcknowledgementOfService)
                .mandatory(AcknowledgementOfService::getTypeOfDocumentAttached)
                .mandatory(AcknowledgementOfService::getHowToRespondApplication, "typeOfDocumentAttached=\"D10\"")
            .done()
            .page("stateToTransitionTo")
            .showCondition("applicationType=\"jointApplication\" OR typeOfDocumentAttached=\"Other\"")
            .complex(CaseData::getApplication)
                .mandatory(Application::getStateToTransitionApplicationTo)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final CaseData data = details.getData();

        if (AOS_D10.equals(data.getAcknowledgementOfService().getTypeOfDocumentAttached())) {

            final CaseDetails<CaseData, State> response = submitAosService.submitOfflineAos(details);
            response.getData().getApplicant2().setOffline(YES);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(response.getData())
                .state(response.getState())
                .build();
        } else {

            final State state = data.getApplication().getStateToTransitionApplicationTo();

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .state(state)
                .build();
        }
    }
}

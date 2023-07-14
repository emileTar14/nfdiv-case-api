package uk.gov.hmcts.divorce.systemupdate.schedule.migration.task;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;

@Component
public class UpdateConfirmReadPetitionFields implements CaseTask {

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        final CaseData caseData = caseDetails.getData();

        caseData.getAcknowledgementOfService().setConfirmReadPetition(NO);
        caseData.getAcknowledgementOfService().setAosIsDrafted(NO);

        return caseDetails;
    }
}
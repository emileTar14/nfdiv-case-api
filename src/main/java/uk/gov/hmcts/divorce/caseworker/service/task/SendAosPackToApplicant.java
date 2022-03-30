package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.print.AosPackPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

@Component
@Slf4j
public class SendAosPackToApplicant implements CaseTask {

    @Autowired
    private AosPackPrinter aosPackPrinter;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        if (caseData.getApplicationType().isSole() && !caseData.getApplicant1().isRepresented()) {

            if (!caseData.getApplicant2().isRepresented() && caseData.getApplicant2().isBasedOverseas()) {
                log.info("Sending NOP and application pack to bulk print as applicant 1 and applicant 2 are not "
                    + "represented and applicant 2 is overseas. Case id: {}:", caseId);
                aosPackPrinter.sendOverseasAosLetterToApplicant(caseData, caseId);
            } else {
                log.info("Sending NOP and application pack to bulk print as applicant 1 is not represented. Case id: {}:", caseId);
                aosPackPrinter.sendAosLetterToApplicant(caseData, caseId);
            }
        } else {
            log.info("Not sending NOP and application pack to bulk print as applicant 1 is represented. Case id: {}:", caseId);
        }
        return caseDetails;
    }
}

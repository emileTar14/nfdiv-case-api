package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.Map;

import static uk.gov.hmcts.divorce.notification.CommonContent.ACCESS_CODE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CREATE_ACCOUNT_LINK;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_REMINDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.REVIEW_DEADLINE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.SUBMISSION_RESPONSE_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.CommonContent.isDivorce;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLICANT_PARTNER_HAS_NOT_RESPONDED;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class AosReminderNotifications {

    private static final String RESPONDENT_SIGN_IN_DIVORCE_URL = "respondentSignInDivorceUrl";
    private static final String RESPONDENT_SIGN_IN_DISSOLUTION_URL = "respondentSignInDissolutionUrl";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private EmailTemplatesConfig config;

    public void sendPartnerNotRespondedToSoleApplicant(CaseData caseData, Long id) {
        log.info("Sending the respondent has not responded notification to the applicant for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOLE_APPLICANT_PARTNER_HAS_NOT_RESPONDED,
            commonTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2()),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendReminderToSoleRespondent(CaseData caseData, Long id) {
        log.info("Sending reminder to respondent to register for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2EmailAddress(),
            SOLE_RESPONDENT_APPLICATION_ACCEPTED,
            reminderToSoleRespondentTemplateVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    private Map<String, String> reminderToSoleRespondentTemplateVars(final CaseData caseData, Long id) {
        final Map<String, String> templateVars = commonTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());
        templateVars.put(REVIEW_DEADLINE_DATE, caseData.getApplication().getIssueDate().plusDays(16).format(DATE_TIME_FORMATTER));
        templateVars.put(
            CREATE_ACCOUNT_LINK,
            config.getTemplateVars()
                .get(isDivorce(caseData) ? RESPONDENT_SIGN_IN_DIVORCE_URL : RESPONDENT_SIGN_IN_DISSOLUTION_URL)
        );
        templateVars.put(ACCESS_CODE, caseData.getCaseInvite().getAccessCode());
        templateVars.put(IS_REMINDER, YES);
        return templateVars;
    }

    private Map<String, String> commonTemplateVars(final CaseData caseData, Long id, Applicant applicant, Applicant partner) {
        final Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, applicant, partner);
        templateVars.put(SUBMISSION_RESPONSE_DATE, caseData.getDueDate().format(DATE_TIME_FORMATTER));
        return templateVars;
    }
}
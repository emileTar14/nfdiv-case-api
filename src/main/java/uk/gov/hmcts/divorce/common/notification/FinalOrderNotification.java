package uk.gov.hmcts.divorce.common.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NOT_PROVIDED;
import static uk.gov.hmcts.divorce.notification.CommonContent.DATE_OF_ISSUE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.SIGN_IN_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.JOINT_SOLICITOR_BOTH_APPLIED_CO_FO;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class FinalOrderNotification implements ApplicantNotification {

    public static final String WILL_BE_CHECKED_WITHIN_2_DAYS = "will be checked within 2 days";
    public static final String WILL_BE_CHECKED_WITHIN_14_DAYS = "will be checked within 14 days";
    public static final String NOW_PLUS_14_DAYS = "now plus 14 days";

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {

        if (caseData.getApplicationType().isSole() && ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), id)) {
            log.info("Sending Applicant 1 notification informing them that they have applied for final order: {}", id);
            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                SOLE_APPLIED_FOR_FINAL_ORDER,
                applicant1TemplateVars(caseData, id),
                caseData.getApplicant1().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {

        if (caseData.getApplicationType().isSole() && !ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), id)) {

            log.info("Sending Applicant 2 notification informing them that they have applied for final order: {}", id);
            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                SOLE_APPLIED_FOR_FINAL_ORDER,
                applicant2TemplateVars(caseData, id),
                caseData.getApplicant2().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            var templateVars = solicitorTemplateVars(caseData, caseId, caseData.getApplicant1());

            notificationService.sendEmail(
                caseData.getApplicant1().getSolicitor().getEmail(),
                JOINT_SOLICITOR_BOTH_APPLIED_CO_FO,
                templateVars,
                caseData.getApplicant1().getLanguagePreference()
            );
        }
    }

    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long caseId) {
        if (!caseData.getApplicationType().isSole()) {
            var templateVars = solicitorTemplateVars(caseData, caseId, caseData.getApplicant2());

            notificationService.sendEmail(
                caseData.getApplicant2().getSolicitor().getEmail(),
                JOINT_SOLICITOR_BOTH_APPLIED_CO_FO,
                templateVars,
                caseData.getApplicant2().getLanguagePreference()
            );
        }
    }

    private Map<String, String> solicitorTemplateVars(final CaseData caseData, final Long caseId, Applicant applicant) {
        Map<String, String> templateVars = commonContent.basicTemplateVars(caseData, caseId);

        templateVars.put(IS_CONDITIONAL_ORDER, NO);
        templateVars.put(IS_FINAL_ORDER, YES);
        templateVars.put(SOLICITOR_NAME, applicant.getSolicitor().getName());
        templateVars.put(SOLICITOR_REFERENCE,
            isNotEmpty(applicant.getSolicitor().getReference()) ? applicant.getSolicitor().getReference() : NOT_PROVIDED);
        templateVars.put(IS_DIVORCE, caseData.getDivorceOrDissolution().isDivorce() ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !caseData.getDivorceOrDissolution().isDivorce() ? YES : NO);
        templateVars.put(SIGN_IN_URL, commonContent.getProfessionalUsersSignInUrl(caseId));
        templateVars.put(DATE_OF_ISSUE, caseData.getApplication().getIssueDate().format(DATE_TIME_FORMATTER));

        return templateVars;
    }

    private Map<String, String> applicant1TemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());

        boolean isFinalOrderEligible = caseData.getFinalOrder().getDateFinalOrderNoLongerEligible().isAfter(LocalDate.now(clock));

        templateVars.put(WILL_BE_CHECKED_WITHIN_2_DAYS, isFinalOrderEligible ? YES : NO);
        templateVars.put(WILL_BE_CHECKED_WITHIN_14_DAYS, !isFinalOrderEligible ? YES : NO);
        templateVars.put(NOW_PLUS_14_DAYS, !isFinalOrderEligible ? getNowPlus14Days() : "");

        return templateVars;
    }

    private Map<String, String> applicant2TemplateVars(CaseData caseData, Long id) {
        Map<String, String> templateVars =
            commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());

        templateVars.put(WILL_BE_CHECKED_WITHIN_2_DAYS, NO);
        templateVars.put(WILL_BE_CHECKED_WITHIN_14_DAYS, YES);
        templateVars.put(NOW_PLUS_14_DAYS, getNowPlus14Days());

        return templateVars;
    }

    private String getNowPlus14Days() {
        return LocalDate.now(clock).plusDays(14).format(DATE_TIME_FORMATTER);
    }
}
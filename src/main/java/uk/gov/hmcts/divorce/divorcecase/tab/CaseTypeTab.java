package uk.gov.hmcts.divorce.divorcecase.tab;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.notShowForState;
import static uk.gov.hmcts.divorce.divorcecase.tab.TabShowCondition.showForState;

@Component
public class CaseTypeTab implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        buildStateTab(configBuilder);
        buildAosTab(configBuilder);
        buildPaymentTab(configBuilder);
        buildLanguageTab(configBuilder);
        buildDocumentsTab(configBuilder);
        buildConfidentialApplicantTab(configBuilder);
        buildConfidentialRespondentTab(configBuilder);
        buildMarriageCertificateTab(configBuilder);
        buildNotesTab(configBuilder);
        buildGeneralReferralTab(configBuilder);
        buildConfidentialDocumentsTab(configBuilder);
        buildServiceApplicationTab(configBuilder);
        buildConditionalOrderTab(configBuilder);
        buildOutcomeOfConditionalOrderTab(configBuilder);
        buildFinalOrderTab(configBuilder);
    }

    private void buildStateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("state", "State")
            .forRoles(APPLICANT_2_SOLICITOR)
            .label("LabelState", null, "#### Case State:  ${[STATE]}");
    }

    //TODO: Need to revisit this tab once the field stated in the ticket NFDIV-595 are available
    private void buildAosTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("aosDetails", "AoS")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR,
                SUPER_USER, SOLICITOR)
            .showCondition("applicationType=\"soleApplication\" AND "
                + notShowForState(Draft, AwaitingHWFDecision, AwaitingPayment, Submitted, AwaitingDocuments))
            .label("LabelAosTabOnlineResponse-Heading", null, "## This is an online AoS response")
            .field("confirmReadPetition")
            .field("jurisdictionAgree")
            .field("reasonCourtsOfEnglandAndWalesHaveNoJurisdiction", "jurisdictionAgree=\"No\"")
            .field("inWhichCountryIsYourLifeMainlyBased", "jurisdictionAgree=\"No\"")
            .field("applicant2LegalProceedings")
            .field("applicant2LegalProceedingsDetails")
            .field("applicant2UserId")
            .field("dueDate")
            .label("LabelAosTabOnlineResponse-RespondentRepresent", null, "### Respondent")
            .field("applicant2SolicitorRepresented")
            .field("digitalNoticeOfProceedings")
            .field("noticeOfProceedingsEmail")
            .field("noticeOfProceedingsSolicitorFirm");
    }

    private void buildPaymentTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("paymentDetailsCourtAdmin", "Payment")
            .field("applicant1HWFReferenceNumber");
    }

    private void buildLanguageTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("languageDetails", "Language")
            .label("LabelLanguageDetails-Applicant", null, "### The applicant")
            .field("applicant1LanguagePreferenceWelsh")
            .label("LabelLanguageDetails-Respondent", null, "### The respondent")
            .field("applicant2LanguagePreferenceWelsh");
    }

    private void buildDocumentsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("documents", "Documents")
            .field(CaseData::getDocumentsGenerated)
            .field(CaseData::getApplicant1DocumentsUploaded)
            .field(CaseData::getGeneralOrders)
            .field(CaseData::getDocumentsUploaded)
            .field("certificateOfServiceDocument")
            .field("coCertificateOfEntitlementDocument");
    }

    private void buildConfidentialApplicantTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("ConfidentialApplicant", "Confidential Address")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR)
            .showCondition("applicant1KeepContactDetailsConfidential=\"Yes\"")
            .field("applicant1CorrespondenceAddress")
            .field("applicant1PhoneNumber")
            .field("applicant1Email")
            .field("applicant1HomeAddress");
    }

    private void buildConfidentialRespondentTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("ConfidentialRespondent", "Confidential Respondent")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR)
            .showCondition("applicant2KeepContactDetailsConfidential=\"Yes\"")
            .field("applicant2CorrespondenceAddress")
            .field("applicant2PhoneNumber")
            .field("applicant2Email")
            .field("applicant2HomeAddress");
    }

    private void buildMarriageCertificateTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("marriageDetails", "Marriage Certificate")
            .field("labelContentTheApplicant2UC", "marriageMarriedInUk=\"NEVER_SHOW\"")
            .field("marriageApplicant1Name")
            .field("marriageApplicant2Name")
            .field("marriageDate")
            .field("marriageMarriedInUk")
            .field("marriagePlaceOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCountryOfMarriage", "marriageMarriedInUk=\"No\"")
            .field("marriageCertifyMarriageCertificateIsCorrect")
            .field("marriageMarriageCertificateIsIncorrectDetails", "marriageCertifyMarriageCertificateIsCorrect=\"No\"")
            .field("marriageIssueApplicationWithoutMarriageCertificate", "marriageCertifyMarriageCertificateIsCorrect=\"No\"");
    }

    private void buildNotesTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("notes", "Notes")
            .field(CaseData::getNotes);
    }

    private void buildGeneralReferralTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("generalReferral", "General Referral")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .field("generalReferralReason")
            .field("generalApplicationFrom", "generalApplicationFrom=\"*\"")
            .field("generalApplicationReferralDate", "generalApplicationReferralDate=\"*\"")
            .field("generalApplicationAddedDate")
            .field("generalReferralType")
            .field("alternativeServiceMedium")
            .field("generalReferralJudgeDetails")
            .field("generalReferralLegalAdvisorDetails")
            .field("generalReferralFeeRequired");
    }

    private void buildConfidentialDocumentsTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("confidentialDocuments", "Confidential Document")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .field(CaseData::getConfidentialDocumentsUploaded);
    }

    private void buildServiceApplicationTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("alternativeService", "Service Application")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SUPER_USER)
            .field("receivedServiceApplicationDate")
            .field("receivedServiceAddedDate")
            .field("alternativeServiceType")
            .field("paymentMethod")
            .field("dateOfPayment", "paymentMethod=\"*\"")
            .field("feeAccountNumber", "paymentMethod=\"feePayByAccount\"")
            .field("feeAccountReferenceNumber", "paymentMethod=\"feePayByAccount\"")
            .field("helpWithFeesReferenceNumber", "paymentMethod=\"feePayByHelp\"")
            .label("bailiffLocalCourtDetailsLabel",
                "localCourtName=\"*\" OR localCourtEmail=\"*\"", "### Bailiff local court details")
            .field("localCourtName")
            .field("localCourtEmail")
            .label("bailiffReturnLabel",
                "certificateOfServiceDate=\"*\" OR successfulServedByBailiff=\"*\" OR reasonFailureToServeByBailiff=\"*\"",
                "### Bailiff return")
            .field("certificateOfServiceDate")
            .label("serviceOutcomeLabel",
                "serviceApplicationGranted=\"No\" OR serviceApplicationGranted=\"Yes\"",
                "### Outcome of Service Application")
            .field("serviceApplicationGranted")
            .field("serviceApplicationDecisionDate")
            .field("serviceApplicationRefusalReason", "serviceApplicationGranted=\"No\"")
            .field("deemedServiceDate")
            .field("successfulServedByBailiff")
            .field("reasonFailureToServeByBailiff")
            .field("alternativeServiceOutcomes");
    }

    private void buildConditionalOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("conditionalOrder", "Conditional Order")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SOLICITOR, SUPER_USER)
            .showCondition("coDateSubmitted=\"*\"")
            .field("coApplyForConditionalOrder")
            .field("coDateSubmitted")
            .field("coChangeOrAddToApplication")
            .field("coApplicantStatementOfTruth")
            .field("coSolicitorName")
            .field("coSolicitorFirm")
            .field("coSolicitorAdditionalComments")
            .field("coCourtName")
            .field("coDateAndTimeOfHearing")
            .field("coPronouncementJudge");
    }

    private void buildOutcomeOfConditionalOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("outcomeOfConditionalOrder", "Outcome of Conditional Order")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SOLICITOR, SUPER_USER)
            .showCondition("coGranted=\"*\"")
            .label("labelLegalAdvisorDecision", null, "## Legal advisor decision")
            .field("coDecisionDate")
            .field("coGranted")
            .field("coClaimsGranted")
            .field("coClaimsCostsOrderInformation")
            .field("coRefusalDecision", "coGranted=\"No\"")
            .field("coRefusalAdminErrorInfo", "coGranted=\"No\"")
            .field("coRefusalRejectionReason", "coGranted=\"No\"")
            .field("coRefusalRejectionAdditionalInfo", "coGranted=\"No\"")
            .field("coRefusalClarificationReason", "coGranted=\"No\"")
            .field("coRefusalClarificationAdditionalInfo", "coGranted=\"No\"")
            .label("labelCoClarificationResponses",
                "coGranted=\"*\" AND coClarificationResponses=\"*\"",
                "## Clarification Responses")
            .field("coRefusalDecision", "coGranted=\"No\"")
            .field("coRefusalRejectionReason", "coGranted=\"No\"")
            .field("coRefusalClarificationAdditionalInfo", "coGranted=\"No\"")
            .field("coRefusalAdminErrorInfo", "coGranted=\"No\"")
            .field("coRefusalRejectionAdditionalInfo", "coGranted=\"No\"")
            .field("coClarificationResponses", "coGranted=\"No\"")
            .field("coClarificationUploadDocuments", "coGranted=\"No\"")
            .label("labelCoPronouncementDetails", null, "## Pronouncement Details")
            .field("bulkListCaseReference")
            .field("coCourt")
            .field("coDateAndTimeOfHearing")
            .field("coPronouncementJudge")
            .field("coGrantedDate")
            .field("dateFinalOrderEligibleFrom")
            .field("coOutcomeCase")
            .label("labelJudgeCostsDecision",
                "coJudgeCostsClaimGranted=\"*\" OR coJudgeCostsOrderAdditionalInfo=\"*\"",
                "## Judge costs decision")
            .field("coJudgeCostsClaimGranted")
            .field("coJudgeCostsOrderAdditionalInfo")
            .field("coCertificateOfEntitlementDocument");
    }

    private void buildFinalOrderTab(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.tab("finalOrder", "Final Order")
            .forRoles(CASE_WORKER, LEGAL_ADVISOR, SOLICITOR, SUPER_USER)
            .showCondition(showForState(
                AwaitingFinalOrder,
                FinalOrderRequested,
                FinalOrderPending,
                FinalOrderOverdue,
                FinalOrderComplete))
            .field("doesApplicantWantToApplyForFinalOrder")
            .field("granted")
            .field("grantedDate")
            .field("dateFinalOrderNoLongerEligible")
            .field("dateFinalOrderEligibleToRespondent");
    }
}

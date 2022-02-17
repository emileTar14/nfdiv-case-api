package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;

public class Applicant2ServiceDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("Applicant2ServiceDetails")
            .pageLabel("Service details")
            .complex(CaseData::getApplicant2)
                .mandatoryWithLabel(Applicant::getSolicitorRepresented, "Is ${labelContentTheApplicant2} represented by a solicitor?")
                .complex(Applicant::getSolicitor)

                    .mandatory(Solicitor::getName,
                        "applicant2SolicitorRepresented=\"Yes\"",
                        null,
                        "${labelContentApplicant2UC} solicitor's full name",
                        "Enter the full name of the individual solicitor who will be dealing with the case")

                    .optional(Solicitor::getReference,
                        "applicant2SolicitorRepresented=\"Yes\"",
                        null,
                        "${labelContentApplicant2UC} solicitor's reference",
                        "The internal reference that the solicitor’s firm uses to identify the case")

                    .optional(Solicitor::getPhone,
                "applicant2SolicitorRepresented=\"Yes\"",
                null,
                "${labelContentApplicant2UC} solicitor's phone number",
                "The solicitor’s direct phone number")

                    .mandatory(Solicitor::getEmail,
                        "applicant2SolicitorRepresented=\"Yes\"",
                        null,
                        "${labelContentApplicant2UC} solicitor’s email address",
                        "The solicitor’s direct email address")

                    .mandatory(Solicitor::getAddress,
                        "applicant2SolicitorRepresented=\"Yes\"",
                        null,
                        "${labelContentApplicant2UC} solicitor's postal address",
                        "solicitor’s postal address")

                    .complex(Solicitor::getOrganisationPolicy, "applicant2SolicitorRepresented=\"Yes\"")
                        .complex(OrganisationPolicy::getOrganisation)
                            .mandatory(Organisation::getOrganisationId)
                            .done()
                        .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole, NEVER_SHOW, APPLICANT_2_SOLICITOR)
                        .optional(OrganisationPolicy::getOrgPolicyReference, NEVER_SHOW)
                        .done()
                    .done()

                .mandatory(Applicant::getHomeAddress,
                    "applicant2SolicitorRepresented=\"Yes\"",
                    null,
                    "${labelContentApplicant2UC} postal address",
                    "This address will be used to notify them about the application")

                .mandatory(Applicant::getEmail,
                    "applicant2SolicitorRepresented=\"Yes\"",
                    null,
                    "${labelContentApplicant2UC} email address")

                .optional(Applicant::getEmail,
                    "applicant2SolicitorRepresented=\"No\"",
                    null,
                    "Enter the email address which they actively use for personal emails")
            .done();
    }
}

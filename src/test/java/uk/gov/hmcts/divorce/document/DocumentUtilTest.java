package uk.gov.hmcts.divorce.document;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.divorce.document.print.model.Letter;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.DocumentUtil.divorceDocumentFrom;
import static uk.gov.hmcts.divorce.document.DocumentUtil.documentFrom;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentType;
import static uk.gov.hmcts.divorce.document.DocumentUtil.lettersWithDocumentTypeSortedByAddedDateDesc;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.OTHER;

@ExtendWith(MockitoExtension.class)
class DocumentUtilTest {

    private static final String DOC_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003";
    private static final String DOC_BINARY_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary";
    private static final String PDF_FILENAME = "draft-divorce-application-1616591401473378.pdf";
    private static final String URL = "url";
    private static final String FILENAME = "filename";
    private static final String BINARY_URL = "binaryUrl";

    @Test
    void shouldConvertFromDocumentInfoToDocument() {

        final Document document = documentFrom(documentInfo());

        assertThat(document)
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL);
    }

    @Test
    void shouldCreateDivorceDocumentFromDocumentInfoAndDocumentType() {

        final DivorceDocument divorceDocument = divorceDocumentFrom(documentInfo(), OTHER);

        assertThat(divorceDocument.getDocumentType()).isEqualTo(OTHER);
        assertThat(divorceDocument.getDocumentFileName()).isEqualTo(PDF_FILENAME);
        assertThat(divorceDocument
            .getDocumentLink())
            .extracting(URL, FILENAME, BINARY_URL)
            .contains(
                DOC_URL,
                PDF_FILENAME,
                DOC_BINARY_URL);
    }

    @Test
    void shouldReturnListOfLetterOfGivenDocumentTypeIfPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .build())
            .build();

        final List<Letter> letters = lettersWithDocumentType(
            asList(doc1, doc2),
            MARRIAGE_CERTIFICATE);

        assertThat(letters.size()).isEqualTo(1);
        assertThat(letters.get(0).getDivorceDocument()).isSameAs(doc2.getValue());
    }

    @Test
    void shouldNotFindDocumentOfGivenDocumentTypeIfNotPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .build())
            .build();

        final List<Letter> letters = lettersWithDocumentType(
            asList(doc1, doc2),
            NAME_CHANGE_EVIDENCE);

        assertThat(letters.size()).isZero();
    }

    @Test
    void shouldReturnEmptyListIfNullDocumentList() {
        final List<Letter> letters = lettersWithDocumentType(null, NAME_CHANGE_EVIDENCE);
        assertThat(letters.size()).isZero();
    }

    @Test
    void shouldReturnEmptyListIfEmptyDocumentList() {
        final List<Letter> letters = lettersWithDocumentType(emptyList(), NAME_CHANGE_EVIDENCE);
        assertThat(letters.size()).isZero();
    }

    @Test
    void shouldReturnListOfLettersOfGivenDocumentTypesIfPresent() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(APPLICATION)
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .build())
            .build();

        final List<Letter> letters = lettersWithDocumentType(
            asList(doc1, doc2, doc3),
            NAME_CHANGE_EVIDENCE);

        assertThat(letters.size()).isEqualTo(1);
        assertThat(letters.get(0).getDivorceDocument()).isSameAs(doc3.getValue());
    }

    @Test
    void shouldReturnListOfLettersOfGivenDocumentTypesSortedByAddedDateInDescOrder() {

        final ListValue<DivorceDocument> doc1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .documentDateAdded(LocalDate.now())
                .build())
            .build();

        final ListValue<DivorceDocument> doc2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .documentDateAdded(LocalDate.now().minusDays(2))
                .build())
            .build();

        final ListValue<DivorceDocument> doc3 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(NAME_CHANGE_EVIDENCE)
                .documentDateAdded(LocalDate.now().plusDays(2))
                .build())
            .build();

        final ListValue<DivorceDocument> doc4 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(MARRIAGE_CERTIFICATE)
                .documentDateAdded(LocalDate.now().plusDays(4))
                .build())
            .build();

        final List<Letter> letters = lettersWithDocumentTypeSortedByAddedDateDesc(
            asList(doc1, doc2, doc3, doc4),
            NAME_CHANGE_EVIDENCE);

        assertThat(letters.size()).isEqualTo(3);
        assertThat(letters.get(0).getDivorceDocument()).isSameAs(doc3.getValue());
        assertThat(letters.get(1).getDivorceDocument()).isSameAs(doc1.getValue());
        assertThat(letters.get(2).getDivorceDocument()).isSameAs(doc2.getValue());
    }

    private DocumentInfo documentInfo() {
        return new DocumentInfo(
            DOC_URL,
            PDF_FILENAME,
            DOC_BINARY_URL
        );
    }
}

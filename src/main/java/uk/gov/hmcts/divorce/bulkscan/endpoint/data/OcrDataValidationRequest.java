package uk.gov.hmcts.divorce.bulkscan.endpoint.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.type.KeyValue;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrDataValidationRequest {

    @JsonProperty("ocr_data_fields")
    private List<KeyValue> ocrDataFields;
}
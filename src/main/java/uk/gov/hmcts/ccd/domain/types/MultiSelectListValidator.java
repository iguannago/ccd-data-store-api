package uk.gov.hmcts.ccd.domain.types;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.ccd.domain.model.definition.CaseField;
import uk.gov.hmcts.ccd.domain.model.definition.FixedListItem;

import javax.inject.Named;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Named
@Singleton
public class MultiSelectListValidator implements BaseTypeValidator {
    private static final String TYPE_ID = "MultiSelectList";

    @Override
    public BaseType getType() {
        return BaseType.get(TYPE_ID);
    }

    @Override
    public List<ValidationResult> validate(final String dataFieldId,
                                           final JsonNode dataValue,
                                           final CaseField caseFieldDefinition) {
        if (isNullOrEmpty(dataValue)) {
            return Collections.emptyList();
        }

        if (!dataValue.isArray()) {
            final ValidationResult result = new ValidationResult(
                "Value should be an array of strings",
                dataFieldId);
            return Collections.singletonList(result);
        }

        final List<ValidationResult> results = new ArrayList<>();

        final List<FixedListItem> validValues = caseFieldDefinition.getFieldType().getFixedListItems();

        final HashSet<String> uniqueValues = new HashSet<>();

        dataValue.forEach(value -> {
            final String textValue = value.asText();

            final Boolean found = validValues.stream()
                .anyMatch(validValue -> validValue.getCode().equals(textValue));

            if (!found) {
                results.add(new ValidationResult(value + " is not a valid value", dataFieldId));
            } else if (uniqueValues.contains(textValue)) {
                results.add(new ValidationResult(value + " is duplicated", dataFieldId));
            } else {
                uniqueValues.add(textValue);
            }
        });

        final BigDecimal minimum = caseFieldDefinition.getFieldType().getMin();
        if (!checkMin(minimum, uniqueValues.size())) {
            final String message = String.format("Select at least %d %s", minimum.intValue(), minimum.equals(new BigDecimal(1)) ? "option" : "options");
            results.add(new ValidationResult(message, dataFieldId));
        }

        final BigDecimal maximum = caseFieldDefinition.getFieldType().getMax();
        if (!checkMax(maximum, uniqueValues.size())) {
            final String message = String.format("Cannot select more than %d %s", maximum.intValue(), maximum.equals(new BigDecimal(1)) ? "option" : "options");
            results.add(new ValidationResult(message, dataFieldId));
        }

        return results;
    }

    private Boolean checkMax(final BigDecimal max, final Integer actualLength) {
        return max == null || actualLength <= max.intValue();
    }

    private Boolean checkMin(final BigDecimal min, final Integer actualLength) {
        return min == null || actualLength >= min.intValue();
    }
}

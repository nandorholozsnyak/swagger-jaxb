package be.redlab.jaxb.swagger.evaluator;

import be.redlab.jaxb.swagger.constants.ApiModelPropertyFields;
import com.sun.codemodel.JAnnotationUse;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSSimpleType;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class AllowableValueEvaluatorRule implements SwaggerAnnotationEvaluatorRule {

    private static final Logger log = Logger.getLogger(AllowableValueEvaluatorRule.class.getName());

    @Override
    public void annotate(XsdField xsdField) {
        if (xsdField.hasNoFieldValues()) {
            XsdField.Extremum minimum = xsdField.getMinimum();
            XsdField.Extremum maximum = xsdField.getMaximum();
            populateAllowableValueField(xsdField.getApiModelProperty(), minimum.getValue(), maximum.getValue(), minimum.isInclusive(), maximum.isInclusive());
        } else {
            if (xsdField.hasOnlyLength()) {
                populateAllowableValueField(xsdField.getApiModelProperty(), xsdField.getLength(), xsdField.getLength(), true, true);
            } else {
                populateAllowableValueField(xsdField.getApiModelProperty(), xsdField.getMinLength(), xsdField.getMaxLength(), true, true);
            }
        }
    }

    /**
     * Fills up {@link ApiModelProperty#allowableValues()} field with the XSD type min/max inclusive/exclusive fields.
     *
     * @param apiProperty  annotation to populate.
     * @param xsSimpleType XSD simple type.
     */
    public void addExtremum(JAnnotationUse apiProperty, XSSimpleType xsSimpleType) {
        Integer length = getFacetAsInteger(xsSimpleType, XSFacet.FACET_LENGTH);
        Integer minLength = getFacetAsInteger(xsSimpleType, XSFacet.FACET_MINLENGTH);
        Integer maxLength = getFacetAsInteger(xsSimpleType, XSFacet.FACET_MAXLENGTH);
        if (Objects.isNull(length) && Objects.isNull(minLength) && Objects.isNull(maxLength)) {
            Extremum minimum = getMinimum(xsSimpleType);
            Extremum maximum = getMaximum(xsSimpleType);
            populateAllowableValueField(apiProperty, minimum.value, maximum.value, minimum.inclusive, maximum.inclusive);
        }
    }

    private Extremum getMinimum(XSSimpleType xsSimpleType) {
        boolean inclusive = false;
        String minInclusive = getFacetAsString(xsSimpleType, XSFacet.FACET_MININCLUSIVE);
        String minExclusive = getFacetAsString(xsSimpleType, XSFacet.FACET_MINEXCLUSIVE);
        String minimum = null;
        if (Objects.nonNull(minInclusive)) {
            minimum = minInclusive;
            inclusive = true;
        } else if (Objects.nonNull(minExclusive)) {
            minimum = minExclusive;
        }
        return new Extremum(minimum, inclusive);
    }

    private Extremum getMaximum(XSSimpleType xsSimpleType) {
        boolean inclusive = false;
        String maximum = null;
        String maxInclusive = getFacetAsString(xsSimpleType, XSFacet.FACET_MAXINCLUSIVE);
        String maxExclusive = getFacetAsString(xsSimpleType, XSFacet.FACET_MAXEXCLUSIVE);
        if (Objects.nonNull(maxInclusive)) {
            maximum = maxInclusive;
            inclusive = true;
        } else if (Objects.nonNull(maxExclusive)) {
            maximum = maxExclusive;
        }
        return new Extremum(maximum, inclusive);
    }

    private void populateAllowableValueField(JAnnotationUse apiProperty, Object minLength, Object maxLength, boolean inclusiveMin, boolean inclusiveMax) {
        StringBuilder stringBuilder = new StringBuilder("range");
        appendMinLength(stringBuilder, minLength, inclusiveMin);
        stringBuilder.append(", ");
        appendMaxLength(stringBuilder, maxLength, inclusiveMax);
        apiProperty.param(ApiModelPropertyFields.ALLOWABLE_VALUES, stringBuilder.toString());
    }

    private void appendMinLength(StringBuilder stringBuilder, Object minLength, boolean inclusiveMin) {
        if (inclusiveMin) {
            stringBuilder.append("[");
        } else {
            stringBuilder.append("(");
        }
        if (minLength != null) {
            stringBuilder.append(minLength);
        } else {
            stringBuilder.append("-infinity");
        }
    }

    private void appendMaxLength(StringBuilder stringBuilder, Object maxLength, boolean inclusiveMax) {
        if (maxLength != null) {
            stringBuilder.append(maxLength);
        } else {
            stringBuilder.append("infinity");
        }
        if (inclusiveMax) {
            stringBuilder.append("]");
        } else {
            stringBuilder.append(")");
        }
    }

    private Integer getFacetAsInteger(XSSimpleType xsSimpleType, String facetName) {
        return Optional.ofNullable(getFacetAsString(xsSimpleType, facetName))
            .map(stringValue -> getIntegerValue(xsSimpleType, facetName, stringValue))
            .orElse(null);
    }

    private String getFacetAsString(XSSimpleType xsSimpleType, String facetName) {
        return Optional.ofNullable(xsSimpleType.getFacet(facetName))
            .map(XSFacet::getValue)
            .map(xmlString -> xmlString.value)
            .orElse(null);
    }

    private Integer getIntegerValue(XSSimpleType xsSimpleType, String facetName, String stringValue) {
        try {
            return Integer.valueOf(stringValue);
        } catch (NumberFormatException e) {
            String format = String.format("Could not obtain facet : [%s]=[%s] as Integer from SimpleType:[%s]!", facetName, stringValue, xsSimpleType);
            log.warning(format);
            return null;
        }
    }

    /**
     * Inner class to represent an Extremum object with a value and a flag which indicates if it is an inclusive or exclusive range.
     */
    private static class Extremum {

        private final String value;

        private final boolean inclusive;

        public Extremum(String value, boolean inclusive) {
            this.value = value;
            this.inclusive = inclusive;
        }
    }
}

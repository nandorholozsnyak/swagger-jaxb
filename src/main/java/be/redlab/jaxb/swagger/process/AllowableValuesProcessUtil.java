package be.redlab.jaxb.swagger.process;

import be.redlab.jaxb.swagger.constants.ApiModelPropertyFields;
import com.sun.codemodel.JAnnotationUse;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSSimpleType;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author nandor.holozsnyak
 */
public final class AllowableValuesProcessUtil {

    private static final Logger log = Logger.getLogger(AllowableValuesProcessUtil.class.getName());

    /**
     * Fills up {@link ApiModelProperty#allowableValues()} field with the values of the simple type.
     *
     * @param apiProperty  annotation to populate.
     * @param xsSimpleType XSD simple type.
     */
    public static void addLength(JAnnotationUse apiProperty, XSSimpleType xsSimpleType) {
        Integer length = getFacetAsInteger(xsSimpleType, XSFacet.FACET_LENGTH);
        Integer maxLength;
        Integer minLength;
        if (length != null) {
            maxLength = length;
            minLength = length;
        } else {
            maxLength = getFacetAsInteger(xsSimpleType, XSFacet.FACET_MAXLENGTH);
            minLength = getFacetAsInteger(xsSimpleType, XSFacet.FACET_MINLENGTH);
        }
        populateAllowableValueField(apiProperty, minLength, maxLength, true, true);
    }

    /**
     * Fills up {@link ApiModelProperty#allowableValues()} field with the XSD type min/max inclusive/exclusive fields.
     *
     * @param apiProperty  annotation to populate.
     * @param xsSimpleType XSD simple type.
     */
    public static void addExtremum(JAnnotationUse apiProperty, XSSimpleType xsSimpleType) {
        Integer length = getFacetAsInteger(xsSimpleType, XSFacet.FACET_LENGTH);
        Integer minLength = getFacetAsInteger(xsSimpleType, XSFacet.FACET_MINLENGTH);
        Integer maxLength = getFacetAsInteger(xsSimpleType, XSFacet.FACET_MAXLENGTH);
        if (Objects.isNull(length) && Objects.isNull(minLength) && Objects.isNull(maxLength)) {
            Extremum minimum = getMinimum(xsSimpleType);
            Extremum maximum = getMaximum(xsSimpleType);
            populateAllowableValueField(apiProperty, minimum.value, maximum.value, minimum.inclusive, maximum.inclusive);
        }
    }

    private static Extremum getMinimum(XSSimpleType xsSimpleType) {
        boolean inclusive = false;
        String minimum = null;
        String minInclusive = getFacetAsString(xsSimpleType, XSFacet.FACET_MININCLUSIVE);
        String minExclusive = getFacetAsString(xsSimpleType, XSFacet.FACET_MINEXCLUSIVE);
        if (Objects.nonNull(minInclusive)) {
            minimum = minInclusive;
            inclusive = true;
        } else if (Objects.nonNull(minExclusive)) {
            minimum = minExclusive;
        }
        return new Extremum(minimum, inclusive);
    }

    private static Extremum getMaximum(XSSimpleType xsSimpleType) {
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

    private static void populateAllowableValueField(JAnnotationUse apiProperty, Object minLength, Object maxLength, boolean inclusiveMin, boolean inclusiveMax) {
        StringBuilder stringBuilder = new StringBuilder("range");
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
        stringBuilder.append(", ");
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
        apiProperty.param(ApiModelPropertyFields.ALLOWABLE_VALUES, stringBuilder.toString());
    }

    private static Integer getFacetAsInteger(XSSimpleType xsSimpleType, String facetName) {
        String stringValue = getFacetAsString(xsSimpleType, facetName);
        if (stringValue != null) {
            try {
                return Integer.valueOf(stringValue);
            } catch (NumberFormatException e) {
                log.warning(String.format("Could not obtain facet : [%s]=[%s] as Integer from SimpleType:[%s]!", facetName, stringValue, xsSimpleType));
                return null;
            }
        }
        return null;
    }

    private static String getFacetAsString(XSSimpleType xsSimpleType, String facetName) {
        XSFacet facet = xsSimpleType.getFacet(facetName);
        String value = null;
        if (facet != null) {
            value = facet.getValue().value;
        }
        return value;
    }

    private static class Extremum {

        private final String value;

        private final boolean inclusive;

        public Extremum(String value, boolean inclusive) {
            this.value = value;
            this.inclusive = inclusive;
        }

    }

    private AllowableValuesProcessUtil() {
    }
}

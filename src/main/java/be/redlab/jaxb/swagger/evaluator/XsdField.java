package be.redlab.jaxb.swagger.evaluator;

import com.sun.codemodel.JAnnotationUse;

import java.util.List;
import java.util.Objects;

public class XsdField {

    private int position;

    private Integer length;

    private Integer minLength;

    private Integer maxLength;

    private Extremum minimum;

    private Extremum maximum;

    private boolean required;

    private String defaultValue;

    private List<String> fixedValues;

    private JAnnotationUse apiModelProperty;

    private boolean getMethod;

    public static XsdFieldBuilder builder() {
        return new XsdFieldBuilder();
    }

    public boolean hasOnlyLength() {
        return Objects.nonNull(length);
    }

    public boolean hasNoFieldValues() {
        return Objects.isNull(length) && Objects.isNull(minLength) && Objects.isNull(maxLength);
    }

    /**
     * Inner class to represent an Extremum object with a value and a flag which indicates if it is an inclusive or exclusive range.
     */
    public static class Extremum {

        private final String value;

        private final boolean inclusive;

        public Extremum(String value, boolean inclusive) {
            this.value = value;
            this.inclusive = inclusive;
        }

        public String getValue() {
            return value;
        }

        public boolean isInclusive() {
            return inclusive;
        }
    }

    public static final class XsdFieldBuilder {
        private int position;
        private Integer length;
        private Integer minLength;
        private Integer maxLength;
        private Extremum minimum;
        private Extremum maximum;
        private boolean required;
        private String defaultValue;
        private List<String> fixedValues;
        private JAnnotationUse apiModelProperty;
        private boolean getMethod;

        private XsdFieldBuilder() {
        }

        public static XsdFieldBuilder aXsdField() {
            return new XsdFieldBuilder();
        }

        public XsdFieldBuilder position(int position) {
            this.position = position;
            return this;
        }

        public XsdFieldBuilder length(Integer length) {
            this.length = length;
            return this;
        }

        public XsdFieldBuilder minLength(Integer minLength) {
            this.minLength = minLength;
            return this;
        }

        public XsdFieldBuilder maxLength(Integer maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public XsdFieldBuilder minimum(Extremum minimum) {
            this.minimum = minimum;
            return this;
        }

        public XsdFieldBuilder maximum(Extremum maximum) {
            this.maximum = maximum;
            return this;
        }

        public XsdFieldBuilder required(boolean required) {
            this.required = required;
            return this;
        }

        public XsdFieldBuilder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public XsdFieldBuilder fixedValues(List<String> fixedValues) {
            this.fixedValues = fixedValues;
            return this;
        }

        public XsdFieldBuilder apiModelProperty(JAnnotationUse apiModelProperty) {
            this.apiModelProperty = apiModelProperty;
            return this;
        }

        public XsdFieldBuilder getMethod(boolean getMethod) {
            this.getMethod = getMethod;
            return this;
        }

        public XsdField build() {
            XsdField xsdField = new XsdField();
            xsdField.minLength = this.minLength;
            xsdField.maximum = this.maximum;
            xsdField.required = this.required;
            xsdField.fixedValues = this.fixedValues;
            xsdField.getMethod = this.getMethod;
            xsdField.defaultValue = this.defaultValue;
            xsdField.apiModelProperty = this.apiModelProperty;
            xsdField.maxLength = this.maxLength;
            xsdField.position = this.position;
            xsdField.length = this.length;
            xsdField.minimum = this.minimum;
            return xsdField;
        }
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Extremum getMinimum() {
        return minimum;
    }

    public void setMinimum(Extremum minimum) {
        this.minimum = minimum;
    }

    public Extremum getMaximum() {
        return maximum;
    }

    public void setMaximum(Extremum maximum) {
        this.maximum = maximum;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<String> getFixedValues() {
        return fixedValues;
    }

    public void setFixedValues(List<String> fixedValues) {
        this.fixedValues = fixedValues;
    }

    public JAnnotationUse getApiModelProperty() {
        return apiModelProperty;
    }

    public void setApiModelProperty(JAnnotationUse apiModelProperty) {
        this.apiModelProperty = apiModelProperty;
    }

    public boolean isGetMethod() {
        return getMethod;
    }

    public void setGetMethod(boolean getMethod) {
        this.getMethod = getMethod;
    }
}

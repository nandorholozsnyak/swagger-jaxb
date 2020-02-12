package be.redlab.jaxb.swagger.process;

import be.redlab.jaxb.swagger.constants.ApiModelPropertyFields;
import com.sun.codemodel.JAnnotationUse;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * @author nandor.holozsnyak
 */
public final class ExampleProcessUtil {

    /**
     * Fills up {@link ApiModelProperty#example()} ()} field with the values of the simple type.
     *
     * @param apiProperty annotation to populate.
     */
    public static void addExample(JAnnotationUse apiProperty, String defaultValue) {
        if (Objects.nonNull(defaultValue)) {
            apiProperty.param(ApiModelPropertyFields.EXAMPLE, defaultValue);
        }
    }

    private ExampleProcessUtil() {
    }

}

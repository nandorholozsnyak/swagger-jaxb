package be.redlab.jaxb.swagger.process;

import be.redlab.jaxb.swagger.constants.ApiModelPropertyFields;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.outline.EnumConstantOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author nandor.holozsnyak
 * @since 1.0
 */
public final class EnumUtil {

    /**
     * Fills up {@link ApiModelProperty#allowableValues()} field with enum values.
     *
     * @param method      method which returns Enum instance.
     * @param enums       available enum values.
     * @param apiProperty model to be populated.
     * @since 1.0
     */
    public static void populateAllowableValuesWithKnownEnums(JMethod method, Collection<EnumOutline> enums, JAnnotationUse apiProperty) {
        EnumOutline eo = getKnownEnum(method.type().fullName(), enums);
        Optional.ofNullable(eo)
            .ifPresent(enumOutline -> addAllowableValues(enumOutline, apiProperty));
    }

    private static EnumOutline getKnownEnum(String clazz, Collection<EnumOutline> enums) {
        return enums.stream()
            .filter(enumOutline -> enumOutline.clazz.fullName().equals(clazz))
            .findFirst()
            .orElse(null);
    }

    private static void addAllowableValues(EnumOutline enumOutline, JAnnotationUse apiProperty) {
        List<EnumConstantOutline> constants = enumOutline.constants;
        int size = constants.size();
        int classNameLength = enumOutline.clazz.fullName().length() + 1;
        StringBuilder builder = createAndPopulateStringBuilderWithEnumValues(constants, size, classNameLength);
        apiProperty.param(ApiModelPropertyFields.ALLOWABLE_VALUES, builder.toString());
    }

    private static StringBuilder createAndPopulateStringBuilderWithEnumValues(List<EnumConstantOutline> constants, int size, int classNameLength) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            builder.append(constants.get(i).constRef.getName().substring(classNameLength));
            if (i < size - 1) {
                builder.append(", ");
            }
        }
        return builder;
    }

    private EnumUtil() {
    }
}

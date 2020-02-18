package be.redlab.jaxb.swagger.process;

import be.redlab.jaxb.swagger.constants.ApiModelPropertyFields;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CEnumConstant;
import com.sun.tools.xjc.outline.EnumConstantOutline;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnumUtilTest {

    @Mock
    private JType jType;

    @Mock
    private JMethod jMethod;

    @Mock
    private JAnnotationUse apiProperty;

    @Test
    void populateAllowableValuesWithKnownEnumsShouldPopulateAllowableValuesWithEnumConstantsWhenEnumHasMoreValues() {
        //Given
        JEnumConstant firstConst = mock(JEnumConstant.class);
        when(firstConst.getName()).thenReturn("x.y.TestEnum.FIRST");
        EnumConstantOutline first = new MockEnumConstantOutline(null, firstConst);

        JEnumConstant secondConst = mock(JEnumConstant.class);
        when(secondConst.getName()).thenReturn("x.y.TestEnum.SECOND");
        EnumConstantOutline second = new MockEnumConstantOutline(null, secondConst);

        JDefinedClass jDefinedClass = mock(JDefinedClass.class);
        when(jDefinedClass.fullName()).thenReturn("x.y.TestEnum");

        EnumOutline enumOutline = new MockEnumOutline(Arrays.asList(first, second), jDefinedClass);
        when(jMethod.type()).thenReturn(jType);
        when(jType.fullName()).thenReturn("x.y.TestEnum");

        //When
        EnumUtil.populateAllowableValuesWithKnownEnums(jMethod, Arrays.asList(enumOutline), apiProperty);

        //Then
        verify(apiProperty).param(ApiModelPropertyFields.ALLOWABLE_VALUES, "FIRST, SECOND");
    }

    @Test
    void populateAllowableValuesWithKnownEnumsShouldPopulateAllowableValuesWithEnumConstantsWhenEnumHasOneValue() {
        //Given
        JEnumConstant firstConst = mock(JEnumConstant.class);
        when(firstConst.getName()).thenReturn("x.y.TestEnum.FIRST");
        EnumConstantOutline first = new MockEnumConstantOutline(null, firstConst);

        JDefinedClass jDefinedClass = mock(JDefinedClass.class);
        when(jDefinedClass.fullName()).thenReturn("x.y.TestEnum");

        EnumOutline enumOutline = new MockEnumOutline(Arrays.asList(first), jDefinedClass);
        when(jMethod.type()).thenReturn(jType);
        when(jType.fullName()).thenReturn("x.y.TestEnum");

        //When
        EnumUtil.populateAllowableValuesWithKnownEnums(jMethod, Arrays.asList(enumOutline), apiProperty);

        //Then
        verify(apiProperty).param(ApiModelPropertyFields.ALLOWABLE_VALUES, "FIRST");
    }

    class MockEnumConstantOutline extends EnumConstantOutline {

        protected MockEnumConstantOutline(CEnumConstant target, JEnumConstant constRef) {
            super(target, constRef);
        }
    }

    class MockEnumOutline extends EnumOutline {

        protected MockEnumOutline(List<EnumConstantOutline> constants, JDefinedClass clazz) {
            super(null, clazz);
            this.constants.addAll(constants);
        }

        @Override
        public Outline parent() {
            return null;
        }
    }
}

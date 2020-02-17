package be.redlab.jaxb.swagger.process;

import be.redlab.jaxb.swagger.constants.ApiModelPropertyFields;
import com.sun.codemodel.JAnnotationUse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ExampleProcessUtilTest {

    @Mock
    private JAnnotationUse apiProperty;

    @Test
    void addExampleShouldPopulateExampleValueFieldWhenIncomingExampleValueIsNotNull() {
        //Given

        //When
        ExampleProcessUtil.addExample(apiProperty, "test-example-value");

        //Then
        verify(apiProperty).param(ApiModelPropertyFields.EXAMPLE, "test-example-value");
    }

    @Test
    void addExampleShouldNotPopulateExampleValueFieldWhenIncomingExampleValueIsNull() {
        //Given

        //When
        ExampleProcessUtil.addExample(apiProperty, null);

        //Then
        verifyNoInteractions(apiProperty);
    }
}

package be.redlab.jaxb.swagger.process;

import be.redlab.jaxb.swagger.constants.ApiModelPropertyFields;
import com.sun.codemodel.JAnnotationUse;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XmlString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllowableValuesProcessUtilTest {

    @Mock
    private XSFacet xsFacet;

    @Mock
    private XSFacet xsFacet2;

    @Mock
    private XSSimpleType xsSimpleType;

    @Mock
    private JAnnotationUse apiProperty;

    @Test
    void addLengthShouldPopulateAllowableValuesWhenLengthIsGiven() {
        //Given
        when(xsSimpleType.getFacet(XSFacet.FACET_LENGTH)).thenReturn(xsFacet);
        when(xsFacet.getValue()).thenReturn(new XmlString("512"));

        //When
        AllowableValuesProcessUtil.addLength(apiProperty, xsSimpleType);

        //Then
        verify(apiProperty).param(ApiModelPropertyFields.ALLOWABLE_VALUES, "range[512, 512]");

    }

    @Test
    void addLengthShouldPopulateAllowableValuesWhenMinAndMaxLengthsAreGiven() {
        //Given
        when(xsSimpleType.getFacet(XSFacet.FACET_LENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXLENGTH)).thenReturn(xsFacet);
        when(xsSimpleType.getFacet(XSFacet.FACET_MINLENGTH)).thenReturn(xsFacet2);
        when(xsFacet.getValue()).thenReturn(new XmlString("512"));
        when(xsFacet2.getValue()).thenReturn(new XmlString("256"));

        //When
        AllowableValuesProcessUtil.addLength(apiProperty, xsSimpleType);

        //Then
        verify(apiProperty).param(ApiModelPropertyFields.ALLOWABLE_VALUES, "range[256, 512]");

    }

    @Test
    void addExtremumShouldNotAddExtremumWhenLengthsAreGiven() {
        //Given
        when(xsSimpleType.getFacet(XSFacet.FACET_LENGTH)).thenReturn(xsFacet);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXLENGTH)).thenReturn(xsFacet);
        when(xsSimpleType.getFacet(XSFacet.FACET_MINLENGTH)).thenReturn(xsFacet);
        when(xsFacet.getValue()).thenReturn(new XmlString("512"));

        //When
        AllowableValuesProcessUtil.addExtremum(apiProperty, xsSimpleType);

        //Then
        verifyNoInteractions(apiProperty);
    }

    @Test
    void addExtremumShouldAddProperRangeWhenLengthsAreAllNullAndMinInclusiveAndMaxInclusive() {
        //Given
        XSFacet min = mock(XSFacet.class);
        XSFacet max = mock(XSFacet.class);
        when(xsSimpleType.getFacet(XSFacet.FACET_LENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXLENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MINLENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MININCLUSIVE)).thenReturn(min);
        when(xsSimpleType.getFacet(XSFacet.FACET_MINEXCLUSIVE)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXINCLUSIVE)).thenReturn(max);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXEXCLUSIVE)).thenReturn(null);
        when(min.getValue()).thenReturn(new XmlString("256.25"));
        when(max.getValue()).thenReturn(new XmlString("512.5"));

        //When
        AllowableValuesProcessUtil.addExtremum(apiProperty, xsSimpleType);

        //Then
        verify(apiProperty).param(ApiModelPropertyFields.ALLOWABLE_VALUES, "range[256.25, 512.5]");
    }

    @Test
    void addExtremumShouldAddProperRangeWhenLengthsAreAllNullAndMinExclusiveAndMaxInclusive() {
        //Given
        XSFacet min = mock(XSFacet.class);
        XSFacet max = mock(XSFacet.class);
        when(xsSimpleType.getFacet(XSFacet.FACET_LENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXLENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MINLENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MININCLUSIVE)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MINEXCLUSIVE)).thenReturn(min);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXINCLUSIVE)).thenReturn(max);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXEXCLUSIVE)).thenReturn(null);
        when(min.getValue()).thenReturn(new XmlString("256.25"));
        when(max.getValue()).thenReturn(new XmlString("512.5"));

        //When
        AllowableValuesProcessUtil.addExtremum(apiProperty, xsSimpleType);

        //Then
        verify(apiProperty).param(ApiModelPropertyFields.ALLOWABLE_VALUES, "range(256.25, 512.5]");
    }

    @Test
    void addExtremumShouldAddProperRangeWhenLengthsAreAllNullAndMinExclusiveAndMaxExclusive() {
        //Given
        XSFacet min = mock(XSFacet.class);
        XSFacet max = mock(XSFacet.class);
        when(xsSimpleType.getFacet(XSFacet.FACET_LENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXLENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MINLENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MININCLUSIVE)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MINEXCLUSIVE)).thenReturn(min);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXINCLUSIVE)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXEXCLUSIVE)).thenReturn(max);
        when(min.getValue()).thenReturn(new XmlString("256.25"));
        when(max.getValue()).thenReturn(new XmlString("512.5"));

        //When
        AllowableValuesProcessUtil.addExtremum(apiProperty, xsSimpleType);

        //Then
        verify(apiProperty).param(ApiModelPropertyFields.ALLOWABLE_VALUES, "range(256.25, 512.5)");
    }
    @Test
    void addExtremumShouldAddProperRangeWhenLengthsAreAllNullAndMinInclusiveAndMaxExclusive() {
        //Given
        XSFacet min = mock(XSFacet.class);
        XSFacet max = mock(XSFacet.class);
        when(xsSimpleType.getFacet(XSFacet.FACET_LENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXLENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MINLENGTH)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MININCLUSIVE)).thenReturn(min);
        when(xsSimpleType.getFacet(XSFacet.FACET_MINEXCLUSIVE)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXINCLUSIVE)).thenReturn(null);
        when(xsSimpleType.getFacet(XSFacet.FACET_MAXEXCLUSIVE)).thenReturn(max);
        when(min.getValue()).thenReturn(new XmlString("256.25"));
        when(max.getValue()).thenReturn(new XmlString("512.5"));

        //When
        AllowableValuesProcessUtil.addExtremum(apiProperty, xsSimpleType);

        //Then
        verify(apiProperty).param(ApiModelPropertyFields.ALLOWABLE_VALUES, "range[256.25, 512.5)");
    }
}

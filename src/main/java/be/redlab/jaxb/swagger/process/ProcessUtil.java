/*
 *  Copyright 2017 Balder Van Camp
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package be.redlab.jaxb.swagger.process;

import be.redlab.jaxb.swagger.XJCHelper;
import be.redlab.jaxb.swagger.constants.ApiModelPropertyFields;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.Objects;

/**
 * @author redlab
 */
public class ProcessUtil {

    private static final String IS = "is";

    private static final String GET = "get";

    private static final ProcessUtil myself = new ProcessUtil();

    private ProcessUtil() {
    }

    public static ProcessUtil getInstance() {
        return myself;
    }

    /**
     * @param mods
     * @return
     */
    public boolean validFieldMods(final int mods) {
        return (mods & JMod.FINAL) == 0 && (mods & JMod.STATIC) == 0
            && (mods & JMod.ABSTRACT) == 0 && (mods & JMod.NATIVE) == 0 && (mods & JMod.TRANSIENT) == 0
            && (mods & JMod.VOLATILE) == 0;

    }

    /**
     * @param implClass
     * @param targetClass
     * @param jFieldVar
     * @param enums
     */
    public void addMethodAnnotationForField(final JDefinedClass implClass, CClassInfo targetClass, final JFieldVar jFieldVar, final Collection<EnumOutline> enums) {
        JMethod jm = getCorrespondingMethod(implClass, jFieldVar.name());
        if (null != jm) {
            addMethodAnnotation(implClass, targetClass, jm, isRequired(jFieldVar), getDefault(jFieldVar), enums);
        }
    }

    /**
     * @param jFieldVar
     * @return
     */
    public String getDefault(final JFieldVar jFieldVar) {
        JAnnotationUse annotation = XJCHelper.getAnnotation(jFieldVar.annotations(), XmlElement.class);
        if (null != annotation) {
            return XJCHelper.getStringValueFromAnnotationMember(annotation, "defaultValue");
        }
        return null;
    }

    /**
     * @param jFieldVar
     * @return
     */
    public boolean isRequired(final JFieldVar jFieldVar) {
        return jFieldVar.type().isPrimitive()
            || isRequiredByAnnotation(XJCHelper.getAnnotation(jFieldVar.annotations(), XmlElement.class));
    }

    /**
     * @param annotation
     * @return
     */
    public boolean isRequiredByAnnotation(final JAnnotationUse annotation) {
        return null != annotation && "true".equalsIgnoreCase(XJCHelper.getStringValueFromAnnotationMember(annotation, ApiModelPropertyFields.REQUIRED));
    }

    /**
     * @param implClass
     * @param key
     * @return
     */
    public JMethod getCorrespondingMethod(final JDefinedClass implClass, final String key) {
        StringBuilder b = new StringBuilder(key.substring(0, 1).toUpperCase());
        if (key.length() > 1) {
            b.append(key.substring(1));
        }
        String get = GET + b.toString();
        String is = IS + b.toString();
        for (JMethod m : implClass.methods()) {
            if (get.equals(m.name()) || is.equals(m.name())) {
                return m;
            }
        }
        return null;
    }

    /**
     * @param mods
     * @return
     */
    public boolean validMethodMods(final int mods) {
        return ((mods & JMod.PROTECTED) == 0 && (mods & JMod.PRIVATE) == 0 && (mods & JMod.FINAL) == 0 && (mods & JMod.STATIC) == 0
            && (mods & JMod.ABSTRACT) == 0 && (mods & JMod.NATIVE) == 0 && (mods & JMod.TRANSIENT) == 0 && (mods & JMod.VOLATILE) == 0);
    }

    /**
     * Add method level annotation {@link ApiModelProperty} if not already on the method
     *
     * @param o            the ClassOutline
     * @param t            the TargetClass
     * @param m            the method to add annotation on
     * @param required
     * @param defaultValue
     * @param enums
     */
    public void addMethodAnnotation(final JDefinedClass o, CClassInfo t, final JMethod m, final boolean required, final String defaultValue,
                                    final Collection<EnumOutline> enums) {
        if (null == XJCHelper.getAnnotation(m.annotations(), ApiModelProperty.class)) {
            if (isValidMethod(m, GET)) {
                internalAddMethodAnnotation(o, t, m, GET, required, defaultValue, enums);
            } else if (isValidMethod(m, IS)) {
                internalAddMethodAnnotation(o, t, m, IS, required, defaultValue, enums);
            }
        }
    }

    /**
     * @param implClass
     * @param targetClass
     * @param method
     * @param prefix
     * @param required
     * @param defaultValue
     * @param enums
     */
    protected void internalAddMethodAnnotation(final JDefinedClass implClass, CClassInfo targetClass,
                                               final JMethod method, final String prefix,
                                               final boolean required,
                                               final String defaultValue, final Collection<EnumOutline> enums) {
        JAnnotationUse apiProperty = method.annotate(ApiModelProperty.class);
        String name = prepareNameFromMethod(method.name(), prefix);
        populateDescription(targetClass, apiProperty, name);
        populateRequired(required, apiProperty);
        ExampleProcessUtil.addExample(apiProperty, defaultValue);
        if (Objects.nonNull(enums) && !enums.isEmpty()) {
            populateAllowableValuesWithKnownEnums(method, enums, apiProperty);
        } else {
            populateAllowableValuesWithMetadata(apiProperty, targetClass, name);
        }
    }

    protected void populateDescription(CClassInfo targetClass, JAnnotationUse apiProperty, String name) {
        String description = getDescription(targetClass, name);
        apiProperty.param(ApiModelPropertyFields.VALUE, description);
    }

    private void populateRequired(boolean required, JAnnotationUse apiProperty) {
        if (required) {
            apiProperty.param(ApiModelPropertyFields.REQUIRED, true);
        }
    }

    private void populateAllowableValuesWithKnownEnums(JMethod m, Collection<EnumOutline> enums, JAnnotationUse apiProperty) {
        EnumUtil.populateAllowableValuesWithKnownEnums(m, enums, apiProperty);
    }

    /**
     * Extract value from {@code <xs:annotation><xs:documentation>} for property if exists.
     *
     * @param targetClass  the TargetClass
     * @param propertyName property name
     * @return value from {@code <xs:annotation><xs:documentation>} or <code>null</code> if
     * {@code <xs:annotation><xs:documentation>} does not exists.
     */
    private String getDescription(CClassInfo targetClass, String propertyName) {
        CPropertyInfo property = targetClass.getProperty(propertyName);
        String description = propertyName;
        XSComponent schemaComponent = property.getSchemaComponent();
        if (schemaComponent instanceof XSParticle) {
            XSAnnotation annotation = ((XSParticle) schemaComponent).getTerm().getAnnotation();
            if (annotation != null) {
                Object annotationObj = annotation.getAnnotation();
                if (annotationObj instanceof BindInfo) {
                    description = ((BindInfo) annotationObj).getDocumentation();
                    description = description.replaceAll("\\t\\r\\n", "");
                }
            }
        }
        return description;
    }

    /**
     * Checks that the given method is valid, meaning that it starts with the given prefix and the prefix is not the
     * name of the method
     *
     * @param m      the method
     * @param prefix the prefix
     * @return true if valid, false otherwise
     */
    public boolean isValidMethod(final JMethod m, final String prefix) {
        return m.name().length() > prefix.length() && m.name().startsWith(prefix);
    }

    /**
     * Create the name for in a {@link ApiModelProperty#value()}
     *
     * @param getterName the name of a getter
     * @param prefix
     * @return the name without get and with first character set to lowerCase
     */
    public String prepareNameFromMethod(final String getterName, final String prefix) {
        String name = getterName.substring(prefix.length());
        StringBuilder b = new StringBuilder();
        b.append(Character.toLowerCase(name.charAt(0)));
        if (name.length() > 1) {
            b.append(name.substring(1));
        }
        return b.toString();
    }

    /**
     * Adds <xs:restriction>-s to {@link ApiModelProperty}
     *
     * @param apiProperty
     * @param targetClass
     * @param propertyName
     */
    private void populateAllowableValuesWithMetadata(JAnnotationUse apiProperty, CClassInfo targetClass, String propertyName) {
        CPropertyInfo property = targetClass.getProperty(propertyName);
        XSComponent schemaComponent = property.getSchemaComponent();
        if (schemaComponent instanceof XSParticle) {
            XSParticle particle = (XSParticle) schemaComponent;
            XSTerm xsTerm = particle.getTerm();
            if (isXSElementDeclAndHasSimpleType(xsTerm)) {
                XSElementDecl xsElementDecl = (XSElementDecl) xsTerm;
                XSSimpleType xsSimpleType = xsElementDecl.getType().asSimpleType();
                AllowableValuesProcessUtil.addLength(apiProperty, xsSimpleType);
                AllowableValuesProcessUtil.addExtremum(apiProperty, xsSimpleType);
            }
        }
    }

    private boolean isXSElementDeclAndHasSimpleType(XSTerm xsTerm) {
        return xsTerm instanceof XSElementDecl
            && Objects.nonNull(((XSElementDecl) xsTerm).getType())
            && Objects.nonNull(((XSElementDecl) xsTerm).getType().asSimpleType());
    }

}

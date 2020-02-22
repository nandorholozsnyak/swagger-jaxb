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

package be.redlab.jaxb.swagger;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JFormatter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author redlab
 */
public final class XJCHelper {

    private static final String VALUE = "value";

    private static final Map<String, XmlAccessType> ACCESS_TYPE_MAP = new HashMap<>();

    static {
        ACCESS_TYPE_MAP.put("javax.xml.bind.annotation.XmlAccessType.FIELD", XmlAccessType.FIELD);
        ACCESS_TYPE_MAP.put("javax.xml.bind.annotation.XmlAccessType.PROPERTY", XmlAccessType.PROPERTY);
        ACCESS_TYPE_MAP.put("javax.xml.bind.annotation.XmlAccessType.PUBLIC_MEMBER", XmlAccessType.PUBLIC_MEMBER);
        ACCESS_TYPE_MAP.put("javax.xml.bind.annotation.XmlAccessType.NONE", XmlAccessType.NONE);
    }

    private XJCHelper() {
    }

    /**
     * @param annotations the annotations to search in for {@link XmlAccessorType}
     * @return {@link XmlAccessType} if valid is found, null otherwise
     */
    public static XmlAccessType getAccessType(final Collection<JAnnotationUse> annotations) {
        JAnnotationUse annotation = getAnnotation(annotations, XmlAccessorType.class);
        String value = getStringValueFromAnnotationMember(annotation, VALUE);
        return ACCESS_TYPE_MAP.get(value);
    }


    /**
     * Searches for the given class in the JAnnotationUse collection
     *
     * @param annotations collection of annotations to search in
     * @param annotation  the annotation class to search for
     * @return the annotation or null if not found
     */
    public static JAnnotationUse getAnnotation(final Collection<JAnnotationUse> annotations, final Class<?> annotation) {
        String name = annotation.getName();
        return annotations.stream()
            .filter(jAnnotationUse -> name.equals(jAnnotationUse.getAnnotationClass().fullName()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Returns a Stringyfied version of the value of an annotation member
     *
     * @param jAnnotationUse the annotation
     * @param member         the member to fetch
     * @return the String value of the member or null of not found
     */
    public static String getStringValueFromAnnotationMember(final JAnnotationUse jAnnotationUse, final String member) {
        return Optional.ofNullable(jAnnotationUse)
            .map(JAnnotationUse::getAnnotationMembers)
            .map(jAnnotationValue -> jAnnotationValue.get(member))
            .map(XJCHelper::writeAnnotationToString)
            .orElse(null);
    }

    private static String writeAnnotationToString(JAnnotationValue it) {
        StringWriter stringWriter = new StringWriter();
        JFormatter jFormatter = new JFormatter(stringWriter);
        it.generate(jFormatter);
        return stringWriter.toString();
    }
}

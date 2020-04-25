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

import be.redlab.jaxb.swagger.ProcessStrategy;
import be.redlab.jaxb.swagger.XJCHelper;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.outline.EnumOutline;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractProcessStrategy implements ProcessStrategy {

    protected final ProcessUtil processUtil;

    public AbstractProcessStrategy() {
        this.processUtil = ProcessUtil.getInstance();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * be.redlab.jaxb.swagger.ProcessStrategy#process(com.sun.codemodel.JDefinedClass)
     */
    public final void process(final JDefinedClass implClass, CClassInfo targetClass, final Collection<EnumOutline> enums) {
        Collection<JMethod> methods = implClass.methods();
        processMethods(implClass, targetClass, methods, enums);
        Map<String, JFieldVar> fields = implClass.fields();
        processFields(implClass, targetClass, fields, enums);
    }

    public abstract boolean isValidForFieldProcess(JFieldVar jFieldVar);

    public abstract boolean isValidForMethodProcess(JMethod jMethod);

    private void processFields(JDefinedClass implClass, CClassInfo targetClass, Map<String, JFieldVar> fields, Collection<EnumOutline> enums) {
        if (Objects.nonNull(fields) && !fields.isEmpty()) {
            int position = getLastPosition(implClass);
            for (Map.Entry<String, JFieldVar> e : fields.entrySet()) {
                JFieldVar jFieldVar = e.getValue();
                if (isValidForFieldProcess(jFieldVar)) {
                    processUtil.addMethodAnnotationForField(implClass, targetClass, jFieldVar, enums, position++);
                }
            }
        }
    }

    private void processMethods(JDefinedClass implClass, CClassInfo targetClass, Collection<JMethod> methods, Collection<EnumOutline> enums) {
        if (Objects.nonNull(methods) && !methods.isEmpty()) {
            int position = getLastPosition(implClass);
            for (JMethod method : methods) {
                if (isValidForMethodProcess(method)) {
                    JAnnotationUse xmlElementAnnotation = XJCHelper.getAnnotation(method.annotations(), XmlElement.class);
                    processUtil.addMethodAnnotation(implClass, targetClass, method, processUtil.isRequiredByAnnotation(xmlElementAnnotation), null, enums, position++);
                }
            }
        }
    }

    private int getLastPosition(JDefinedClass implClass) {
        if (Objects.nonNull(implClass)) {
            int base = 0;
            JClass superClass = implClass._extends();
            if (Objects.nonNull(superClass) && superClass instanceof JDefinedClass) {
                base = getLastPosition((JDefinedClass) superClass);
            }
            return base + getLastPositionOfClass(implClass);
        }
        return 0;
    }

    private int getLastPositionOfClass(JDefinedClass implClass) {
        Collection<JMethod> methods = implClass.methods();
        return (int) methods.stream()
            .flatMap(jMethod -> jMethod.annotations().stream())
            .map(JAnnotationUse::getAnnotationClass)
            .map(JType::fullName)
            .filter(this::isApiModelProperty)
            .count();
    }

    private boolean isApiModelProperty(String fullName) {
        return fullName.equals(ApiModelProperty.class.getName());
    }
}

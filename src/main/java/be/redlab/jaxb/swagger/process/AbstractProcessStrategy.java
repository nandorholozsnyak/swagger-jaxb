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
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.outline.EnumOutline;

import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

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
        Map<String, JFieldVar> fields = implClass.fields();
        doProcess(implClass, targetClass, methods, fields, enums);
    }

    protected void doProcess(JDefinedClass implClass, CClassInfo targetClass, Collection<JMethod> methods, Map<String, JFieldVar> fields,
                             Collection<EnumOutline> enums) {
        processFields(implClass, targetClass, fields, enums);
        processMethods(implClass, targetClass, methods, enums);
    }

    public abstract boolean isValidForFieldProcess(JFieldVar jFieldVar);

    public abstract boolean isValidForMethodProcess(JMethod jMethod);

    private void processFields(JDefinedClass implClass, CClassInfo targetClass, Map<String, JFieldVar> fields, Collection<EnumOutline> enums) {
        if (Objects.nonNull(fields) && !fields.isEmpty()) {
            int position = 0;
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
            JMethod[] jMethods = methods.toArray(new JMethod[0]);
            IntStream.range(0, methods.size())
                .forEach(index -> {
                    JMethod jMethod = jMethods[index];
                    if (isValidForMethodProcess(jMethod)) {
                        JAnnotationUse xmlElementAnnotation = XJCHelper.getAnnotation(jMethod.annotations(), XmlElement.class);
                        processUtil.addMethodAnnotation(implClass, targetClass, jMethod, processUtil.isRequiredByAnnotation(xmlElementAnnotation), null, enums, index);
                    }
                });
        }
    }
}

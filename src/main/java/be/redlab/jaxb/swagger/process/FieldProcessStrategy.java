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
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author redlab
 */
public final class FieldProcessStrategy extends AbstractProcessStrategy {

    @Override
    public boolean isValidForFieldProcess(JFieldVar jFieldVar) {
        int mods = jFieldVar.mods().getValue();
        JAnnotationUse xmlTransientAnnotation = XJCHelper.getAnnotation(jFieldVar.annotations(), XmlTransient.class);
        return processUtil.validFieldMods(mods) && null == xmlTransientAnnotation;
    }

    @Override
    public boolean isValidForMethodProcess(JMethod jMethod) {
        int mods = jMethod.mods().getValue();
        JAnnotationUse xmlElementAnnotation = XJCHelper.getAnnotation(jMethod.annotations(), XmlElement.class);
        return processUtil.validMethodMods(mods) && null != xmlElementAnnotation;
    }

}

/*
 * Copyright 2013 Balder Van Camp
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package be.redlab.jaxb.swagger;

import java.util.Collection;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JMethod;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

/**
 * @author redlab
 *
 */
public class SwaggerAnnotationsJaxbPlugin extends Plugin {

	private static final String SWAGGERIFY = "swaggerify";
	private static final String USAGE = "Add this plugin to the JAXB classes generator classpath and provide the argument '-swaggerify'.";

	/*
	 * (non-Javadoc)
	 *
	 * @see com.sun.tools.xjc.Plugin#getOptionName()
	 */
	@Override
	public String getOptionName() {
		return SWAGGERIFY;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.sun.tools.xjc.Plugin#getUsage()
	 */
	@Override
	public String getUsage() {
		return USAGE;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.sun.tools.xjc.Plugin#run(com.sun.tools.xjc.outline.Outline, com.sun.tools.xjc.Options,
	 * org.xml.sax.ErrorHandler)
	 *
	 * Api Annotations Info
	 * String value() default "";
	 * String allowableValues() default "";endIndex
	 * String access() default "";
	 * String notes() default "";
	 * String dataType() default "";
	 * boolean required() default false;
	 */
	@Override
	public boolean run(final Outline outline, final Options opt, final ErrorHandler errorHandler) throws SAXException {
		Collection<? extends ClassOutline> classes = outline.getClasses();
		for (ClassOutline o : classes) {
			if (o.implClass.isClass() && !o.implClass.isAbstract() && !o.implClass.isInterface()
					&& !o.implClass.isAnnotationTypeDeclaration()) {
				addClassAnnotation(o);
				for (JMethod m : o.implClass.methods()) {
					addMethodAnnotation(o, m);
				}
			} else {
				errorHandler.warning(new SAXParseException(String.format("Skipping %s as it is not an implementation or class",
						o), null));
			}
		}
		return true;
	}

	/**
	 * Add the class level annotation, {@link ApiClass}
	 *
	 * @param o the ClassOutline
	 */
	protected void addClassAnnotation(final ClassOutline o) {
		JAnnotationUse apiClass = o.implClass.annotate(ApiClass.class);
		apiClass.param("value", o.ref.name());
		apiClass.param("description", new StringBuilder(o.ref.fullName())
				.append(" description generated by jaxb-swagger, hence no class description yet.").toString());
	}

	/**
	 * Add method level annotation {@link ApiProperty}
	 *
	 * @param o the ClassOutline
	 * @param m the method to add annotation on
	 */
	protected void addMethodAnnotation(final ClassOutline o, final JMethod m) {
		if (m.name().startsWith("get") && m.name().length() > 3) {
			JAnnotationUse apiProperty = m.annotate(ApiProperty.class);
			String name = prepareNameFromGetter(m.name());
			apiProperty.param("value", name);
			String dataType = DataTypeDeterminationUtil.determineDataType(m.type());
			if (dataType != null) {
				apiProperty.param("dataType", dataType);
			}
			Collection<JAnnotationUse> fieldAnnotations = o.implClass.fields()
					.get(name.substring(0, 1).toLowerCase() + name.substring(1)).annotations();
			if (fieldAnnotations.isEmpty()) {
				fieldAnnotations = m.annotations();
			}
			for (JAnnotationUse jau : fieldAnnotations) {
				AnnotationProcessorFactory.findProcessor(jau).process(apiProperty);
			}
		}
	}

	/**
	 * Create the name for in a {@link ApiProperty#value()}
	 *
	 * @param getterName the name of a getter
	 * @return the name without get and with first character set to lowerCase
	 */
	protected String prepareNameFromGetter(final String getterName) {
		String name = getterName.substring(3);
		StringBuilder b = new StringBuilder();
		b.append(Character.toLowerCase(name.charAt(0)));
		if (name.length() > 1) {
			b.append(name.substring(1));
		}
		return b.toString();
	}
}

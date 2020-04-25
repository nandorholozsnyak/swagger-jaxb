package be.redlab.jaxb.swagger.evaluator;

import java.util.List;

/**
 *
 */
public class SwaggerAnnotationEvaluator {

    private final List<SwaggerAnnotationEvaluatorRule> swaggerAnnotationEvaluatorRules;

    /**
     * @param swaggerAnnotationEvaluatorRules
     */
    public SwaggerAnnotationEvaluator(List<SwaggerAnnotationEvaluatorRule> swaggerAnnotationEvaluatorRules) {
        this.swaggerAnnotationEvaluatorRules = swaggerAnnotationEvaluatorRules;
    }

    /**
     * @param xsdField
     */
    public void annotateFields(XsdField xsdField) {
        swaggerAnnotationEvaluatorRules.forEach(swaggerAnnotationEvaluatorRule -> swaggerAnnotationEvaluatorRule.annotate(xsdField));
    }
}

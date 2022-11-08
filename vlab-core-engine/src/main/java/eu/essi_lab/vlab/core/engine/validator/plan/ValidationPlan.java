package eu.essi_lab.vlab.core.engine.validator.plan;

import eu.essi_lab.vlab.core.datamodel.BasicValidationResponse;

/**
 * @author Mattia Santoro
 */
public interface ValidationPlan<T, R extends BasicValidationResponse> {

    R apply(T tobevalidated);
}

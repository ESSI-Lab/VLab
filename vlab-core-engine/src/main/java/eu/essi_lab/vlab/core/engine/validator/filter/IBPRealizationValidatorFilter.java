package eu.essi_lab.vlab.core.engine.validator.filter;

import eu.essi_lab.vlab.core.datamodel.BasicValidationResponse;

/**
 * @author Mattia Santoro
 */
public interface IBPRealizationValidatorFilter<T, R extends BasicValidationResponse> {

    R validate(T r);

}

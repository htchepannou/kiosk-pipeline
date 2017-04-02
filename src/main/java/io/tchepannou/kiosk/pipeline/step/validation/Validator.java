package io.tchepannou.kiosk.pipeline.step.validation;


public class Validator<T> {
    private final Iterable<Rule<T>> rules;

    public Validator(final Iterable<Rule<T>> rules) {
        this.rules = rules;
    }

    public Validation validate (final T subject){
        for (Rule rule : rules){
            final Validation validation = rule.validate(subject);
            if (!validation.isSuccess()){
                return validation;
            }
        }
        return Validation.success();
    }
}

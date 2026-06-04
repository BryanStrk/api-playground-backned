package com.bryan.apiplayground.common.exception;

public class PremiumFeatureException extends RuntimeException {

    private final String requiredPlan;
    private final String feature;

    public PremiumFeatureException(String requiredPlan, String feature) {
        super("La función '" + feature + "' requiere el plan " + requiredPlan + " de BALLDONTLIE");
        this.requiredPlan = requiredPlan;
        this.feature = feature;
    }

    public String getRequiredPlan() {
        return requiredPlan;
    }

    public String getFeature() {
        return feature;
    }
}

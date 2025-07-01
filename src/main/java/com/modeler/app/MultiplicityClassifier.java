package com.modeler.app;

import java.util.HashMap;
import java.util.Map;

/**
 * Classifies relationship types according to their multiplicity constraints.
 * Rules are provided via dependency injection so the classifier can be
 * independently tested and easily extended.
 */
public class MultiplicityClassifier {
    private final Map<String, Multiplicity> rules;

    /**
     * Create a classifier with the provided mapping of relationship type
     * to multiplicity.
     */
    public MultiplicityClassifier(Map<String, Multiplicity> rules) {
        this.rules = new HashMap<>(rules);
    }

    /**
     * Determine the multiplicity for the given relationship type string.
     * Unknown types default to {@link Multiplicity#ONE_TO_MANY}.
     */
    public Multiplicity classify(String relationType) {
        return rules.getOrDefault(relationType, Multiplicity.ONE_TO_MANY);
    }

    /**
     * Convenience method for classifying a {@link Claim} instance.
     */
    public Multiplicity classify(Claim claim) {
        return classify(claim.type);
    }

    /**
     * Default rule set used by the application. Additional types can be
     * configured by supplying a custom map when constructing this class.
     */
    public static Map<String, Multiplicity> defaultRules() {
        Map<String, Multiplicity> map = new HashMap<>();
        map.put("calls", Multiplicity.ONE_TO_MANY);
        map.put("default_db", Multiplicity.ONE_TO_ONE);
        return map;
    }
}


package com.modeler.app;

/**
 * Enumerates multiplicity constraints for relationship types.
 */
public enum Multiplicity {
    /** Only one target is allowed for the relationship. */
    ONE_TO_ONE,
    /** Multiple targets may exist for the relationship. */
    ONE_TO_MANY
}


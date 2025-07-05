package com.modeler.app.domain;

/**
 * Enumerates dependency relationship types.
 */
public enum DependencyType {
    CALLS,
    DEFAULT_DB,
    MESSAGE_QUEUE,
    HTTP
}

package ca.yorku.smartbudget.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ValidationException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public ValidationException(Map<String, String> fieldErrors) {
        super(buildMessage(fieldErrors));
        this.fieldErrors = Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors));
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public boolean hasErrors() {
        return !fieldErrors.isEmpty();
    }

    /** Single message for UI (e.g. modal error label). Replaces duplicate buildErrorMessage in modals. */
    public String getDisplayMessage() {
        if (fieldErrors == null || fieldErrors.isEmpty()) return "Validation failed.";
        return String.join("; ", fieldErrors.values());
    }

    private static String buildMessage(Map<String, String> errors) {
        if (errors == null || errors.isEmpty()) return "Validation failed.";
        return "Validation failed: " + String.join("; ", errors.values());
    }
}

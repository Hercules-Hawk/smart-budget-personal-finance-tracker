package ca.yorku.smartbudget.domain;

/**
 * Budget status for alerts. Matches Diagram B (OverspendingAlert).
 */
public enum AlertStatus {
    OK,
    REACHED,
    EXCEEDED
}

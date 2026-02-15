package ca.yorku.smartbudget.domain;

import java.util.List;

/**
 * Full report for a period. Matches Diagram C (ReportService.generateReport).
 */
public class ReportResult {
    private final PeriodRange range;
    private final SummaryReport summary;
    private final List<CategoryTotal> breakdown;
    private final boolean hasData;
    private final String message;

    public ReportResult(PeriodRange range, SummaryReport summary, List<CategoryTotal> breakdown, boolean hasData, String message) {
        this.range = range;
        this.summary = summary;
        this.breakdown = breakdown;
        this.hasData = hasData;
        this.message = message;
    }

    public PeriodRange getRange() {
        return range;
    }

    public SummaryReport getSummary() {
        return summary;
    }

    public List<CategoryTotal> getBreakdown() {
        return breakdown;
    }

    public boolean isHasData() {
        return hasData;
    }

    public String getMessage() {
        return message;
    }
}

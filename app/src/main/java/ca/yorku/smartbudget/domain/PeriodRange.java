package ca.yorku.smartbudget.domain;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Date range for reports. Matches Diagram C (ReportService).
 */
public class PeriodRange {
    private final LocalDate start;
    private final LocalDate end;

    public PeriodRange(LocalDate start, LocalDate end) {
        this.start = start;
        this.end = end;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public boolean isValid() {
        if (start == null && end == null) return true;
        if (start == null || end == null) return true;
        return !start.isAfter(end);
    }

    public static PeriodRange ofMonth(YearMonth month) {
        return new PeriodRange(month.atDay(1), month.atEndOfMonth());
    }

    public static PeriodRange allTime() {
        return new PeriodRange(null, null);
    }
}

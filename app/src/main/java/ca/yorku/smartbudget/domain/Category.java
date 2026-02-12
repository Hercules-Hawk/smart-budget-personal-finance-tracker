package ca.yorku.smartbudget.domain;

public enum Category {
    BILLS_AND_UTILITIES,
    EDUCATION,
    ENTERTAINMENT,
    FOOD_AND_DINING,
    HEALTHCARE,
    SHOPPING,
    TRANSPORTATION,
    FREELANCE,
    INVESTMENT,
    SALARY,
    OTHER;

    /** Display name for UI (e.g. "Food & Dining"). */
    public String getDisplayName() {
        switch (this) {
            case BILLS_AND_UTILITIES: return "Bills & Utilities";
            case FOOD_AND_DINING: return "Food & Dining";
            case TRANSPORTATION: return "Transportation";
            case ENTERTAINMENT: return "Entertainment";
            case HEALTHCARE: return "Healthcare";
            case SHOPPING: return "Shopping";
            case EDUCATION: return "Education";
            case FREELANCE: return "Freelance";
            case INVESTMENT: return "Investment";
            case SALARY: return "Salary";
            default: return name().replace('_', ' ').toLowerCase();
        }
    }
}

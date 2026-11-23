package banking.entity;

public enum AccountType {
    SAVINGS("Savings Account", "SA"),
    INVESTMENT("Investment Account", "IA"),
    CHEQUE("Cheque Account", "CA");

    private final String displayName;
    private final String prefix;

    AccountType(String displayName, String prefix) {
        this.displayName = displayName;
        this.prefix = prefix;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }
}

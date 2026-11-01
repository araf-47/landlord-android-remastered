package com.landlord.android.feature.ledger;

public class LedgerEntry {
    public final String id;
    public final String date;
    public final boolean isIncome;
    public final String description;
    public final double amount;

    public LedgerEntry(String id, String date, boolean isIncome, String description, double amount) {
        this.id = id;
        this.date = date;
        this.isIncome = isIncome;
        this.description = description;
        this.amount = amount;
    }
}

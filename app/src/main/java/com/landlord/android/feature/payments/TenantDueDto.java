package com.landlord.android.feature.payments;

public class TenantDueDto {
    public String tenantId;
    public String tenantName;
    public String tenantPhone;
    public String unitNumber;
    public String propertyName;
    public double monthlyRent;
    public double totalDue;
    public double totalPaid;
    public String latestPeriod;
    public String paymentStatus; // "PAID", "PARTIAL", "UNPAID"
}

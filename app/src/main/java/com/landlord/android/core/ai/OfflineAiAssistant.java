package com.landlord.android.core.ai;

import java.util.Locale;

public final class OfflineAiAssistant {

    private OfflineAiAssistant() {}

    public static String getResponse(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "Hello! I am your LandLord AI Assistant. Ask me about rental yields, tenant agreements, maintenance tips, or collection strategies.";
        }

        String lower = query.toLowerCase(Locale.US);

        if (lower.contains("yield") || lower.contains("roi") || lower.contains("return")) {
            return "📈 **Rental Yield Formula**:\n"
                    + "Gross Yield = (Annual Rental Income / Property Value) × 100\n"
                    + "Net Yield = ((Annual Income - Annual Expenses) / Property Value) × 100.\n\n"
                    + "In urban centers like Dhaka, typical residential gross yields average 4% to 6.5%, while commercial properties often yield 7% to 9%.";
        }

        if (lower.contains("rent") && (lower.contains("increase") || lower.contains("raise") || lower.contains("hike"))) {
            return "📋 **Rent Increase Guidelines**:\n"
                    + "1. Check your rental agreement terms (usually 10%–15% increase every 2 years or upon renewal).\n"
                    + "2. Give at least 2 to 3 months written notice before the current term expires.\n"
                    + "3. Benchmark against similar units in your area (e.g., Dhanmondi or Gulshan) to stay competitive.";
        }

        if (lower.contains("agreement") || lower.contains("lease") || lower.contains("contract")) {
            return "📜 **Key Lease Terms to Include**:\n"
                    + "• Duration & renewal notice period (minimum 2 months).\n"
                    + "• Security deposit terms (typically 2–3 months advance rent).\n"
                    + "• Due date (usually on or before the 5th/10th of every month).\n"
                    + "• Clear assignment of utility responsibilities (Prepaid electricity vs gas/water/service charges).\n"
                    + "• Subletting restrictions and condition upon move-out.";
        }

        if (lower.contains("vacan") || lower.contains("tenant") || lower.contains("market")) {
            return "💡 **Tips to Reduce Vacancy**:\n"
                    + "1. Ensure prompt repainting and deep cleaning immediately after tenant move-out.\n"
                    + "2. Take bright, high-resolution photos of rooms, kitchen, and balconies.\n"
                    + "3. Offer transparent utility breakdown so tenants know exact total monthly costs.\n"
                    + "4. Use the in-app Marketplace feature to accept leads from prospective tenants.";
        }

        if (lower.contains("maintenance") || lower.contains("repair") || lower.contains("leak")) {
            return "🔧 **Maintenance Best Practices**:\n"
                    + "• Address plumbing leaks within 24 hours to prevent wall seepage and damage to lower floors.\n"
                    + "• Service ACs and clean compressor filters every 6 months prior to summer.\n"
                    + "• Keep a monthly emergency reserve fund (approx. 5% of monthly rental revenue) for urgent building repairs.";
        }

        if (lower.contains("tax") || lower.contains("deduct")) {
            return "🏛️ **Rental Income & Taxes**:\n"
                    + "Rental income is taxable under Income from House Property. Allowable deductions typically include municipal taxes, repair/maintenance allowance (usually 25% for residential), and mortgage interest paid on the property.";
        }

        return "🤖 **LandLord AI Advisor**:\n"
                + "I analyzed your question: \"" + query + "\".\n\n"
                + "For optimal property performance, maintain updated ledger records, issue itemized monthly utility invoices by the 1st of each month, and resolve pending maintenance requests swiftly to retain reliable, high-quality tenants.";
    }
}

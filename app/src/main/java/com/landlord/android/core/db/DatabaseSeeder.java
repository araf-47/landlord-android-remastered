package com.landlord.android.core.db;

import java.util.ArrayList;
import java.util.List;

public final class DatabaseSeeder {

    private DatabaseSeeder() {}

    public static void seed(AppDatabase db) {
        db.runInTransaction(() -> {
            clearAll(db);
            seedPropertiesAndUnits(db);
            seedTenantsAndAgreements(db);
            seedInvoicesAndPayments(db);
            seedExpenses(db);
            seedMaintenanceTickets(db);
            seedMarketplaceRequests(db);
            seedConversationsAndMessages(db);
            seedNotifications(db);
        });
    }

    public static void clearAll(AppDatabase db) {
        db.notificationDao().deleteAll();
        db.messageDao().deleteAll();
        db.conversationDao().deleteAll();
        db.marketplaceRequestDao().deleteAll();
        db.maintenanceTicketDao().deleteAll();
        db.expenseDao().deleteAll();
        db.paymentDao().deleteAll();
        db.invoiceDao().deleteAll();
        db.rentalAgreementDao().deleteAll();
        db.tenantDao().deleteAll();
        db.unitDao().deleteAll();
        db.propertyDao().deleteAll();
    }

    private static void seedPropertiesAndUnits(AppDatabase db) {
        long now = System.currentTimeMillis();

        PropertyEntity p1 = new PropertyEntity();
        p1.localId = "prop-1";
        p1.name = "Green View Apartments";
        p1.address = "Road 12, Dhanmondi";
        p1.district = "Dhaka";
        p1.area = "Dhanmondi";
        p1.propertyType = "apartment";
        p1.landlordId = 101L;
        p1.createdAt = now - 90L * 86400000L;

        PropertyEntity p2 = new PropertyEntity();
        p2.localId = "prop-2";
        p2.name = "Sunset Heights";
        p2.address = "Road 45, Gulshan-2";
        p2.district = "Dhaka";
        p2.area = "Gulshan";
        p2.propertyType = "apartment";
        p2.landlordId = 101L;
        p2.createdAt = now - 60L * 86400000L;

        PropertyEntity p3 = new PropertyEntity();
        p3.localId = "prop-3";
        p3.name = "Lake Breeze Residency";
        p3.address = "Sector 3, Uttara";
        p3.district = "Dhaka";
        p3.area = "Uttara";
        p3.propertyType = "apartment";
        p3.landlordId = 101L;
        p3.createdAt = now - 45L * 86400000L;

        db.propertyDao().insert(p1);
        db.propertyDao().insert(p2);
        db.propertyDao().insert(p3);

        List<UnitEntity> units = new ArrayList<>();

        // Prop 1 Units
        units.add(createUnit("unit-1", "prop-1", "A-101", 16000, "occupied", "apartment", false,
                "[{\"label\":\"Water\",\"amount\":400},{\"label\":\"Service Charge\",\"amount\":800}]", now));
        units.add(createUnit("unit-2", "prop-1", "A-102", 18500, "occupied", "apartment", false,
                "[{\"label\":\"Water\",\"amount\":400},{\"label\":\"Service Charge\",\"amount\":800}]", now));
        units.add(createUnit("unit-3", "prop-1", "B-201", 20000, "vacant", "apartment", false,
                "[{\"label\":\"Water\",\"amount\":500},{\"label\":\"Service Charge\",\"amount\":1000}]", now));

        // Prop 2 Units
        units.add(createUnit("unit-4", "prop-2", "G-401", 32000, "occupied", "apartment", false,
                "[{\"label\":\"Water\",\"amount\":600},{\"label\":\"Service Charge\",\"amount\":1500},{\"label\":\"Generator\",\"amount\":1000}]", now));
        units.add(createUnit("unit-5", "prop-2", "G-402", 35000, "occupied", "apartment", false,
                "[{\"label\":\"Water\",\"amount\":600},{\"label\":\"Service Charge\",\"amount\":1500},{\"label\":\"Generator\",\"amount\":1000}]", now));
        units.add(createUnit("unit-6", "prop-2", "Penthouse", 48000, "vacant", "apartment", false,
                "[{\"label\":\"Water\",\"amount\":1000},{\"label\":\"Service Charge\",\"amount\":2500}]", now));

        // Prop 3 Units
        units.add(createUnit("unit-7", "prop-3", "L-101", 14000, "occupied", "apartment", false,
                "[{\"label\":\"Water\",\"amount\":300},{\"label\":\"Service Charge\",\"amount\":500}]", now));
        units.add(createUnit("unit-8", "prop-3", "L-102", 15000, "vacant", "apartment", false,
                "[{\"label\":\"Water\",\"amount\":300},{\"label\":\"Service Charge\",\"amount\":500}]", now));

        db.unitDao().insertAll(units);
    }

    private static UnitEntity createUnit(String id, String propId, String unitNo, double rent,
                                         String status, String type, boolean adPaused,
                                         String utilitiesJson, long now) {
        UnitEntity u = new UnitEntity();
        u.localId = id;
        u.propertyId = propId;
        u.unitNumber = unitNo;
        u.rent = rent;
        u.status = status;
        u.propertyType = type;
        u.adPaused = adPaused;
        u.utilityItemsJson = utilitiesJson;
        u.createdAt = now;
        return u;
    }

    private static void seedTenantsAndAgreements(AppDatabase db) {
        long now = System.currentTimeMillis();

        List<TenantEntity> tenants = new ArrayList<>();
        tenants.add(createTenant("t-1", "Tanvir Ahmed", "+880 1712-345678", "tanvir.ahmed@gmail.com", "8293817263", "unit-1", "active", now));
        tenants.add(createTenant("t-2", "Nusrat Jahan", "+880 1819-234567", "nusrat.jahan@yahoo.com", "5938201948", "unit-2", "active", now));
        tenants.add(createTenant("t-3", "Farhan Chowdhury", "+880 1911-876543", "farhan.c@outlook.com", "3049281745", "unit-4", "active", now));
        tenants.add(createTenant("t-4", "Sadia Rahman", "+880 1622-445566", "sadia.rahman@gmail.com", "4820193856", "unit-5", "active", now));
        tenants.add(createTenant("t-5", "Kazi Mahfuz", "+880 1555-778899", "kazi.mahfuz@gmail.com", "1928374650", "unit-7", "active", now));

        db.tenantDao().insertAll(tenants);

        List<RentalAgreementEntity> agreements = new ArrayList<>();
        agreements.add(createAgreement("agr-1", "t-1", "unit-1", "2026-01-01", "1-year renewable residential lease. Rent due by 5th.", 32000, now));
        agreements.add(createAgreement("agr-2", "t-2", "unit-2", "2026-03-01", "2-year residential lease. Security deposit refundable upon inspection.", 37000, now));
        agreements.add(createAgreement("agr-3", "t-3", "unit-4", "2025-11-01", "Executive luxury lease. Generator and lift charges included.", 64000, now));
        agreements.add(createAgreement("agr-4", "t-4", "unit-5", "2026-02-15", "Family residence lease agreement. Subletting prohibited.", 70000, now));
        agreements.add(createAgreement("agr-5", "t-5", "unit-7", "2026-04-01", "Standard flat lease agreement with 2-month notice period.", 28000, now));

        db.rentalAgreementDao().insertAll(agreements);
    }

    private static TenantEntity createTenant(String id, String name, String phone, String email,
                                             String nid, String unitId, String status, long now) {
        TenantEntity t = new TenantEntity();
        t.localId = id;
        t.name = name;
        t.phone = phone;
        t.email = email;
        t.nationalId = nid;
        t.unitId = unitId;
        t.status = status;
        t.createdAt = now;
        return t;
    }

    private static RentalAgreementEntity createAgreement(String id, String tenantId, String unitId,
                                                         String startDate, String terms, double deposit, long now) {
        RentalAgreementEntity a = new RentalAgreementEntity();
        a.localId = id;
        a.tenantId = tenantId;
        a.unitId = unitId;
        a.startDate = startDate;
        a.terms = terms;
        a.deposit = deposit;
        a.createdAt = now;
        return a;
    }

    private static void seedInvoicesAndPayments(AppDatabase db) {
        long now = System.currentTimeMillis();

        List<InvoiceEntity> invoices = new ArrayList<>();

        // Past Paid invoices
        invoices.add(createInvoice("inv-1", "t-1", "unit-1", "2026-09", 16000, 1200, 0, 17200, 0, "paid", "2026-09-05", now));
        invoices.add(createInvoice("inv-2", "t-2", "unit-2", "2026-09", 18500, 1200, 0, 19700, 0, "paid", "2026-09-05", now));
        invoices.add(createInvoice("inv-3", "t-3", "unit-4", "2026-09", 32000, 3100, 0, 35100, 0, "paid", "2026-09-05", now));

        // Current Month (2026-10) invoices
        invoices.add(createInvoice("inv-4", "t-1", "unit-1", "2026-10", 16000, 1200, 0, 17200, 17200, "unpaid", "2026-10-10", now));
        invoices.add(createInvoice("inv-5", "t-2", "unit-2", "2026-10", 18500, 1200, 0, 19700, 7500, "partial", "2026-10-10", now));
        invoices.add(createInvoice("inv-6", "t-3", "unit-4", "2026-10", 32000, 3100, 0, 35100, 0, "paid", "2026-10-05", now));
        invoices.add(createInvoice("inv-7", "t-4", "unit-5", "2026-10", 35000, 3100, 0, 38100, 38100, "unpaid", "2026-10-10", now));
        invoices.add(createInvoice("inv-8", "t-5", "unit-7", "2026-10", 14000, 800, 0, 14800, 0, "paid", "2026-10-05", now));

        db.invoiceDao().insertAll(invoices);

        List<PaymentEntity> payments = new ArrayList<>();
        payments.add(createPayment("pay-1", "t-1", "inv-1", 17200, "mobile", "confirmed", "2026-09-03", "bKash: BK8291823", now));
        payments.add(createPayment("pay-2", "t-2", "inv-2", 19700, "mobile", "confirmed", "2026-09-05", "Nagad: NG5839201", now));
        payments.add(createPayment("pay-3", "t-3", "inv-3", 35100, "bank", "confirmed", "2026-09-02", "CityBank: CB938210", now));
        payments.add(createPayment("pay-4", "t-3", "inv-6", 35100, "bank", "confirmed", "2026-10-02", "CityBank: CB104928", now));
        payments.add(createPayment("pay-5", "t-2", "inv-5", 12200, "mobile", "confirmed", "2026-10-04", "bKash: BK9948271", now));
        payments.add(createPayment("pay-6", "t-5", "inv-8", 14800, "cash", "confirmed", "2026-10-05", "Cash Receipt #104", now));

        db.paymentDao().insertAll(payments);
    }

    private static InvoiceEntity createInvoice(String id, String tenantId, String unitId, String period,
                                               double rent, double utilities, double prevUnpaid,
                                               double total, double balance, String status,
                                               String dueDate, long now) {
        InvoiceEntity inv = new InvoiceEntity();
        inv.localId = id;
        inv.tenantId = tenantId;
        inv.unitId = unitId;
        inv.period = period;
        inv.rent = rent;
        inv.utilityTotal = utilities;
        inv.prevUnpaidRolled = prevUnpaid;
        inv.amount = total;
        inv.balance = balance;
        inv.status = status;
        inv.dueDate = dueDate;
        inv.createdAt = now;
        return inv;
    }

    private static PaymentEntity createPayment(String id, String tenantId, String invoiceId,
                                               double amount, String method, String status,
                                               String date, String ref, long now) {
        PaymentEntity p = new PaymentEntity();
        p.localId = id;
        p.tenantId = tenantId;
        p.invoiceId = invoiceId;
        p.amount = amount;
        p.method = method;
        p.status = status;
        p.date = date;
        p.transactionRef = ref;
        p.createdAt = now;
        return p;
    }

    private static void seedExpenses(AppDatabase db) {
        long now = System.currentTimeMillis();

        List<ExpenseEntity> expenses = new ArrayList<>();
        expenses.add(createExpense("exp-1", "prop-2", "Utility", "Generator Diesel Fuel (50 Liters)", 4500, "landlord", null, "2026-10-03", now));
        expenses.add(createExpense("exp-2", "prop-1", "Maintenance", "Water Pump Capacitor Replacement", 2800, "landlord", null, "2026-10-06", now));
        expenses.add(createExpense("exp-3", "prop-3", "Cleaning", "Common Corridor and Stairway Cleaning", 1500, "landlord", null, "2026-10-08", now));
        expenses.add(createExpense("exp-4", "prop-2", "Maintenance", "Lift Monthly Servicing & Inspection", 6000, "landlord", null, "2026-09-28", now));

        db.expenseDao().insertAll(expenses);
    }

    private static ExpenseEntity createExpense(String id, String propId, String category,
                                               String desc, double amount, String bearer,
                                               String tenantId, String date, long now) {
        ExpenseEntity e = new ExpenseEntity();
        e.localId = id;
        e.propertyId = propId;
        e.category = category;
        e.description = desc;
        e.amount = amount;
        e.bearer = bearer;
        e.tenantId = tenantId;
        e.date = date;
        e.createdAt = now;
        return e;
    }

    private static void seedMaintenanceTickets(AppDatabase db) {
        long now = System.currentTimeMillis();

        List<MaintenanceTicketEntity> tickets = new ArrayList<>();

        MaintenanceTicketEntity t1 = new MaintenanceTicketEntity();
        t1.localId = "ticket-1";
        t1.unitId = "unit-1";
        t1.tenantId = "t-1";
        t1.title = "Kitchen Sink Pipe Leakage";
        t1.description = "Water slowly dripping under kitchen cabinet, needs washer replacement.";
        t1.status = "pending";
        t1.priority = "medium";
        t1.createdAt = now - 2L * 86400000L;
        tickets.add(t1);

        MaintenanceTicketEntity t2 = new MaintenanceTicketEntity();
        t2.localId = "ticket-2";
        t2.unitId = "unit-4";
        t2.tenantId = "t-3";
        t2.title = "AC Master Bedroom Cooling Issue";
        t2.description = "Compressor trips after running 10 minutes. Technician scheduled.";
        t2.status = "in_progress";
        t2.priority = "high";
        t2.createdAt = now - 4L * 86400000L;
        tickets.add(t2);

        MaintenanceTicketEntity t3 = new MaintenanceTicketEntity();
        t3.localId = "ticket-3";
        t3.unitId = "unit-2";
        t3.tenantId = "t-2";
        t3.title = "Bathroom Geyser Repair";
        t3.description = "Heating element burnt out, replaced with new Ariston coil.";
        t3.status = "resolved";
        t3.priority = "high";
        t3.createdAt = now - 10L * 86400000L;
        tickets.add(t3);

        MaintenanceTicketEntity t4 = new MaintenanceTicketEntity();
        t4.localId = "ticket-4";
        t4.unitId = "unit-5";
        t4.tenantId = "t-4";
        t4.title = "Balcony Light Switch Broken";
        t4.description = "Switch replaced with new gang box.";
        t4.status = "resolved";
        t4.priority = "low";
        t4.createdAt = now - 15L * 86400000L;
        tickets.add(t4);

        db.maintenanceTicketDao().insertAll(tickets);
    }

    private static void seedMarketplaceRequests(AppDatabase db) {
        long now = System.currentTimeMillis();

        List<MarketplaceRequestEntity> reqs = new ArrayList<>();

        MarketplaceRequestEntity r1 = new MarketplaceRequestEntity();
        r1.localId = "req-1";
        r1.unitId = "unit-3"; // B-201
        r1.applicantName = "Mehedi Hasan";
        r1.applicantPhone = "+880 1788-112233";
        r1.message = "Looking for a 2-bedroom apartment for family of 3. Can move in next month.";
        r1.status = "pending";
        r1.createdAt = now - 86400000L;
        reqs.add(r1);

        MarketplaceRequestEntity r2 = new MarketplaceRequestEntity();
        r2.localId = "req-2";
        r2.unitId = "unit-6"; // Penthouse
        r2.applicantName = "Dr. Sabrina Islam";
        r2.applicantPhone = "+880 1999-334455";
        r2.message = "Interested in visiting this Friday afternoon for 1-year corporate lease.";
        r2.status = "pending";
        r2.createdAt = now - 2L * 86400000L;
        reqs.add(r2);

        MarketplaceRequestEntity r3 = new MarketplaceRequestEntity();
        r3.localId = "req-3";
        r3.unitId = "unit-8"; // L-102
        r3.applicantName = "Rashed Karim";
        r3.applicantPhone = "+880 1822-667788";
        r3.message = "Security deposit ready, agreement requested.";
        r3.status = "approved";
        r3.createdAt = now - 5L * 86400000L;
        reqs.add(r3);

        db.marketplaceRequestDao().insertAll(reqs);
    }

    private static void seedConversationsAndMessages(AppDatabase db) {
        long now = System.currentTimeMillis();

        ConversationEntity c1 = new ConversationEntity();
        c1.localId = "conv-1";
        c1.tenantId = "t-1";
        c1.tenantName = "Tanvir Ahmed";
        c1.lastMessage = "Plumber is scheduled for tomorrow at 11 AM.";
        c1.lastMessageTime = now - 3600000L;
        c1.unreadCount = 0;

        ConversationEntity c2 = new ConversationEntity();
        c2.localId = "conv-2";
        c2.tenantId = "t-3";
        c2.tenantName = "Farhan Chowdhury";
        c2.lastMessage = "Received payment confirmation. Thank you!";
        c2.lastMessageTime = now - 86400000L;
        c2.unreadCount = 0;

        ConversationEntity c3 = new ConversationEntity();
        c3.localId = "conv-3";
        c3.tenantId = "t-2";
        c3.tenantName = "Nusrat Jahan";
        c3.lastMessage = "I paid 12,200 today, remaining balance next week.";
        c3.lastMessageTime = now - 2L * 86400000L;
        c3.unreadCount = 1;

        db.conversationDao().insert(c1);
        db.conversationDao().insert(c2);
        db.conversationDao().insert(c3);

        List<MessageEntity> msgs = new ArrayList<>();

        MessageEntity m1 = new MessageEntity();
        m1.localId = "msg-1";
        m1.conversationId = "conv-1";
        m1.senderType = "tenant";
        m1.content = "Salam Bhai, the kitchen sink pipe is leaking again.";
        m1.sentAt = now - 7200000L;
        msgs.add(m1);

        MessageEntity m2 = new MessageEntity();
        m2.localId = "msg-2";
        m2.conversationId = "conv-1";
        m2.senderType = "landlord";
        m2.content = "Walaykum Assalam. I have notified the technician. Plumber is scheduled for tomorrow at 11 AM.";
        m2.sentAt = now - 3600000L;
        msgs.add(m2);

        MessageEntity m3 = new MessageEntity();
        m3.localId = "msg-3";
        m3.conversationId = "conv-2";
        m3.senderType = "tenant";
        m3.content = "Transferred October rent 35,100 via City Bank. Txn CB104928.";
        m3.sentAt = now - 90000000L;
        msgs.add(m3);

        MessageEntity m4 = new MessageEntity();
        m4.localId = "msg-4";
        m4.conversationId = "conv-2";
        m4.senderType = "landlord";
        m4.content = "Received payment confirmation. Thank you!";
        m4.sentAt = now - 86400000L;
        msgs.add(m4);

        db.messageDao().insertAll(msgs);
    }

    private static void seedNotifications(AppDatabase db) {
        long now = System.currentTimeMillis();

        List<NotificationEntity> notifs = new ArrayList<>();

        NotificationEntity n1 = new NotificationEntity();
        n1.localId = "notif-1";
        n1.title = "Rent Payment Pending";
        n1.body = "Tanvir Ahmed (Unit A-101) October invoice of ৳17,200 is due.";
        n1.category = "payment";
        n1.read = false;
        n1.createdAt = now - 1800000L;
        notifs.add(n1);

        NotificationEntity n2 = new NotificationEntity();
        n2.localId = "notif-2";
        n2.title = "New Maintenance Ticket";
        n2.body = "Kitchen sink pipe leakage reported in Green View A-101.";
        n2.category = "maintenance";
        n2.read = false;
        n2.createdAt = now - 3600000L;
        notifs.add(n2);

        NotificationEntity n3 = new NotificationEntity();
        n3.localId = "notif-3";
        n3.title = "Marketplace Inquiry";
        n3.body = "Dr. Sabrina Islam sent inquiry for Penthouse at Sunset Heights.";
        n3.category = "lead";
        n3.read = false;
        n3.createdAt = now - 12000000L;
        notifs.add(n3);

        NotificationEntity n4 = new NotificationEntity();
        n4.localId = "notif-4";
        n4.title = "Payment Received";
        n4.body = "৳35,100 received from Farhan Chowdhury for Unit G-401.";
        n4.category = "payment";
        n4.read = true;
        n4.createdAt = now - 86400000L;
        notifs.add(n4);

        db.notificationDao().insertAll(notifs);
    }
}

package com.landlord.android.feature.maintenance;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.common.AppExecutors;
import com.landlord.android.core.db.AppDatabase;
import com.landlord.android.core.db.MaintenanceTicketDao;
import com.landlord.android.core.db.MaintenanceTicketEntity;
import java.util.List;
import java.util.UUID;

public class MaintenanceRepository {

    private final MaintenanceTicketDao ticketDao;

    public MaintenanceRepository(Context context) {
        this.ticketDao = AppDatabase.getInstance(context).maintenanceTicketDao();
    }

    public LiveData<List<MaintenanceTicketEntity>> observeTickets() {
        return ticketDao.observeAll();
    }

    public LiveData<Integer> observePendingCount() {
        return ticketDao.observePendingCount();
    }

    public void createTicket(String unitId, String tenantId, String title, String description, String priority) {
        AppExecutors.DB.execute(() -> {
            MaintenanceTicketEntity ticket = new MaintenanceTicketEntity();
            ticket.localId = "ticket-" + UUID.randomUUID().toString().substring(0, 8);
            ticket.unitId = unitId;
            ticket.tenantId = tenantId;
            ticket.title = title;
            ticket.description = description;
            ticket.status = "pending";
            ticket.priority = priority != null ? priority : "medium";
            ticket.createdAt = System.currentTimeMillis();
            ticketDao.insert(ticket);
        });
    }

    public void updateStatus(MaintenanceTicketEntity ticket, String newStatus) {
        AppExecutors.DB.execute(() -> {
            ticket.status = newStatus;
            ticketDao.update(ticket);
        });
    }

    public void deleteTicket(MaintenanceTicketEntity ticket) {
        AppExecutors.DB.execute(() -> ticketDao.delete(ticket));
    }
}

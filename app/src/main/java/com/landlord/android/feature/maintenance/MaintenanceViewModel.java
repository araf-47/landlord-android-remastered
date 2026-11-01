package com.landlord.android.feature.maintenance;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.landlord.android.core.db.MaintenanceTicketEntity;
import java.util.List;

public class MaintenanceViewModel extends AndroidViewModel {

    private final MaintenanceRepository repository;

    public MaintenanceViewModel(@NonNull Application application) {
        super(application);
        this.repository = new MaintenanceRepository(application);
    }

    public LiveData<List<MaintenanceTicketEntity>> getTickets() {
        return repository.observeTickets();
    }

    public void createTicket(String unitId, String tenantId, String title, String description, String priority) {
        repository.createTicket(unitId, tenantId, title, description, priority);
    }

    public void updateStatus(MaintenanceTicketEntity ticket, String status) {
        repository.updateStatus(ticket, status);
    }

    public void deleteTicket(MaintenanceTicketEntity ticket) {
        repository.deleteTicket(ticket);
    }
}

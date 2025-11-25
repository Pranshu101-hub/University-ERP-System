package edu.univ.erp.access;
import edu.univ.erp.data.ErpDataStore;

public class AccessChecker {
    private final ErpDataStore erpDataStore;
    public AccessChecker() {
        this.erpDataStore =new ErpDataStore();
    }
    //checks maintenance flag in db
    public boolean isMaintenanceOn() {
        try { // checks 'maintenance_on =true/false' from the settings table
            String value =erpDataStore.getSetting("maintenance_on");
            return "true".equalsIgnoreCase(value);
        } catch (Exception e) {
            e.printStackTrace();
            return true; //true if maintenance is on, false otherwise
        }
    }
}
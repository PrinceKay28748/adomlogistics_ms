package adomlogistics.service;

import java.sql.SQLException;

import adomlogistics.model.Driver;
import adomlogistics.model.Route;
import adomlogistics.utils.BasicHashMap;
import adomlogistics.storage.Database;

public class DispatcherService {
    private Driver[] availableDrivers;
    private int driverCount;
    private BasicHashMap<Integer, Driver> driverMap;
    private BasicHashMap<Integer, Route[]> driverRoutes;
    private int nextDriverId = 1;
    private Database database;

    public DispatcherService(Database database) {
        availableDrivers = new Driver[100];
        driverMap = new BasicHashMap<>();
        driverRoutes = new BasicHashMap<>();
        driverCount = 0;
        this.database = database;
    }

    // ✅ Single method to add drivers
    public void addDriver(Driver driver) {
        // Check for duplicate name
        for (Object obj : driverMap.values()) {
            Driver existing = (Driver) obj;
            if (existing.name.equalsIgnoreCase(driver.name)) {
                System.out.println("Driver \"" + driver.name + "\" already exists. Skipping...");
                return;
            }
        }

        // Insert into availableDrivers[] in sorted order by experience
        int i = driverCount - 1;
        while (i >= 0 && availableDrivers[i].experienceYears < driver.experienceYears) {
            availableDrivers[i + 1] = availableDrivers[i];
            i--;
        }
        availableDrivers[i + 1] = driver;
        driverCount++;

        // Add to internal maps
        driverMap.put(driver.id, driver);
        driverRoutes.put(driver.id, new Route[0]);

        // Save to DB if not already existing
        try {
            if (!database.driverExists(driver.id)) {
                database.saveDriver(driver);
            }
        } catch (SQLException e) {
            System.out.println("Failed to save driver to DB: " + e.getMessage());
        }

        System.out.println("Driver \"" + driver.name + "\" added successfully.");
    }

    public Driver assignDriver() {
        if (driverCount == 0) return null;
        Driver driver = availableDrivers[driverCount - 1];
        driverCount--;
        return driver;
    }

    public Driver getDriver(int driverId) {
        return driverMap.get(driverId);
    }

    public int getDriverCount() {
        return driverCount;
    }

    public Driver[] getAvailableDrivers() {
        Driver[] available = new Driver[driverCount];
        System.arraycopy(availableDrivers, 0, available, 0, driverCount);
        return available;
    }

    public Driver[] getAllDrivers() {
        Object[] raw = driverMap.values();
        Driver[] drivers = new Driver[raw.length];
        for (int i = 0; i < raw.length; i++) {
            drivers[i] = (Driver) raw[i];
        }
        return drivers;
    }

    public Route[] getDriverRoutes(int driverId) {
        return driverRoutes.get(driverId);
    }

    public void addRouteToDriver(int driverId, Route route) {
        Route[] currentRoutes = driverRoutes.get(driverId);
        Route[] newRoutes = new Route[currentRoutes.length + 1];
        System.arraycopy(currentRoutes, 0, newRoutes, 0, currentRoutes.length);
        newRoutes[currentRoutes.length] = route;
        driverRoutes.put(driverId, newRoutes);
    }

    public String getDriverPerformance(int driverId) {
        Route[] routes = driverRoutes.get(driverId);
        if (routes == null || routes.length == 0) {
            return "No routes assigned to this driver.";
        }

        int completed = 0;
        int totalTime = 0;

        for (Route r : routes) {
            if ("Completed".equalsIgnoreCase(r.status)) {
                completed++;
            }
            totalTime += r.estimatedTime;
        }

        return String.format(
            "Driver ID %d - Total Routes: %d | Completed: %d | Completion Rate: %.1f%% | Total Time: %d mins",
            driverId, routes.length, completed, (100.0 * completed / routes.length), totalTime
        );
    }
}

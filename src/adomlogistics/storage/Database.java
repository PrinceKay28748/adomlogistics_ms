package adomlogistics.storage;

import adomlogistics.model.Vehicle;
import adomlogistics.model.Driver;

import java.sql.*;
import java.util.*;

public class Database {
    private static final String url = "jdbc:mysql://localhost:3306/adom_logistics"; 
    private static final String USER = "root";
    private static final String PASSWORD = "Flight$23";
    private Connection connection;

    // Establish database connection and initialize tables
    public Database() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, USER, PASSWORD);
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    // Create tables if they don't exist
    private void initializeDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Vehicles table
            stmt.execute("CREATE TABLE IF NOT EXISTS vehicles (" +
                    "reg_number VARCHAR(20) PRIMARY KEY, " +
                    "name VARCHAR(100), " +
                    "type VARCHAR(50), " +
                    "fuel_usage FLOAT, " +
                    "mileage INT, " +
                    "driver_id INT, " +
                    "maintenance_history TEXT, " +
                    "last_service_date VARCHAR(20))");

            // Drivers table with AUTO_INCREMENT for automatic ID assignment
            stmt.execute("CREATE TABLE IF NOT EXISTS drivers (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(100), " +
                    "experience_years INT, " +
                    "distance_from_pickup FLOAT, " +
                    "available BOOLEAN)");

            // Deliveries table
            stmt.execute("CREATE TABLE IF NOT EXISTS deliveries (" +
                    "package_id VARCHAR(50) PRIMARY KEY, " +
                    "origin VARCHAR(100), " +
                    "destination VARCHAR(100), " +
                    "assigned_vehicle_id VARCHAR(20), " +
                    "assigned_driver_id INT, " +
                    "estimated_hours INT, " +
                    "status VARCHAR(20), " +
                    "created_at VARCHAR(50))");
        }
    }

    // Check if a driver already exists based on ID
    public boolean driverExists(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM drivers WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Save a driver to the database, insert or update if already exists
    public void saveDriver(Driver driver) throws SQLException {
        String sql = "INSERT INTO drivers (id, name, experience_years, distance_from_pickup, available) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name=VALUES(name), " +
                     "experience_years=VALUES(experience_years), " +
                     "distance_from_pickup=VALUES(distance_from_pickup), " +
                     "available=VALUES(available)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, driver.id);  // Set manually only if you're managing IDs
            stmt.setString(2, driver.name);
            stmt.setInt(3, driver.experienceYears);
            stmt.setDouble(4, driver.distanceFromPickup);
            stmt.setBoolean(5, driver.available);
            stmt.executeUpdate();
        }
    }

    // Save or update vehicle record in database
    public void saveVehicle(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO vehicles VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "name=VALUES(name), type=VALUES(type), fuel_usage=VALUES(fuel_usage), " +
                     "mileage=VALUES(mileage), driver_id=VALUES(driver_id), " +
                     "maintenance_history=VALUES(maintenance_history), last_service_date=VALUES(last_service_date)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, vehicle.regNumber);
            stmt.setString(2, vehicle.name);
            stmt.setString(3, vehicle.type);
            stmt.setFloat(4, vehicle.fuelUsage);
            stmt.setInt(5, vehicle.mileage);
            stmt.setObject(6, vehicle.driverId, Types.INTEGER);
            stmt.setString(7, vehicle.maintenanceHistory);
            stmt.setString(8, vehicle.lastServiceDate);
            stmt.executeUpdate();
        }
    }

    // Load all vehicles from the database
    public List<Vehicle> loadAllVehicles() throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT * FROM vehicles";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Vehicle vehicle = new Vehicle(
                        rs.getString("reg_number"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getFloat("fuel_usage"),
                        rs.getInt("mileage"),
                        rs.getObject("driver_id", Integer.class),
                        rs.getString("maintenance_history"),
                        rs.getString("last_service_date")
                );
                vehicles.add(vehicle);
            }
        }
        return vehicles;
    }

    // Close database connection when done
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

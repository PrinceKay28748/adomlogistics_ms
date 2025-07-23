package adomlogistics.model;

public class Driver {
    public int id;
    public String name;
    public int experienceYears;
    public double distanceFromPickup;
    public boolean available;

    public Driver(int id, String name, int experienceYears, double distanceFromPickup) {
        this.id = id;
        this.name = name;
        this.experienceYears = experienceYears;
        this.distanceFromPickup = distanceFromPickup;
        this.available = true;
    }

    public Driver(int i, String name2, int experienceYears2) {
        //TODO Auto-generated constructor stub
    }

    @Override
    public String toString() {
        return String.format("%d: %s (%d yrs exp) | %.1f km away | %s",
                id, name, experienceYears, distanceFromPickup,
                available ? "Available" : "Assigned");
    }
}
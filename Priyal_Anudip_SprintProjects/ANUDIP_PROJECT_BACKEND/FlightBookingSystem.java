import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class FlightBookingSystem {

    static final String url = "jdbc:mysql://localhost:3306/flightdb2";
    static final String user = "root";
    static final String pass = "Priyal121704";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try (Connection con = DriverManager.getConnection(url, user, pass)) {

            System.out.println("Database connected successfully!");

            while (true) {

                System.out.println("\n===== Flight Booking System =====");
                System.out.println("1. Add Flight");
                System.out.println("2. Display Flights");
                System.out.println("3. Book Ticket");
                System.out.println("4. View Booked Tickets");
                System.out.println("5. Cancel Ticket");
                System.out.println("6. Delete Flight");
                System.out.println("7. Exit");
                System.out.print("Enter choice: ");

                int choice = sc.nextInt();

                switch (choice) {
                    case 1 -> addFlight(con, sc);
                    case 2 -> displayFlights(con);
                    case 3 -> bookTicket(con, sc);
                    case 4 -> viewBookings(con);
                    case 5 -> cancelTicket(con, sc);
                    case 6 -> deleteFlight(con, sc);
                    case 7 -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice!");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    // 1. ADD FLIGHT
    static void addFlight(Connection con, Scanner sc) {
        try {
            sc.nextLine();
            System.out.print("Flight Name: ");
            String name = sc.nextLine();

            System.out.print("Source: ");
            String source = sc.nextLine();

            System.out.print("Destination: ");
            String dest = sc.nextLine();

            System.out.print("Seats: ");
            int seats = sc.nextInt();

            String sql = "INSERT INTO flights (flight_name, source, destination, seats_available) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, source);
            ps.setString(3, dest);
            ps.setInt(4, seats);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                System.out.println("Flight added! ID: " + rs.getInt(1));
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 2. DISPLAY FLIGHTS
    static void displayFlights(Connection con) {
        try {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM flights");

            System.out.println("\nID | Name | Route | Seats");
            System.out.println("--------------------------------");

            while (rs.next()) {
                System.out.println(
                        rs.getInt("id") + " | " +
                                rs.getString("flight_name") + " | " +
                                rs.getString("source") + " -> " +
                                rs.getString("destination") + " | " +
                                rs.getInt("seats_available"));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching flights");
        }
    }

    // 3. BOOK TICKET
    static void bookTicket(Connection con, Scanner sc) {
        try {
            System.out.print("Enter Flight ID: ");
            int id = sc.nextInt();
            sc.nextLine();

            System.out.print("Passenger Name: ");
            String name = sc.nextLine();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT seats_available FROM flights WHERE id=?");
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {

                PreparedStatement book = con.prepareStatement(
                        "INSERT INTO bookings (flight_id, passenger_name, booking_date) VALUES (?, ?, CURDATE())",
                        Statement.RETURN_GENERATED_KEYS);

                book.setInt(1, id);
                book.setString(2, name);
                book.executeUpdate();

                ResultSet rsBook = book.getGeneratedKeys();
                int bookingId = -1;

                if (rsBook.next()) {
                    bookingId = rsBook.getInt(1);
                }

                PreparedStatement update = con.prepareStatement(
                        "UPDATE flights SET seats_available = seats_available - 1 WHERE id=?");
                update.setInt(1, id);
                update.executeUpdate();

                System.out.println("Ticket booked! Booking ID: " + bookingId);

                logToFile("BOOKED: " + name + " FlightID: " + id + " BookingID: " + bookingId);

            } else {
                System.out.println("No seats or invalid flight ID!");
            }

        } catch (SQLException e) {
            System.out.println("Booking error");
        }
    }

    // 4. VIEW BOOKINGS
    static void viewBookings(Connection con) {
        try {
            String sql = "SELECT b.id, b.passenger_name, f.flight_name, f.source, f.destination " +
                    "FROM bookings b JOIN flights f ON b.flight_id = f.id";

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            System.out.println("\nBooking ID | Passenger | Flight | Route");

            while (rs.next()) {
                System.out.println(
                        rs.getInt(1) + " | " +
                                rs.getString(2) + " | " +
                                rs.getString(3) + " | " +
                                rs.getString(4) + " -> " +
                                rs.getString(5));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching bookings");
        }
    }

    // 5. CANCEL TICKET
    static void cancelTicket(Connection con, Scanner sc) {
        try {
            System.out.print("Enter Booking ID: ");
            int bid = sc.nextInt();

            PreparedStatement get = con.prepareStatement(
                    "SELECT flight_id FROM bookings WHERE id=?");
            get.setInt(1, bid);

            ResultSet rs = get.executeQuery();

            if (rs.next()) {

                int fid = rs.getInt(1);

                PreparedStatement del = con.prepareStatement(
                        "DELETE FROM bookings WHERE id=?");
                del.setInt(1, bid);
                del.executeUpdate();

                PreparedStatement upd = con.prepareStatement(
                        "UPDATE flights SET seats_available = seats_available + 1 WHERE id=?");
                upd.setInt(1, fid);
                upd.executeUpdate();

                System.out.println("Ticket canceled! Booking ID: " + bid);

                logToFile("CANCELLED BookingID: " + bid);

            } else {
                System.out.println("Booking not found!");
            }

        } catch (SQLException e) {
            System.out.println("Cancel error");
        }
    }

    // 6. DELETE FLIGHT
    static void deleteFlight(Connection con, Scanner sc) {
        try {
            System.out.print("Enter Flight ID: ");
            int id = sc.nextInt();

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM flights WHERE id=?");
            ps.setInt(1, id);

            int rows = ps.executeUpdate();
            System.out.println(rows + " flight deleted!");

        } catch (SQLException e) {
            System.out.println("Delete error");
        }
    }

    // FILE LOGGING
    static void logToFile(String msg) {
        try (FileWriter fw = new FileWriter("booking_log.txt", true)) {
            fw.write(msg + "\n");
        } catch (IOException e) {
            System.out.println("File error");
        }
    }
}
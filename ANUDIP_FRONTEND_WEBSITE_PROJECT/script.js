let flights = JSON.parse(localStorage.getItem("flights")) || [];
let bookings = JSON.parse(localStorage.getItem("bookings")) || [];

/* =========================
   SAVE DATA
========================= */
function save() {
    localStorage.setItem("flights", JSON.stringify(flights));
    localStorage.setItem("bookings", JSON.stringify(bookings));
}

/* =========================
   NOTIFICATION SYSTEM
========================= */
function showNotification(message, type = "info") {
    const notification = document.createElement("div");

    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <span>${message}</span>
    `;

    document.body.appendChild(notification);

    setTimeout(() => notification.classList.add("show"), 10);

    setTimeout(() => {
        notification.classList.remove("show");
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

/* =========================
   CLEAR INPUTS
========================= */
function clearInputs() {
    document.getElementById("name").value = "";
    document.getElementById("source").value = "";
    document.getElementById("dest").value = "";
    document.getElementById("seats").value = "";
    document.getElementById("flightId").value = "";
    document.getElementById("passenger").value = "";
    document.getElementById("bookingId").value = "";
}

/* =========================
   ADD FLIGHT
========================= */
function addFlight() {
    let name = document.getElementById("name").value.trim();
    let source = document.getElementById("source").value.trim();
    let dest = document.getElementById("dest").value.trim();
    let seats = parseInt(document.getElementById("seats").value);

    if (!name || !source || !dest || isNaN(seats) || seats <= 0) {
        showNotification("Enter valid flight details", "error");
        return;
    }

    if (source.toLowerCase() === dest.toLowerCase()) {
        showNotification("Source and destination cannot be same", "error");
        return;
    }

    let flight = {
        id: Date.now(),
        name,
        source,
        dest,
        seats
    };

    flights.push(flight);
    save();
    displayFlights();
    displayBookings();
    clearInputs();

    showNotification("Flight added successfully", "success");
}

/* =========================
   DISPLAY FLIGHTS
========================= */
function displayFlights() {
    let table = document.getElementById("flightTable");
    table.innerHTML = "";

    if (flights.length === 0) {
        document.getElementById("emptyFlightState").style.display = "block";
        return;
    }

    document.getElementById("emptyFlightState").style.display = "none";

    flights.forEach(f => {
        let row = document.createElement("tr");

        row.innerHTML = `
            <td>${f.id}</td>
            <td>${f.name}</td>
            <td>${f.source} → ${f.dest}</td>
            <td>${f.seats}</td>
            <td>
                <button class="btn btn-sm btn-danger" onclick="deleteFlight(${f.id})">
                    Delete
                </button>
            </td>
        `;

        table.appendChild(row);
    });
}

/* =========================
   BOOK TICKET
========================= */
function bookTicket() {
    let flightId = parseInt(document.getElementById("flightId").value);
    let passenger = document.getElementById("passenger").value.trim();

    if (isNaN(flightId) || !passenger) {
        showNotification("Enter valid booking details", "error");
        return;
    }

    let flight = flights.find(f => f.id === flightId);

    if (!flight) {
        showNotification("Flight not found", "error");
        return;
    }

    if (flight.seats <= 0) {
        showNotification("No seats available", "error");
        return;
    }

    flight.seats--;

    let booking = {
        id: Date.now(),
        flightId,
        passenger,
        bookingDate: new Date().toLocaleString()
    };

    bookings.push(booking);

    save();
    displayFlights();
    displayBookings();
    clearInputs();

    showNotification("Ticket booked successfully", "success");
}

/* =========================
   CANCEL TICKET (FIXED)
========================= */
function cancelTicket() {
    let bookingId = parseInt(document.getElementById("bookingId").value);

    if (isNaN(bookingId)) {
        showNotification("Enter valid booking ID", "error");
        return;
    }

    let index = bookings.findIndex(b => b.id === bookingId);

    if (index === -1) {
        showNotification("Booking not found", "error");
        return;
    }

    let booking = bookings[index];
    let flight = flights.find(f => f.id === booking.flightId);

    if (flight) {
        flight.seats++;
    }

    bookings.splice(index, 1);

    save();
    displayFlights();
    displayBookings();
    clearInputs();

    showNotification("Booking cancelled successfully", "success");
}

/* =========================
   DELETE FLIGHT
========================= */
function deleteFlight(id) {
    flights = flights.filter(f => f.id !== id);
    bookings = bookings.filter(b => b.flightId !== id);

    save();
    displayFlights();
    displayBookings();

    showNotification("Flight deleted successfully", "success");
}

/* =========================
   DISPLAY BOOKINGS
========================= */
function displayBookings() {
    let table = document.getElementById("bookingTable");
    table.innerHTML = "";

    if (bookings.length === 0) {
        document.getElementById("emptyBookingState").style.display = "block";
        return;
    }

    document.getElementById("emptyBookingState").style.display = "none";

    bookings.forEach(b => {
        let flight = flights.find(f => f.id === b.flightId);

        let row = document.createElement("tr");

        row.innerHTML = `
            <td>${b.id}</td>
            <td>${b.passenger}</td>
            <td>${b.flightId}</td>
            <td>${flight ? flight.name : "Deleted"}</td>
            <td>${flight ? flight.source + " → " + flight.dest : "-"}</td>
            <td>${b.bookingDate}</td>
        `;

        table.appendChild(row);
    });
}

/* =========================
   CLEAR ALL DATA
========================= */
function clearAll() {
    if (confirm("Clear all data?")) {
        localStorage.clear();
        flights = [];
        bookings = [];

        displayFlights();
        displayBookings();
        clearInputs();

        showNotification("All data cleared", "success");
    }
}

/* =========================
   INIT
========================= */
document.addEventListener("DOMContentLoaded", () => {
    displayFlights();
    displayBookings();

    document.querySelector(".btn-add").addEventListener("click", addFlight);
    document.querySelector(".btn-book").addEventListener("click", bookTicket);
    document.querySelector(".btn-cancel").addEventListener("click", cancelTicket);
    document.getElementById("clearAllBtn").addEventListener("click", clearAll);
});
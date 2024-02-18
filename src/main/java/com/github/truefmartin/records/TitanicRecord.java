package com.github.truefmartin.records;

import java.io.IOException;
import java.util.Objects;

import static java.lang.CharSequence.compare;

public class TitanicRecord extends Writeable implements Comparable<TitanicRecord> {

    public String passengerId;
    public String firstName;
    public String lastName;
    public String age;
    public String ticketNumber;
    public String ticketFare;
    public String purchaseDate;
    private boolean empty;

    public TitanicRecord () {
        super("-1", "", "", "", "", "", "");
        empty = true;
        passengerId = "-1";
    }

    public TitanicRecord(String... fields) {
        super(fields);
    }

    public TitanicRecord(String passengerId,
                         String firstName,
                         String lastName,
                         String age,
                         String ticketNumber,
                         String ticketFare,
                         String purchaseDate) {
        super(passengerId, firstName, lastName, age, ticketNumber, ticketFare, purchaseDate);
        this.passengerId = passengerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.ticketNumber = ticketNumber;
        this.ticketFare = ticketFare;
        this.purchaseDate = purchaseDate;
    }

    /**
     * Update the fields of a record from an array of fields
     *
     * @param fields array with values of fields
     * @throws IOException on fields != length of 7
     */
    public void updateFields(String[] fields) throws IOException {
        if (fields.length == 7) {
            assign(fields);
            empty = false;
        } else {
            throw new IOException("Err: Invalid number of fields from reading IN record");
        }
    }

    /**
     * Check if record fields have been updated
     *
     * @return true if record has been updated otherwise false
     */
    public boolean isPopulated() {
        return !isEmpty();
    }

    @Override
    public String toString() {
        return String.format("\nPassenger ID: %s\n" +
                "\t* Name: %s %s\n" +
                "\t* Age: %s\n" +
                "\t* Ticket Info\n" +
                        "\t\t* Number: %s\n" +
                        "\t\t* Fare: %s\n" +
                        "\t\t* Date of Purchase: %s",
                passengerId,
                firstName, lastName,
                age,
                ticketNumber, ticketFare, purchaseDate);
    }

    @Override
    public int compareTo(TitanicRecord o) {
        if (Objects.equals(this.passengerId, "-1") || Objects.equals(o.passengerId, "-1")) return 0;
        return compare(this.passengerId, o.passengerId);
    }

    @Override
    public void reassignFields() {
        assign(attributes);
        empty = Objects.equals(passengerId, "-1") || Objects.equals(passengerId, "");
    }

    private void assign(String[] columns) {
        this.passengerId = columns[0];
        this.firstName = columns[1];
        this.lastName = columns[2];
        this.age = columns[3];
        this.ticketNumber = columns[4];
        this.ticketFare = columns[5];
        this.purchaseDate = columns[6];
    }

    @Override
    public void print() {
        System.out.println(this);
    }

    @Override
    public boolean isEmpty() {
        return empty || Objects.equals(passengerId, "-1") || Objects.equals(passengerId, "");
    }

    @Override
    public void printAttributeNames() {
        System.out.println("0) Passenger ID, 1) Last name, 2) First Name, 3) Age,\n" +
                "4) Ticket Number, 5) Ticket Fare, 6) Purchase Date");
    }

    @Override
    protected void initAttributeNames() {
        this.attributeNames = new String[]{
                "Passenger ID",
                "Last Name",
                "First Name",
                "Age",
                "Ticket Number",
                "Ticket Fare",
                "Purchase Date"
        };
    }

}

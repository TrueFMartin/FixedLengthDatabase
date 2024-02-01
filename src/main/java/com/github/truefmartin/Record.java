package com.github.truefmartin;

import java.io.IOException;

public class Record {

  private boolean empty;

  public String passengerId;
  public String firstName;
  public String lastName;
  public String age;
  public String ticketNumber;
  public String ticketFare;
  public String purchaseDate;


  public Record() {
    empty = true;
    passengerId = "-1";
  }

  /**
   * Update the fields of a record from an array of fields
   * 
   * @param fields array with values of fields
   * @throws IOException on fields != length of 7
   */
  public void updateFields(String[] fields) throws IOException {
    if (fields.length == 7) {
      this.passengerId = fields[0];
      this.firstName = fields[1];
      this.lastName = fields[2];
      this.age = fields[3];
      this.ticketNumber = fields[4];
      this.ticketFare = fields[5];
      this.purchaseDate = fields[6];
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
    return !empty;
  }

  @Override
  public String toString() {
    return String.format("Passenger ID: %s\n\t* Name: %s %s\n\t* Age: %s\n\t* Ticket Info --  Number: %s,\tFare: %s,\tDate of Purchase: %s",
            passengerId,
            firstName, lastName,
            age,
            ticketNumber, ticketFare, purchaseDate);
  }
}

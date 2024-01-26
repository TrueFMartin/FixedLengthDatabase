package com.github.truefmartin;

import java.io.IOException;

public class Record {

  private boolean empty;

  public String Id;
  public String Experience;
  public String Married;
  public String Wage;
  public String Industry;

  public Record() {
    empty = true;
  }

  /**
   * Update the fields of a record from an array of fields
   * 
   * @param fields array with values of fields
   * @return nothing
   * @throws IOException
   */
  public void updateFields(String[] fields) throws IOException {
    if (fields.length == 5) {
      this.Id = fields[0];
      this.Experience = fields[1];
      this.Married = fields[2];
      this.Wage = fields[3];
      this.Industry = fields[4];

      empty = false;
    } else
      throw new IOException();
  }

  /**
   * Check if record fields have been updated
   * 
   * @return true if record has been updated otherwise false
   */
  public boolean isEmpty() {
    return empty;
  }

  public String toString() {
    return "Id: " + this.Id +
        ", Experience: " + this.Experience +
        ", Married: " + this.Married +
        ", Wage: " + this.Wage +
        ", Industry: " + this.Industry;
  }

}

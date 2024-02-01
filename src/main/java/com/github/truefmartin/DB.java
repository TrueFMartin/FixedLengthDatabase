package com.github.truefmartin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class DB {
  public static final int NUM_RECORDS = 10;
  public static final int RECORD_SIZE = 71;

  private RandomAccessFile Din;
  private RandomAccessFile Dout;
  private int num_records;
  private String Id;
  private String Experience;
  private String Married;
  private String Wage;
  private String Industry;

  public DB() {
    this.Din = null;
    this.Dout = null;
    this.num_records = 0;
    this.Id = "ID";
    this.Experience = "EXPERIENCE";
    this.Married = "MARRIED";
    this.Wage = "WAGE";
    this.Industry = "INDUSTRY";
  }

  /**
   * Opens the file in read/write mode
   * 
   * @param filename (e.g., input.data)
   * @return status true if operation successful
   */
  public void  open(String filename) {
    // Set the number of records
    this.num_records = NUM_RECORDS;

    // Open file in read/write mode
    try {
      this.Din = new RandomAccessFile(filename, "r");
    } catch (FileNotFoundException e) {
      System.out.println("Could not open file\n");
      e.printStackTrace();
    }
  }
  
  public static RandomAccessFile openFile(String fileName) throws IOException {
      return new RandomAccessFile(fileName, "rw");
  }
  
  /** 
   * 
   *  
   */
  public void writeRecord(RandomAccessFile file, String Id, String Experience, String Married, String Wage, String Industry ) {
	  
	  try {
		  
		  file.writeBytes(Id + Experience + Married + Wage + Industry+"\n");
		  
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}  
  }
  
  /** Opens CSV file and creates new data file
   * Reads CSV file attributes
   * @throws IOException 
   */
  public void createDB(String filename) throws IOException {
	  
	  this.Din = new RandomAccessFile(filename+".csv", "r");
	  RandomAccessFile file = openFile(filename+".data"); 
	  
	  String line;
      while ((line = this.Din.readLine()) != null) {
          String[] attribute = line.split(",");
          this.Id = String.format("%-10s", attribute[0]);
          this.Experience = String.format("%-5s", attribute[1]);
          this.Married = String.format("%-5s", attribute[2]);
          this.Wage = String.format("%-20s", attribute[3]);
          this.Industry = String.format("%-30s", attribute[4]);
               
          writeRecord (file, this.Id, this.Experience, this.Married, this.Wage, this.Industry);
          
      }
      file.close();
      //Din.close();
  }

  /**
   * Close the database file
   */
  public void close() {
    try {
      Din.close();
    } catch (IOException e) {
      System.out.println("There was an error while attempting to close the database file.\n");
      e.printStackTrace();
    }
  }

  /**
   * Get record number n (Records numbered from 0 to NUM_RECORDS-1)
   * 
   * @param record_num
   * @return values of the fields with the name of the field and
   *         the values read from the record
   */
  public Record readRecord(int record_num) {
    Record record = new Record();
    String[] fields;

    if ((record_num >= 0) && (record_num < this.num_records)) {
      try {
        Din.seek(0); // return to the top of the file
        Din.skipBytes(record_num * RECORD_SIZE);
        // parse record and update fields
        fields = Din.readLine().split("\\s{2,}", 0);
        record.updateFields(fields);
      } catch (IOException e) {
        System.out.println("There was an error while attempting to read a record from teh database file.\n");
        e.printStackTrace();
      }
    }

    return record;
  }

  /**
   * Binary Search by record id
   * 
   * @param id
   * @return Record number (which can then be used by read to
   *         get the fields) or -1 if id not found
   */
  public boolean binarySearch(String id, int[] recordNum) {
    int Low = 0;
    int High = NUM_RECORDS - 1;
    int Middle = 0;
    boolean Found = false;
    Record record;

    while (!Found && (High >= Low)) {
      Middle = (Low + High) / 2;
      record = readRecord(Middle);
      String MiddleId = record.passengerId;

      // int result = MiddleId[0].compareTo(id); // DOES STRING COMPARE
      int result = Integer.parseInt(MiddleId) - Integer.parseInt(id); // DOES INT COMPARE of MiddleId[0] and id
      if (result == 0){
        Found = true;
        //update the recordNum saved on array
        recordNum[0] = Middle;
      }

      else if (result < 0)
        Low = Middle + 1;
      else
        High = Middle - 1;
    }
    if (Found) {
      return true; // the record number of the record
    } else
      return false;
  }
}

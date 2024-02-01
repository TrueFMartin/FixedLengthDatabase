package com.github.truefmartin;

import java.io.IOException;

//-----------------------------------------------------
// Example code to read from fixed length records (random access file)
//-----------------------------------------------------

public class Main {
    static Record record;

    public static void main(String[] args) throws IOException {

        // calls constructor
        DB db = new DB();


        db.createDB("input");

        // opens "input.data"
        db.open("input.data");

        System.out
                .println("------------- Testing readRecord ------------");

        // Reads record 0
        // Then prints the values of the 5 fields to the screen with the name of the
        // field and the values read from the record, i.e.,
        // id: 00003 experience: 3 married: no wages: 1.344461678 industry:
        // Business_and_Repair_Service
        int record_num = 0;
        record = new Record();
        record = db.readRecord(record_num);
        if (record.isPopulated())
            System.out.println("RecordNum " + record_num + ": " + record.toString() + "\n\n");
        else {
            System.out.println("Could not get Record " + record_num);
            System.out.println("Record out of range");
        }

        // Reads record 9 (last record)
        record_num = DB.NUM_RECORDS - 1;
        record = db.readRecord(record_num);
        if (record.isPopulated())
            System.out.println("RecordNum " + record_num + ": " + record.toString() + "\n\n");
        else {
            System.out.println("Could not get Record " + record_num);
            System.out.println("Record out of range");
        }

        System.out
                .println("------------- Testing binarySearch ------------");

        int[] recordNum = new int[1];

        // Find record with id 42 (should not be found)// Find record 17
        String ID = "42";
        boolean found = db.binarySearch(ID, recordNum);
        if (found) {
            record = db.readRecord(recordNum[0]);
            System.out
                    .println(
                            "ID " + ID + " found at Record " + recordNum[0] + "\nRecordNum " + recordNum[0] + ": \n" + record.toString()
                                    + "\n\n");
        } else
            System.out.println("ID " + ID + " not found in our records\n\n");

        // Find record with id 00000 (the first one in the file)
        ID = "0000";
        found = db.binarySearch(ID,recordNum);
        if (found) {
            record = db.readRecord(recordNum[0]);
            System.out
                    .println(
                            "ID " + ID + " found at Record " + recordNum[0] + "\nRecordNum " + recordNum[0] + ": \n" + record.toString()
                                    + "\n\n");
        } else
            System.out.println("ID " + ID + " not found in our records\n\n");

        // Find record with id 00015 (the last one in the file)
        ID = "00015";
        found = db.binarySearch(ID,recordNum);
        if (found) {
            record = db.readRecord(recordNum[0]);
            System.out
                    .println(
                            "ID " + ID + " found at Record " + recordNum[0] + "\nRecordNum " + recordNum[0] + ": \n" + record.toString()
                                    + "\n\n");
        } else
            System.out.println("ID " + ID + " not found in our records\n\n");

        // Find record with id 00006 (somewhere in the middle)
        ID = "00006";
        found = db.binarySearch(ID,recordNum);
        if (found) {
            record = db.readRecord(recordNum[0]);
            System.out
                    .println(
                            "ID " + ID + " found at Record " + recordNum[0] + "\nRecordNum " + recordNum[0] + ": \n" + record.toString()
                                    + "\n\n");
        } else
            System.out.println("ID " + ID + " not found in our records\n\n");

        // Close database
        db.close();
    }
}

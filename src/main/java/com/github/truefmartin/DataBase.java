package com.github.truefmartin;

import com.github.truefmartin.exceptions.ColumnBoundsException;
import com.github.truefmartin.exceptions.DatabaseAlreadyOpenException;
import com.github.truefmartin.exceptions.DatabaseIsClosedException;
import com.github.truefmartin.exceptions.RowBoundsException;
import com.github.truefmartin.records.TitanicRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;

public class DataBase {

    private static final Logger logger = LogManager.getLogger(DataBase.class);
    private boolean isOpen = false;
    private FixedRecordsAccess<TitanicRecord> fileAccess;
    private int numRecords;
    private String fileName;


    /**
     * Create a new database file and then close the database afterward. Must use 'openDB()' to open the newly created
     * DB. This method can be called while another database is open and will not close that database.
     *
     * @param fileName   the file name prefix to build the database off of.
     * @param columns    the column sizes in bytes.
     * @return true if database file is created successfully with at least one record written. False otherwise.
     */
    public boolean create(String fileName, int[] columns) throws FileNotFoundException {
        NewTitanic newTitanic = new NewTitanic(fileName, columns);
        // If new database file fails to create, log and return false.
        int numWritten = newTitanic.start();
        if (numWritten <= 0) {
            logger.error("failed to create database from file {}, numRecords {}, columns {}", fileName, numWritten, columns);
            return false;
        }
        numRecords = numWritten;
        return true;
    }

    public boolean openDb(String fileName) throws FileNotFoundException {
        if (isOpen) {
            logger.warn(new DatabaseAlreadyOpenException("database with filename: " + fileName + ", is already open"));
            return false;
        }
        fileAccess = new FixedRecordsAccess<>(fileName + ".config", fileName + ".data", FixedRecordsAccess.FileStatus.WRITE);
        this.isOpen = true;
        this.fileName = fileName;
        this.numRecords = fileAccess.getNumRecords();
        logger.info("opened a database file of name {}, with numRecords {}", fileName, numRecords);
        return true;
    }

    public TitanicRecord readRow(int row) throws DatabaseIsClosedException, RowBoundsException {
        if (!isOpen) {
            throw new DatabaseIsClosedException("database is closed, unable to read row");
        }
        TitanicRecord record = new TitanicRecord();
        if (!fileAccess.read(row, record)) {
            throw new RowBoundsException("given record, " + row + ", is outside of expected bounds," + numRecords + ".");
        }
        return record;
    }


    /**
     * Find record by passenger id. Returns null if not found.
     *
     * @param passengerId the passenger id
     * @return the titanic record or null if not found.
     * @throws DatabaseIsClosedException the database is closed exception if this method is called when the
     * database is closed.
     */
    public TitanicRecord findRecord(String passengerId) throws DatabaseIsClosedException {
        if (!isOpen) {
            throw new DatabaseIsClosedException("database is closed, unable to read row");
        }
        int[] location = new int[]{-1};
        var record = binarySearch(passengerId, location);
        if (location[0] == -1) {
            return null;
        }
        return record;
    }

    public TitanicRecord[] getReport() throws DatabaseIsClosedException, RowBoundsException {
        if (!isOpen) {
            throw new DatabaseIsClosedException("database is closed, unable to read row");
        }
        TitanicRecord[] records = new TitanicRecord[Math.min(numRecords, 10)];
        for(int i = 0; i < records.length; i++) {
            records[i] = new TitanicRecord();
        }
        try {
            fileAccess.readBatch(0, records);
        } catch (ColumnBoundsException e) {
            logger.error("incorrect column size from get report");
            throw new RuntimeException(e);
        }
        return records;
    }


    /**
     * Close the currently open database and write to config file if anything has changed.
     */
    public void close() {
        if (!isOpen) {
            return;
        }
        fileAccess.writeConfigs();
        fileAccess.close();
        isOpen = false;
        fileAccess = null;
    }

    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Binary Search by record id
     *
     * @param id record key to search for
     * @param location an int array of size 1, if record is found its row number is stored in position 0 in 'location'
     * @return the record found
     */
    public TitanicRecord binarySearch(String id, int[] location) throws DatabaseIsClosedException {
        int low = 0;
        int high = numRecords - 1;
        int middle = 0;
        boolean isFound = false;
        boolean isNotEmpty = true;
        TitanicRecord record = new TitanicRecord();
        boolean searchDownIfEmpty = true;
        while (!isFound && (high >= low)) {
            middle = (low + high) / 2;
            try {
                while ( middle > low ) {
                    isNotEmpty = fileAccess.readFalseOnEmpty(middle, record);
                    if (isNotEmpty) {
                        // Everytime a record is found that is not empty, reset 'searchDownIfEmpty' so that next loop we
                        // will first decrement middle, before incrementing it if no non-empty records found.
                        searchDownIfEmpty = true;
                        break;
                    }
                    if (searchDownIfEmpty) {
                        middle--;
                    } else {
                        middle++;
                    }
                }
            } catch (RowBoundsException e) {
                logger.fatal("binary search went out of bounds with {}, in table of size {}", middle, numRecords);
                throw new RuntimeException(e);
            }
            // If after searching down from middle we could not find a non-empty record, search up from middle
            if (record.isEmpty()) {
                // If we have already tried searching up and down, break out of main loop.
                if (!searchDownIfEmpty) {
                    break;
                }
                searchDownIfEmpty = false;
                continue;
            }
            String MiddleId = record.passengerId;
            // int result = MiddleId[0].compareTo(id); // DOES STRING COMPARE
            int result = Integer.parseInt(MiddleId) - Integer.parseInt(id); // DOES INT COMPARE of MiddleId[0] and id
            // If record is found, end loop and return record
            if (result == 0) {
                isFound = true;
            } else if (result < 0) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        if (isFound) {
            location[0] = middle;
            return record;
        }
        return null;
    }
}

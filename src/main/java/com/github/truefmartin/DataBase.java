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
     * @return the titanic record or an empty record if none found. Set's record's row to the place the
     * ID was found or should have found. Will be negative if not found.
     * @throws DatabaseIsClosedException the database is closed exception if this method is called when the
     * database is closed.
     */
    public BinarySearchResult findRecord(String passengerId) throws DatabaseIsClosedException, RowBoundsException {
        if (!isOpen) {
            throw new DatabaseIsClosedException("database is closed, unable to read row");
        }
        int id = Integer.parseInt(passengerId);

        if (id < 0) {
            throw new RowBoundsException("id is negative, " + passengerId);
        }
        return binarySearch(passengerId);
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
            fileAccess.readBatch(0, records, true);
        } catch (ColumnBoundsException e) {
            logger.error("incorrect column size from get report");
            throw new RuntimeException(e);
        }
        return records;
    }

    public boolean updateRecord(TitanicRecord record) throws DatabaseIsClosedException {
        if (!isOpen) {
            throw new DatabaseIsClosedException("attempted to update record while database is closed");
        }
        if (record.getRowNum() >= numRecords || record.getRowNum() < 0) {
            return false;
        }
        fileAccess.update(record, record.getRowNum());
        return true;
    }

    public boolean deleteRecord(TitanicRecord record) throws DatabaseIsClosedException {
        if (!isOpen) {
            throw new DatabaseIsClosedException("attempted to delete row while database closed");
        }
        if (record.getRowNum() >= numRecords || record.getRowNum() < 0) {
            return false;
        }
        fileAccess.update(new TitanicRecord(), record.getRowNum());
        return true;
    }

    public boolean addRecord(TitanicRecord record) throws DatabaseIsClosedException, RowBoundsException, ColumnBoundsException {
        if (!isOpen) {
            throw new DatabaseIsClosedException("attempted to add record while database closed");
        }
        if (Integer.parseInt(record.passengerId) < 0) {
            throw new RowBoundsException("record IDs can not be negative");
        }
        var searchResult = findInsertRow(record.passengerId);
        if (searchResult == -2) {
            throw new RowBoundsException("a record by that ID is already present");
        }
        if (searchResult == -1) {
            throw new RowBoundsException("there is no room for that record, please delete a record first");
        }
        fileAccess.write(record, searchResult);
        return true;
    }

    /**
     * Close the currently open database and write to config file if anything has changed.
     */
    public void close() throws DatabaseIsClosedException {
        if (!isOpen) {
            throw new DatabaseIsClosedException("attempted to close database while one is not open");
        }
        fileAccess.writeConfigs();
        fileAccess.close();
        isOpen = false;
        fileAccess = null;
        numRecords = 0;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public static class BinarySearchResult {
        TitanicRecord record;
        int rowFound;
        boolean isFound;
        public BinarySearchResult(TitanicRecord record, int rowFound, boolean isFound) {
            this.record = record;
            this.rowFound = rowFound;
            this.isFound = isFound;
        }

    }

    /**
     * Binary Search by record id
     *
     * @param id record key to search for
     * @return the record found, will assign row number if it was found
     */
    public BinarySearchResult binarySearch(String id) {
        int low = 0;
        int high = numRecords - 1;
        int middle = 0;
        boolean isFound = false;
        boolean isNotEmpty = true;
        boolean searchDownIfEmpty = true;
        int diff = 0;
        int intID = Integer.parseInt(id);
        TitanicRecord record = new TitanicRecord();
        BinarySearchResult result = new BinarySearchResult(record, -1, false);
        while (!isFound && (high >= low)) {
            middle = (low + high) / 2;
            try {
                while ( middle >= low && middle <= high) {
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
            if (!isNotEmpty) {
                // If we have already tried searching up and down, break out of main loop.
                if (!searchDownIfEmpty) {
                    break;
                }
                searchDownIfEmpty = false;
                continue;
            }
            String MiddleId = record.passengerId;
            diff = Integer.parseInt(MiddleId) - intID;
            // If record is found, end loop and return record
            if (diff == 0) {
                isFound = true;
            } else if (diff < 0) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        fileAccess.read(middle, record);
        result.rowFound = middle;
        result.record = record;
        result.isFound = isFound;
        return result;
    }

    /**
     * Binary Search by record id, finding an empty row to insert an entry.
     *
     * @param id record key to search for
     * @return the row to insert a record into. -1 if no surrounding empty space, -2 if already present.
     */
    public int findInsertRow(String id) throws RowBoundsException, ColumnBoundsException {
        int low = 0;
        int high = numRecords - 1;
        int middle = 0;
        boolean isFound = false;
        boolean isNotEmpty = true;
        int intID = Integer.parseInt(id);
        TitanicRecord record = new TitanicRecord();
        boolean searchDownIfEmpty = true;
        int diff = 0; // DOES INT COMPARE of MiddleId[0] and id
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
            diff = Integer.parseInt(MiddleId) - intID;
            // If record is found, end loop and return record
            if (diff == 0) {
                isFound = true;
            } else if (diff < 0) {
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        if (isFound) {
            return -2;
        }
        int batchSize = 6;
        int startBatch = Math.max(0, middle - (batchSize/2));
        if (startBatch + batchSize >= numRecords) {
            batchSize = numRecords - startBatch;
        }
        var surrounding = new TitanicRecord[batchSize];
        for (int i = 0; i < surrounding.length; i++)
            surrounding[i] = new TitanicRecord();
        fileAccess.readBatch(startBatch, surrounding, false);
        int above = Integer.MAX_VALUE;
        int below = -1;
        int empty = Integer.MAX_VALUE;
        // Find best place within a small section to place new entry
        for (int i = 0; i < batchSize; i++) {
            var currRec = surrounding[i];
            if (currRec.isFirstFieldEmpty()) {
                empty = i;
            } else if (Integer.parseInt(currRec.passengerId) == intID) {
                return -2;
            } else if (Integer.parseInt(currRec.passengerId) > intID) {
                above = i;
                break;
            } else {
                below = i;
            }
        }
        if (empty > below && empty < above) {
            return startBatch+empty;
        } else {
            return -1;
        }
    }

}

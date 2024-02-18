package com.github.truefmartin;

import com.github.truefmartin.exceptions.ColumnBoundsException;
import com.github.truefmartin.exceptions.RowBoundsException;
import com.github.truefmartin.records.Writeable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * The type Raf table.
 *
 * @param <T> the type parameter that implements Writeable
 */
public class FixedRecordsAccess<T extends Writeable> implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(FixedRecordsAccess.class);

    private final int[] colSizes;
    private final int NUM_COLUMNS;
    private final String FILE_NAME;
    private final String CONFIG_FILE_NAME;
    private RandomAccessFile stream;


    /**
     * close will be used in later homeworks when I turn this class into an actual extension of
     * RandomAccessFile. Methods will throw exceptions that need to be caught instead of handling them inside
     * Fixed Records Access. Fixed Records Access will be used in a 'try-with-resources' instead of the testing state it is in now.
     */
    @Override
    public void close(){
        closeFile();
    }

    /**
     * The enum of the Random Access File status.
     */
    public enum FileStatus {
        /**
         * Read Random Access File status.
         */
        READ,
        /**
         * Write Random Access File status.
         */
        WRITE,
        /**
         * Closed Random Access File status.
         */
        CLOSED;

        /**
         * Random Access File open option string.
         *
         * @return the string, 'r' for READ, 'rw' for WRITE
         */
        String fileOpenOption(){
            switch (this) {
                case READ: return "r";
                case WRITE: return "rw";
                case CLOSED: return "";
            }
            return "";
        }

        /**
         * Get the enum RafStatus associated with the string of Random Access File's options .
         *
         * @param option the option "r" or "rw"
         * @return the raf status READ, WRITE, or CLOSED
         */
        FileStatus statusFromOption(String option) {
            switch (option) {
                case "r": return READ;
                case "rw": return WRITE;
                default: return CLOSED;
            }
        }
    }
    private FileStatus status;



    private int numRecords;
    private final int RECORD_SIZE;

    /**
     * Instantiates a new Fixed Record Access table from a config file.
     *
     * @param configFileName the config file name
     * @param fileName    the out file name
     * @param status         the status to open the stream as
     */
    public FixedRecordsAccess(String configFileName, String fileName, FileStatus status) throws FileNotFoundException {
        this.CONFIG_FILE_NAME = configFileName;
        this.FILE_NAME = fileName;
        this.status = status;
        String data;
        try (RandomAccessFile configStream = new RandomAccessFile(CONFIG_FILE_NAME, "r")){
            this.stream = new RandomAccessFile(FILE_NAME, status.fileOpenOption());
            data = configStream.readLine();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("a file was not found by that name");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] configValues = data.split(" ");
        this.numRecords = Integer.parseInt(configValues[0]);
        this.NUM_COLUMNS = configValues.length - 1;
        this.colSizes = new int[NUM_COLUMNS];
        int sumOfSizes = 0;
        // Loop starts at 1 because first value of config file is numRecords instead of colSizes
        for(int i = 1; i < configValues.length; i++) {
            colSizes[i - 1] = Integer.parseInt(configValues[i]);
            sumOfSizes += colSizes[i - 1];
        }
        // The number of bytes in a row. Each value length + a space between each + a newline at the end
        this.RECORD_SIZE = sumOfSizes + NUM_COLUMNS + 1;
        try {
            stream = new RandomAccessFile(FILE_NAME, status.fileOpenOption());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
    /**
     * Instantiates a new Fixed Record Access table.
     *
     * @param configFileName the config file name
     * @param outFileName    the out file name
     * @param status           the status to open the Fixed Record Access in, READ, CLOSED, WRITE
     * @param colSizes         the column sizes
     */
    public FixedRecordsAccess(String configFileName, String outFileName, FileStatus status, int... colSizes) {
        this.CONFIG_FILE_NAME = configFileName;
        this.FILE_NAME = outFileName;
        this.status = status;
        this.colSizes = colSizes;
        this.NUM_COLUMNS = colSizes.length;
        if (this.status != FileStatus.CLOSED) {
            try {
                this.stream = new RandomAccessFile(outFileName, status.fileOpenOption());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        this.RECORD_SIZE = Arrays.stream(colSizes).sum() + NUM_COLUMNS + 1;
        this.numRecords = 0;
    }


    /**
     * Write to a specific row in the RAF. The file write position is returned to the
     * location it was before this write.
     *
     * @param data the data to write, of type Writeable
     * @param row  the row to write on
     */
    public void write(T data, int row) {
        try {
            long currentPosition = stream.getFilePointer();
            stream.seek((long) row * RECORD_SIZE);
            write(data, true);
            stream.seek(currentPosition);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write to a specific row in the RAF. The file write position is returned to the
     * location it was before this write. Does not increase FixedRecordAccess's total count of records
     *
     * @param data the data to write, of type Writeable
     * @param row  the row to write on
     */
    public void update(T data, int row) {
        try {
            long currentPosition = stream.getFilePointer();
            stream.seek((long) row * RECORD_SIZE);
            write(data, false);
            stream.seek(currentPosition);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write a record to file at current file pointer.
     *
     * @param data the data
     * @param increaseNumRecord should increase numRecords
     */
    public void write(T data, boolean increaseNumRecord) {
        try {
            StringBuilder stringBuilder = new StringBuilder(RECORD_SIZE);
            for(int size: colSizes) {
                if (!data.hasNext()) {
                    throw new ColumnBoundsException("Number of columns for data type " + data.getClass().getName()
                            + " does not match number of columns for Fixed Records Access: " + NUM_COLUMNS);
                }
                String next = data.getNext();
                if (next.length() > size) {
                    logger.warn("input data is longer{} than max length{} for column, truncating '{}'",
                            next.length(), size, next);
                    next = next.substring(0, size);
                }
                String formatter = "%" + size + "s" + " ";

                stringBuilder.append(String.format(formatter, next));
            }
            stringBuilder.append('\n');
            stream.write(stringBuilder.toString().getBytes());
            if (increaseNumRecord) {
                numRecords += 1;
            }
        } catch (IOException | ColumnBoundsException e) {
            throw new RuntimeException(e);
        }
        data.resetNext();
    }

    /**
     * Write an empty record starting with -1 in the first column.
     *
     */
    public void writeEmpty() {
        try {
            StringBuilder stringBuilder = new StringBuilder(RECORD_SIZE);
            String next = "-1";
            String formatter = "%" + colSizes[0] + "s" + " ";

            stream.write(String.format(formatter, next
                    .substring(0, Math.min(next.length(), colSizes[0]))).getBytes());
            next = "";
            for(int i = 1; i < colSizes.length; i++) {
                formatter = "%" + colSizes[i] + "s" + " ";
                stream.write(String.format(formatter, next).getBytes());
            }
            numRecords += 1;
            stream.write('\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read one record at position recordNum, loading 'data' with results.
     *
     * @param recordNum the record num
     * @param data      the data used to figure the column size, modified
     *                  during reading with the results
     * @return false if record num is outside of bounds.
     */
    public boolean read(int recordNum, T data) {
        if ((recordNum < 0) || (recordNum >= numRecords)) {
            return false;
        }

        try {
            stream.seek((long) recordNum * RECORD_SIZE);
            String row = stream.readLine();
            // Populate record with row data
            if (row == null) {
                return false;
            }
            assignRecord(data, row);
        } catch (IOException | ColumnBoundsException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Read one record at position recordNum, loading 'data' with results.
     * Immediately returns false if key is the empty value, '-1'.
     *
     * @param recordNum the record num
     * @param data      the data used to figure the column size, modified
     *                  during reading with the results
     * @return false if it is an empty record.
     */
    public boolean readFalseOnEmpty(int recordNum, T data) throws RowBoundsException{
        if ((recordNum < 0) || (recordNum >= numRecords)) {
            throw new RowBoundsException("search for " + recordNum + " is out of bounds from table with size " + numRecords);
        }

        try {
            stream.seek((long) recordNum * RECORD_SIZE);
            String row = stream.readLine();
            String key = row.substring(0, colSizes[0]).strip();
            if (key.equals("-1")) {
                return false;
            }
            // Populate record with contents of row
            assignRecord(data, row);
        } catch (IOException | ColumnBoundsException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private void assignRecord(T data, String row) throws ColumnBoundsException {
        int prevIndex = 0;
        for(int columnSize: colSizes) {
            if (!data.hasNext()) {
                throw new ColumnBoundsException("Number of columns for data type " + data.getClass().getName()
                        + " does not match number of columns for Fixed Records Access: " + NUM_COLUMNS);
            }
            data.setNext(row.substring(prevIndex, columnSize+prevIndex).strip());
            // Next value will start from end of current value(columnSize + prevIndex) + 1 space.
            prevIndex = columnSize + prevIndex + 1;
        }
        data.reassignFields();
        data.resetNext();
    }

    /**
     * Read multiple records starting at position recordNum,
     * loading 'data[]' with results.
     *
     * @param recordNum the record num
     * @param data      the data used to figure the column size, modified
     *                  during reading with the results
     * @return false if record num is outside of bounds
     */
    public boolean readBatch(int recordNum, T[] data, boolean ignoreEmpty) throws RowBoundsException, ColumnBoundsException{
        if ((recordNum < 0) || (recordNum >= numRecords)) {
            return false;
        }
        if (recordNum + data.length >= numRecords) {
            throw new RowBoundsException("Miscalculated number of records." +
                    "\nTried to read more than is available in file. " +
                    data.getClass().getSimpleName() +
                    "--Requested records: "  + recordNum + "-"
                    + (recordNum + data.length) +
                    ", actual available: " + numRecords);
        }
        try {
            stream.seek((long) recordNum * RECORD_SIZE);
            int relativeIndex = 0;
            String row;
            while ((row = stream.readLine()) != null) {
                if (relativeIndex >= data.length) {
                    break;
                }
                if (ignoreEmpty && row.substring(0, colSizes[0]).trim().equals("-1")) {
                    continue;
                }

                int columnCharIndex = 0;
                for (int columnSize : colSizes) {
                    if (!data[relativeIndex].hasNext()) {
                        throw new ColumnBoundsException("Number of columns for data type " + data.getClass().getName()
                                + " does not match number of columns for Fixed Records Access: " + NUM_COLUMNS);
                    }
                    data[relativeIndex].setNext(row.substring(columnCharIndex, columnSize + columnCharIndex).strip());
                    // Next value will start from end of current value(columnSize + columnCharIndex) + 1 space.
                    columnCharIndex = columnSize + columnCharIndex + 1;
                }
                data[relativeIndex].reassignFields();
                data[relativeIndex].resetNext();
                relativeIndex++;
            }
        } catch(IOException e){
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Read records until the desired result is found, or an empty record.
     * Designed for use with a hashed table.
     *
     * @param recordNumStart the record num to start searching at
     * @param data      the data used to figure the column size, modified
     *                  during reading with the results
     * @param desired the string to search for
     * @param columnNumber the column in which the desired string is found in
     * @return false if not found or recordNumStart is out of bounds, true otherwise
     */
    public boolean readAndFind(int recordNumStart, T data, String desired, int columnNumber) {
        if ((recordNumStart < 0) || (recordNumStart >= numRecords)) {
            return false;
        }
        try {
            stream.seek((long) recordNumStart * RECORD_SIZE);
            int currentRecord = recordNumStart - 1;
            String row;
            while((row = stream.readLine()) != null) {
                currentRecord++;
                assignRecord(data, row);
                // If empty result, desired was not found. Return false
                if (data.isEmpty()) {
                    return false;
                }
                // If the data is equal to desired, return true
                if (data.getValue(columnNumber).equals(
                        // Take substring of desired to match what is in records
                        desired.substring(0, Math.min(colSizes[columnNumber], desired.length())))
                ) {
                    return true;
                }
                // If we reach end of records,
                if(currentRecord == numRecords - 1) {
                    currentRecord = -1;
                    // Reset file to beginning
                    stream.seek(0);
                    continue;
                }
                // If we have traveled full circle and the result before where we started is not it,
                if(currentRecord == recordNumStart - 1) {
                    return false;
                }
            }
            return false;
        } catch (IOException | ColumnBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    // Returns the row number of the next empty (above or below), updates Data with the last found
    // non-empty record
    public int findNextEmpty(int recordNumStart, T data, boolean searchUp) {
        if ((recordNumStart < 0) || (recordNumStart >= numRecords)) {
            return -1;
        }
        try {
            int currentRecord = recordNumStart;
            String row;
            while((row = stream.readLine()) != null) {
            stream.seek((long) currentRecord * RECORD_SIZE);
                if (row.substring(0, colSizes[0]).trim().equals("-1")) {
                    return currentRecord;
                }
                assignRecord(data, row);
                // If searching down,
                currentRecord = searchUp ? currentRecord + 1: currentRecord - 1;
            }
            return -1;
        } catch (IOException | ColumnBoundsException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Close file
     *
     * @return true on success
     */
    public boolean closeFile() {
        numRecords = 0;
        if (status != FileStatus.CLOSED) {
            status = FileStatus.CLOSED;
            try {
                stream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false;
    }

    /**
     * Sets read mode.
     */
    public void setReadMode() {
        if (status == FileStatus.READ) {
            return;
        }
        setNewStream(FileStatus.READ);
    }

    /**
     * Sets write mode. Starts num records at 0. Deletes current file
     */
    public void setWriteModeNew() {
        numRecords = 0;
        closeFile();
        try {
            Files.deleteIfExists(Path.of(FILE_NAME));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setNewStream(FileStatus.WRITE);
    }

    /**
     *  Opens an already present database file. Starts num records at 0.
     */
    private void setNewStream(FileStatus streamOption) {
        closeFile();
        if (streamOption == FileStatus.CLOSED) {
            return;
        }
        status = streamOption;
        try {
            stream = new RandomAccessFile(FILE_NAME, streamOption.fileOpenOption());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write configs.
     */
    public void writeConfigs() {
        StringBuilder outString = new StringBuilder();
        outString.append(numRecords).append(' ');
        for(int colSize: colSizes) {
            outString.append(colSize).append(' ');
        }
        try {
            Files.deleteIfExists(Path.of("./" + CONFIG_FILE_NAME));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try(RandomAccessFile configStream = new RandomAccessFile(CONFIG_FILE_NAME, "rw")){
            configStream.write(outString.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNumRecords() {
        return numRecords;
    }
}

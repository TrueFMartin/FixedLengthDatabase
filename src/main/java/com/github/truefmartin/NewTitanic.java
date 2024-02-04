package com.github.truefmartin;

import com.github.truefmartin.records.TitanicRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.*;

public class NewTitanic {
    private final String fileName;
    private int numRecords;
    private final int[] columns;

    private static final Logger logger = LogManager.getLogger(NewTitanic.class);

    public NewTitanic(String fileName, int[] columns) {
        this.fileName = fileName;
        this.numRecords = 0;
        this.columns = columns;
    }

    public void setNumRecords(int numRecords) {
        this.numRecords = numRecords;
    }

    /**
     * Build the database file from the provided csv. The old database file is temporarily renamed
     * before it is removed, in case building the new database fails.
     */
    public int start() throws FileNotFoundException {
        InputStreamReader inStream;
        try {
            String csvFile = fileName + ".csv";
            inStream = new InputStreamReader(Files.newInputStream(Path.of(csvFile)), StandardCharsets.UTF_8);
            logger.info("opening csv file, {}", fileName);
        } catch (NoSuchFileException | FileNotFoundException e) {
            throw new FileNotFoundException("File by that name was not found");
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Rename old .data file to *.data-backup in case building the new file fails.
        String outFile = fileName + ".data";
        Path temporaryBackup = null;
        try {
            temporaryBackup = Files.move(Path.of(outFile), Path.of(outFile + "-backup"), StandardCopyOption.REPLACE_EXISTING);
        } catch (NoSuchFileException e) {
            logger.info("no current database file to delete");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FixedRecordsAccess<TitanicRecord> writer = new FixedRecordsAccess<>(fileName + ".config",
                fileName + ".data", FixedRecordsAccess.FileStatus.WRITE, columns);
        BufferedReader reader = new BufferedReader(inStream, 300);
        // Create a concurrent queue of size 500 or numRecords, whatever is less
        int size = numRecords == 0? 100: Math.min(numRecords, 100);
        ArrayBlockingQueue<TitanicRecord> queue = new ArrayBlockingQueue<>(size);
        FutureTask<Integer> readFuture = new FutureTask<>(new ReadTitanicCsv(reader, queue));
        FutureTask<Integer> writeFuture = new FutureTask<>(new WriteFixedFile(writer, queue));

        Thread read = new Thread(readFuture);
        Thread write = new Thread(writeFuture);
        read.start();
        write.start();

        int readCount = 0;
        try {
            readCount = readFuture.get();
            if (readCount == 0) {
                logger.error("read 0 records from csv");
            }
            reader.close();
        } catch (InterruptedException e) {
            logger.error("readFuture timed out in adding to queue", e);
        } catch (ExecutionException e) {
            logger.error("failed to open csv file", e);
        } catch (IOException e) {
            logger.error("failed to close csv", e);
            throw new RuntimeException(e);
        }

        int writeCount = 0;
        try {
            writeCount = writeFuture.get();
            if (writeCount == 0) {
                logger.error("wrote 0 records to new database");
            }
            writer.writeConfigs();
            writer.close();
        } catch (InterruptedException e) {
            logger.error("writeFuture timed out in taking from the queue", e);
        } catch (ExecutionException e) {
            logger.error("get future from WriteTitanicCsv failed", e);
            throw new RuntimeException(e);
        }
        // If everything has been successful, delete the backup file (previous .data file)
        if (readCount == writeCount && readCount != 0) {
            try {
                if (temporaryBackup != null) {
                    logger.info("deleting temporary backup");
                    Files.deleteIfExists(temporaryBackup);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Successfully read and wrote correct number of records.
            return writeCount;
        }
        return -1;
    }
}

class ReadTitanicCsv implements Callable<Integer> {
    private final BufferedReader reader;
    private final ArrayBlockingQueue<TitanicRecord> queue;

    ReadTitanicCsv(BufferedReader reader, ArrayBlockingQueue<TitanicRecord> queue) {
        this.reader = reader;
        this.queue = queue;
    }

    @Override
    public Integer call() throws IOException, InterruptedException {
        int numRead = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            String[] columns = line.split(",");
            if (columns.length > 0) {
                // If offering a record fails from timeout, return false.
                if (!queue.offer(new TitanicRecord(columns), 500L, TimeUnit.MILLISECONDS))
                    throw new InterruptedException("blocked on queue, deadline exceeded");
                numRead++;
            }
        }
        // Offer signal to close queue poller
        queue.offer(new TitanicRecord(), 500L, TimeUnit.MILLISECONDS);
        return numRead;
    }
}



class WriteFixedFile implements Callable<Integer> {
    private final FixedRecordsAccess<TitanicRecord> writer;
    private final ArrayBlockingQueue<TitanicRecord> queue;

    WriteFixedFile(FixedRecordsAccess<TitanicRecord> writer, ArrayBlockingQueue<TitanicRecord> queue) {
        this.writer = writer;
        this.queue = queue;
    }

    // Integer returned by this method is the number of records from the queue that were written.
    // In the actual database file there will be twice as many records due to the blank records between.
    // This is reflected in the cofnig file.
    @Override
    public Integer call() throws InterruptedException {
        int numRead = 0;
        // Continue pulling from poll until either the poll writer sends an empty record or
        // the 500ms deadline is exceeded.
        for (;;) {
            TitanicRecord record;
            if ((record = queue.poll(500L, TimeUnit.MILLISECONDS)) == null) {
                throw new InterruptedException("queue poll deadline exceeded");
            }
            // If queue writer sent empty record, stop reading queue
            if (record.isEmpty()) {
                break;
            }
            // Write record
            writer.write(record);
            // Write empty block
            writer.writeEmpty();
            numRead++;
        }
        return numRead;
    }
}

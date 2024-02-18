package com.github.truefmartin;

import com.github.truefmartin.exceptions.ColumnBoundsException;
import com.github.truefmartin.exceptions.DatabaseIsClosedException;
import com.github.truefmartin.exceptions.RowBoundsException;
import com.github.truefmartin.records.TitanicRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.*;

public class Control {

    private static final Logger logger = LogManager.getLogger(Control.class);

    private final DataBase dataBase;
    private static final Menu menu = new Menu();
    private static final int[] COLUMN_SIZES = {4,15,20,4,20,6,10};

    public Control() {
        dataBase = new DataBase();
    }


    /**
     * Start the main loop of the program. Prints the menu and gets input from the user.
     */
    public void start() {
        Menu.Selection selection = Menu.Selection.CREATE;
        while (selection != Menu.Selection.QUIT) {
            menu.displayMenu();
            selection = menu.getSelection();
            try {
                menuResponse(selection);
            } catch (RowBoundsException | ColumnBoundsException | DatabaseIsClosedException | FileNotFoundException e) {
                logger.error("input caused the following error: {}", e.getMessage());
            }
        }
    }

    /*
     * Calls database methods depending on passed in menu selection. Throws exceptions from the database.
     * Reads input from user to pass to database.
     */
    private void menuResponse(Menu.Selection selection) throws RowBoundsException, DatabaseIsClosedException, InputMismatchException, FileNotFoundException, ColumnBoundsException {
        Scanner scanner = new Scanner(System.in);
        switch (selection) {
            case CREATE:
                if (dataBase.create(scanner.nextLine(), COLUMN_SIZES)) {
                    System.out.println("Database successfully created.\n\t**\tOpen the database to begin working.");
                }
                break;
            case OPEN:
                if (dataBase.openDb(scanner.nextLine())) {
                    System.out.println("Database successfully opened.");
                } else {
                    System.out.println("\t**\tDatabase failed to open");
                    if (dataBase.isOpen()) {
                        System.out.println("\t**\tYou already have a database open");
                        System.out.println("\t**\tplease close the open database");
                    }
                }
                break;
            case CLOSE:
                dataBase.close();
                System.out.println("Database closed.");
                break;
            case READ: {
                int row;
                try {
                    row = scanner.nextInt();
                } catch (InputMismatchException e) {
                    throw new InputMismatchException("invalid input on read row command");
                }
                var record = dataBase.readRow(row);
                if (record != null) {
                    record.print();
                }
                break;
            }
            case DISPLAY:
                findAndDisplay(scanner);
                break;
            case REPORT:
                var records = dataBase.getReport();
                System.out.println(Arrays.toString(records));
                break;
            case UPDATE: {
                var record = findAndDisplay(scanner);
                // If record is populated, and user input is accepted
                if (record != null && record.isPopulated() && record.getRowNum() >= 0) {
                    System.out.println("\n\t**\tEnter value to update entry's attribute. Enter no input to skip attribute.");
                    if (scanInRecord(scanner, record, false) && dataBase.updateRecord(record)) {
                        System.out.println("Update successful.");
                        break;
                    }
                }
                System.out.println("Update failed");
                break;
            }
            case DELETE:{
                var record = findAndDisplay(scanner);
                // If record is populated, delete record
                if (record != null && record.isPopulated() && record.getRowNum() >= 0) {
                    System.out.println("\n\t**\tDeleting Record for passenger ID #" + record.passengerId);
                    if (dataBase.deleteRecord(record)) {
                        System.out.println("Delete successful.");
                        break;
                    }
                }
                System.out.println("Delete failed, empty record");
                break;
            }
            case ADD:{
                TitanicRecord record = new TitanicRecord();
                if(!scanInRecord(scanner, record, true))
                    break;
                if (dataBase.addRecord(record)) {
                    System.out.println("\n\t**\tAdded Record for passenger ID #" + record.passengerId);
                    break;
                }
                System.out.println("Add failed, no empty space or a record already exists with that ID.");
                break;
            }
            case QUIT:
                try {
                    dataBase.close();
                } catch(DatabaseIsClosedException ignored) {}
                break;
            case ERROR:
                break;
        }
    }

    private TitanicRecord findAndDisplay(Scanner scanner) throws DatabaseIsClosedException, RowBoundsException {
        String id = scanner.nextLine();
        var result = dataBase.findRecord(id);
        var found = result.record;
        if (found == null || !result.isFound || result.rowFound < 0) {
            System.out.println("\t**\tNo passenger by ID #" + id + " was found");
        } else {
            found.setRowNum(result.rowFound);
            found.print();
        }
        return found;
    }
    // Uses scan to fill a record. Can pass in loaded scanner with space seperated column numbers to auto-select
    // columns to write.
    private boolean scanInRecord(Scanner scanner, TitanicRecord record, boolean includeId) throws InputMismatchException {
        boolean hasBeenUpdated = false;
        // If ID should be included, start on column 0
        for (int i = includeId? 0: 1; i < record.getNumAttributes(); i++) {
            System.out.printf("\n\t%s:\t%s -->", record.getAttributeName(i), record.getValue(i));
            String in = scanner.nextLine();
            // If the user didn't just press 'enter' and update attribute value succeeds
            if (!Objects.equals(in, "") && record.setAttributeValue(i, in)) {
                hasBeenUpdated = true;
            }
        }
        return hasBeenUpdated;
    }
}

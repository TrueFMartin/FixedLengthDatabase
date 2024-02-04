package com.github.truefmartin;

import com.github.truefmartin.exceptions.DatabaseIsClosedException;
import com.github.truefmartin.exceptions.RowBoundsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

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
            } catch (RowBoundsException | DatabaseIsClosedException | FileNotFoundException e) {
                logger.error("input caused the following error: {}", e.getMessage());
            }
        }
    }

    /*
     * Calls database methods depending on passed in menu selection. Throws exceptions from the database.
     * Reads input from user to pass to database.
     */
    private void menuResponse(Menu.Selection selection) throws RowBoundsException, DatabaseIsClosedException, InputMismatchException, FileNotFoundException {
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
            case READ:
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
            case DISPLAY:
                String id = scanner.nextLine();
                var found = dataBase.findRecord(id);
                if (found == null || found.isEmpty()) {
                    System.out.println("\t**\tNo passenger by ID #" + id + " was found");
                } else {
                    found.print();
                }
                break;
            case REPORT:
                var records = dataBase.getReport();
                System.out.println(Arrays.toString(records));
                break;
            case UPDATE:
            case DELETE:
            case ADD:
                System.out.print("\nTODO\n");
                break;
            case QUIT:
                dataBase.close();
                break;
            case ERROR:
                break;
        }
    }

}

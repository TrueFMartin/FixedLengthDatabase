package com.github.truefmartin;

import java.util.LinkedHashMap;
import java.util.Scanner;

public class Menu {
    private final LinkedHashMap<String, MenuOption> menuMap;
    public Menu() {
        MenuOption optionCreate = new MenuOption("1) Create new database", "Enter file prefix: ", Selection.CREATE);
        MenuOption optionOpen = new MenuOption("2) Open existing database", "Enter database name: ", Selection.OPEN);
        MenuOption optionClose = new MenuOption("3) Close an open database", "", Selection.CLOSE);
        MenuOption optionRead = new MenuOption("4) Read a record by row NUMBER", "Enter row number: ", Selection.READ);
        MenuOption optionDisplay = new MenuOption("5) Display a record by row KEY", "Enter key: ", Selection.DISPLAY);
        MenuOption optionReport = new MenuOption("6) Display first 10 NON-EMPTY records", "Displaying first 10 rows.\n", Selection.REPORT);
        MenuOption optionUpdate = new MenuOption("7) Update a record by row KEY", "Enter key: ", Selection.UPDATE);
        MenuOption optionDelete = new MenuOption("8) Delete a record by row KEY", "Enter key number: ", Selection.DELETE);
        MenuOption optionAdd = new MenuOption("9) Add a record to the database", "", Selection.ADD);
        MenuOption optionQuit = new MenuOption("0) Quit", "", Selection.QUIT);

        menuMap = new LinkedHashMap<>(10);
        menuMap.put("1", optionCreate);
        menuMap.put("2", optionOpen);
        menuMap.put("3", optionClose);
        menuMap.put("4", optionRead);
        menuMap.put("5", optionDisplay);
        menuMap.put("6", optionReport);
        menuMap.put("7", optionUpdate);
        menuMap.put("8", optionDelete);
        menuMap.put("9", optionAdd);
        menuMap.put("0", optionQuit);
    }

    public void displayMenu() {
        menuMap.forEach((String k, MenuOption v) -> System.out.println(v.display));
    }


    public Selection getSelection() {
        System.out.print("\tInput: ");
        Scanner scan = new Scanner(System.in);
        String in = scan.next();
        MenuOption selected = menuMap.get(in);
        if (selected == null) {
            System.out.format("\nInvalid input of '%s', please try again.\n", in);
            return getSelection();
        }
        System.out.print(selected.instruction);
        return selected.selection;
    }

    public enum Selection{CREATE, OPEN, CLOSE, READ, DISPLAY, REPORT, UPDATE, DELETE, ADD, QUIT, ERROR}

    private static class MenuOption {
        String display;
        String instruction;
        Selection selection;

        public MenuOption(String display, String instruction, Selection selection) {
            this.display = display;
            this.instruction = instruction;
            this.selection = selection;
        }
    }
}

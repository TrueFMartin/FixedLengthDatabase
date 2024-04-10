package com.github.truefmartin;

import java.util.LinkedHashMap;
import java.util.Scanner;

public class Menu {
    private final LinkedHashMap<String, MenuOption> menuMap;
    public Menu() {

        menuMap = new LinkedHashMap<>(10);
        menuMap.put("1",
                new MenuOption(
                        "1) GET menu by restaurant name and city",
                        Selection.GET_MENUS,
                        "Enter restaurant name: ",
                        "Enter city name: "));
        menuMap.put("2",
                new MenuOption(
                        "2) ADD Order by dish name",
                        Selection.ADD_ORDER,
                        "Enter dish name: "));
        menuMap.put("3",
                new MenuOption(
                        "3) GET orders by restaurant name and city",
                        Selection.GET_ORDERS,
                        "Enter restaurant name: ",
                        "Enter city name: "));
        menuMap.put("4",
                new MenuOption(
                        "4) DELETE order by order number",
                Selection.DELETE_ORDER));
        menuMap.put("5",
                new MenuOption(
                        "5) ADD new dish to restaurant",
                Selection.ADD_DISH,
                        "Enter restaurant name: ",
                        "Enter city name: "
                ));
        menuMap.put("ls",
                new MenuOption(
                        "ls) LIST all entries for a relation",
                Selection.LIST_RELATION,
                        "Enter relation name ('dish', 'food_order', 'menu_item', 'restaurant'): "
                ));
        menuMap.put("0", new MenuOption(
                "0) Quit",
                Selection.QUIT
        ));
    }

    public void displayMenu() {
        menuMap.forEach((String k, MenuOption v) -> System.out.println(v.display));
    }


    protected MenuOption getMenuOption() {
        System.out.print("\tInput: ");
        Scanner scan = new Scanner(System.in);
        String in = scan.next();
        MenuOption selected = menuMap.get(in);
        if (selected == null) {
            System.out.format("\nInvalid input of '%s', please try again.\n", in);
            return getMenuOption();
        }
        return selected;
    }

    public enum Selection{GET_MENUS, ADD_ORDER, GET_ORDERS, DELETE_ORDER, ADD_DISH, LIST_RELATION, QUIT}

    protected static class MenuOption {
        String display;
        String[] instructions;
        Selection selection;

        public MenuOption(String display, Selection selection, String ... instructions) {
            this.display = display;
            this.selection = selection;
            this.instructions = instructions;
        }
    }
}

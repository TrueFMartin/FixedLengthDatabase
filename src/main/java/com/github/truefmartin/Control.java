package com.github.truefmartin;

import com.github.truefmartin.exceptions.EmptyResultsException;
import com.github.truefmartin.models.*;
import com.github.truefmartin.views.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.*;

public class Control {

    private static final Logger logger = LogManager.getLogger(Control.class);

    private static final Menu menuUI = new Menu();
    private final Model model;

    public Control(Model model) {
        this.model = model;
    }

    /**
     * Start the main user-facing loop of the program. Prints the menu and gets input from the user.
     */
    public void start() {
        Menu.MenuOption menuOption;
        do {
            menuUI.displayMenu();
            menuOption = menuUI.getMenuOption();
            try {
                menuResponse(menuOption);
            } catch (InputMismatchException | EmptyResultsException e) {
                logger.error("input caused the following error: {}", e.getMessage());
            }
        } while (menuOption.selection != Menu.Selection.QUIT);
    }

    /*
     * Calls database methods depending on passed in menu selection. Throws exceptions from the database.
     * Reads input from user to pass to database.
     */
    private void menuResponse(Menu.MenuOption menuOption) throws EmptyResultsException, InputMismatchException {
        if (menuOption.selection == Menu.Selection.QUIT) {
            return;
        }
        Scanner scanner = new Scanner(System.in);
        String[] lines = new String[menuOption.instructions.length];
        for (int i = 0; i < menuOption.instructions.length; i++) {
            System.out.println(menuOption.instructions[i]);
            lines[i] = scanner.nextLine();
        }
        switch (menuOption.selection) {
            /*
           Prompt the user for a restaurant name and city.
           Find and list all menu items available from that restaurant location.
           Output the restaurant name once (echo the user input) and then list the dish name and price for each
           available menu item.
            */
            case GET_MENUS: {
                displayRestaurantMenus(lines);
                break;
            }
            /*
            Prompt the user for the dishName of the item that they want to order.
            If the dish is found, display the itemNo, restaurantName, city and price for all matches.
            Prompt the user for the itemNo for the MenuItem that they want to order.
            Add the itemNo, current time, and current date to the FoodOrder table.
             */
            case ADD_ORDER: {
                // Get and display the results of the dish search
                var results = getAndDisplayDishResults(lines);
                // Get user selection and save the order
                saveDishOrder(scanner, results);
                break;
            }
            /*
            Prompt the user for the restaurantName and city .
            If the restaurant is found, display all orders for that restaurant.
            Display the restaurantName once (echo the user input) and
            then display the dishName, price, date, and time for all orders for that restaurant.
            */
            case GET_ORDERS: {
                // Display all food orders for a restaurant
                displayRestaurantOrders(lines);
                break;
            }
            /*
             Display all food orders (orderNo, dishName, restaurantName, date, time).
             Prompt the user for the orderNo of the order that they wish to cancel.
             Remove that order from the FoodOrder table.
             */
            case DELETE_ORDER: {
                // Get and display all orders, form a map between order numbers and orders
                HashMap<Integer, FoodOrderEntity> orderMap = getAndDisplayOrderMap();
                // Get user selection and delete the order
                deleteOrder(scanner, orderMap);
                break;
            }

            /*
            Prompt the user for the restaurantName and city.
            If the restaurant is found, prompt for the name, type, and price of the new dish.
            Assume that the dish is unique.
            Insert it into the Dish table. Insert it into the MenuItem table.
             */
            case ADD_DISH: {
                // Get matching restaurant
                RestaurantEntity restaurant = getRestaurant(lines);
                // Prompt for dish details and add the dish
                promptAndAddDish(scanner, restaurant);
                break;
            }
            default:
                throw new InputMismatchException("Invalid input, please enter a valid menu option.");
        }
    }

    private void displayRestaurantMenus(String[] lines) throws EmptyResultsException {
        if (invalidRestaurantCity(lines)) {
            throw new InputMismatchException("Invalid input, please enter a restaurant name and city.");
        }
        var restaurantName = lines[0];
        var cityName = lines[1];
        var displays = model.getMenusOfRestaurant(restaurantName, cityName);
        System.out.println("Restaurant: " + restaurantName + ", City: " + cityName);
        for (DisplayDishMenu display :
                displays
        ) {
            System.out.println("-".repeat(20));
            System.out.println(display);
        }
        System.out.println("-".repeat(20));
    }

    private List<DisplayRestaurantMenu> getAndDisplayDishResults(String[] lines) throws EmptyResultsException {
        if (lines.length != 1 || Objects.equals(lines[0], "")) {
            throw new InputMismatchException("Invalid input, please enter a dish name.");
        }
        var dishName = lines[0];
        var results = model.getMenusOfDish(dishName);
        System.out.println("Dish: " + dishName);
        for (DisplayRestaurantMenu display :
                results
        ) {
            System.out.println("-".repeat(20));
            System.out.println(display);
        }
        System.out.println("-".repeat(20));
        return results;
    }

    private void saveDishOrder(Scanner scanner, List<DisplayRestaurantMenu> results) throws EmptyResultsException {
        System.out.print("Enter itemNo to add to orders: ");
        var itemNoStr = scanner.nextLine();
        int itemNo;
        try {
            itemNo = Integer.parseInt(itemNoStr);
        } catch (NumberFormatException e) {
            throw new InputMismatchException("input of " + itemNoStr + " was not able to be translated to an itemNo");
        }

        var possibleMenu = results.stream().filter((var m) -> m.getItemNo() == itemNo).findFirst();
        if (possibleMenu.isEmpty()) {
            throw new EmptyResultsException("input of " + itemNo + " did not match a menuItem");
        }
        var menuDisplay = possibleMenu.get();
        MenuItemEntity menu = menuDisplay.getMenu();
        model.addOrder(menu);
    }

    private void displayRestaurantOrders(String[] lines) throws EmptyResultsException {
        if (invalidRestaurantCity(lines)) {
            throw new InputMismatchException("Invalid input, please enter a restaurant name and city.");
        }
        var restaurantName = lines[0];
        var cityName = lines[1];
        List<DisplayDishMenuOrder> results = model.getOrdersOfRestaurant(restaurantName, cityName);
        for (DisplayDishMenuOrder result :
                results) {
            System.out.println("-".repeat(20));
            System.out.println(result);
        }
        System.out.println("-".repeat(20));
    }

    private HashMap<Integer, FoodOrderEntity> getAndDisplayOrderMap() throws EmptyResultsException {
        List<DisplayRestaurantDishOrder> result = model.getAllOrders();
        HashMap<Integer, FoodOrderEntity> orderMap = new HashMap<>();
        for (DisplayRestaurantDishOrder display :
                result
        ) {
            System.out.println("-".repeat(20));
            System.out.println(display);
            orderMap.put(display.getOrder().getOrderNo(), display.getOrder());
        }
        System.out.println("-".repeat(20));
        return orderMap;
    }

    private void deleteOrder(Scanner scanner, HashMap<Integer, FoodOrderEntity> orderMap) throws EmptyResultsException {
        // Get itemNo to add a new order to food_order relation
        System.out.print("Enter orderNo to remove: ");
        var orderNoStr = scanner.nextLine();
        int orderNo;
        try {
            orderNo = Integer.parseInt(orderNoStr);
        } catch (NumberFormatException e) {
            throw new InputMismatchException("input of " + orderNoStr + " was not able to be translated to an orderNo");
        }
        FoodOrderEntity order;
        if ((order = orderMap.get(orderNo)) == null) {
            throw new EmptyResultsException("no order with number(" + orderNo + "), unable to remove order.");
        }
        model.deleteOrder(order);
    }

    private RestaurantEntity getRestaurant(String[] lines) throws EmptyResultsException {
        if (invalidRestaurantCity(lines)) {
            throw new InputMismatchException("Invalid input, please enter a restaurant name and city.");
        }
        var restaurantName = lines[0];
        var cityName = lines[1];
        return model.getRestaurant(restaurantName, cityName);
    }

    private void promptAndAddDish(Scanner scanner, RestaurantEntity restaurant) {
        // prompt for the name, type, and price of the new dish.
        System.out.print("Enter dish name: ");
        var dishName = scanner.nextLine();

        // Handle Types enum
        var typesRaw = Type.values();
        String[] types = new String[typesRaw.length];
        StringBuilder typesConcat = new StringBuilder();
        for (int i = 0; i < typesRaw.length; i++) {
            types[i] = typesRaw[i].name();
            typesConcat.append(types[i]).append("/");
        }
        System.out.print("Enter dish type(" + typesConcat + "): ");
        var dishTypeStr = scanner.nextLine();
        Type dishType;
        try {
            dishType = Type.valueOf(dishTypeStr);
        } catch (IllegalArgumentException e) {
            throw new InputMismatchException("input of " + dishTypeStr + " is not a valid dish type.");
        }

        System.out.print("Enter dish price: ");
        var dishPriceStr = scanner.nextLine();
        var dishPrice = 0.0f;
        try {
            dishPrice = Float.parseFloat(dishPriceStr);
        } catch (NumberFormatException e) {
            throw new InputMismatchException("input of " + dishPriceStr + " did not convert to a price for a menu item");
        }
        DishEntity dish = new DishEntity();
        dish.setDishName(dishName);
        dish.setType(dishType);
        // Build menu
        MenuItemEntity menu = new MenuItemEntity();
        menu.setPrice(BigDecimal.valueOf(dishPrice));
        menu.setRestaurant(restaurant);
        // Associate dish to menu
        menu.setDish(dish);
        HashSet<MenuItemEntity> menus = new HashSet<>();
        menus.add(menu);
        dish.setMenuItems(menus);
        // Persist both dish and menu
        model.addDish(dish);
    }

    private boolean invalidRestaurantCity(String[] lines) {
        return lines.length != 2 ||
                Objects.equals(lines[0], "") ||
                Objects.equals(lines[1], "");
    }
}


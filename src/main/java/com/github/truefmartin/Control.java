package com.github.truefmartin;

import com.github.truefmartin.exceptions.EmptyResultsException;
import com.github.truefmartin.model.FoodOrderEntity;
import com.github.truefmartin.model.MenuItemEntity;
import com.github.truefmartin.views.DisplayRestaurantDish;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.*;

public class Control {

    private static final Logger logger = LogManager.getLogger(Control.class);

    private static final Menu menuUI = new Menu();
    private static final int[] COLUMN_SIZES = {4,15,20,4,20,6,10};
    private final SessionFactory sessionFactory;

    public Control(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Start the main loop of the program. Prints the menu and gets input from the user.
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
        Session tx = sessionFactory.openSession();
        switch (menuOption.selection) {

            case GET_MENUS:
                getMenus(lines, tx);
                break;

            case ADD_ORDER:
                    getMatchingDishes(lines, tx, scanner);
                break;
                /*
                 Prompt the user for the restaurantName and city .
                 If the restaurant is found, display all orders for that restaurant.
                 Display the restaurantName once (echo the user input) and
                 then display the dishName, price, date, and time for all orders for that restaurant.
                 */
            case GET_ORDERS:
                 break;
                /*
                 Display all food orders (orderNo, dishName, restaurantName, date, time).
                 Prompt the user for the orderNo of the order that they wish to cancel.
                 Remove that order from the FoodOrder table.
                 */
            case DELETE_ORDER: {
                int row;
                try {
                    row = scanner.nextInt();
                } catch (InputMismatchException e) {
                    throw new InputMismatchException("invalid input on read row command");
                }

                break;
            }
            /*
            Prompt the user for the restaurantName and city.
            If the restaurant is found, prompt for the name, type, and price of the the new dish.
            Assume that the dish is unique.
            Insert it into the Dish table. Insert it into the MenuItem table.
             */
            case ADD_DISH:
                break;

            case ERROR:
                break;
        }
    }

    /*
    Prompt the user for a restaurant name and city.
    Find and list all menu items available from that restaurant location.
    Output the restaurant name once (echo the user input) and then list the dish name and price for each
    available menu item.
     */
    private static void getMenus(String[] lines, Session tx) throws EmptyResultsException {
        if (lines.length != 2 ||
                Objects.equals(lines[0], "") ||
                Objects.equals(lines[1], "")) {
            throw new InputMismatchException("Invalid input, please enter a restaurant name and city.");
        }
        var restaurantName = lines[0];
        var cityName = lines[1];
        List<MenuItemEntity> menus = tx.createQuery(
          "from MenuItemEntity m join RestaurantEntity r " +
                  "on m.restaurantNo = r.restaurantId " +
                  "where r.restaurantName = :rName " +
                  "and r.city = :rCity",
                MenuItemEntity.class
        )
                .setParameter("rName", restaurantName)
                .setParameter("rCity", cityName)
                .getResultList();
        tx.close();
        if (menus.isEmpty()) {
            throw EmptyResultsException.fromInput(restaurantName, cityName);
        }
        System.out.println("Restaurant: " + restaurantName + ", City: " + cityName);
        for (MenuItemEntity menu :
                menus
        ) {
            System.out.println("-".repeat(20));
            System.out.println(menu);
        }
        System.out.println("-".repeat(20));
    }

    /*
    Prompt the user for the dishName of the item that they want to order.
    If the dish is found, display the itemNo, restaurantName, city and price for all matches.
    Prompt the user for the itemNo for the MenuItem that they want to order.
    Add the itemNo, current time, and current date to the FoodOrder table.
     */
    private static void getMatchingDishes(String[] lines, Session tx, Scanner scanner) throws EmptyResultsException {
        if (lines.length != 1 || Objects.equals(lines[0], "")) {
            throw new InputMismatchException("Invalid input, please enter a dish name.");
        }
        var dishName = lines[0];
        List<DisplayRestaurantDish> displayRestaurantDishes = tx.createQuery(
                "select new com.github.truefmartin.views.DisplayRestaurantDish(d, r, m)" +
                        "from DishEntity d " +
                        "join MenuItemEntity m on d.dishNo = m.dishNo " +
                        "join RestaurantEntity r on m.restaurantNo = r.restaurantId " +
                        "where d.dishName = :dishName ",
                        DisplayRestaurantDish.class
                )
                .setParameter("dishName", dishName)
                .getResultList();
        if (displayRestaurantDishes.isEmpty()) {
            throw EmptyResultsException.fromInput(dishName);
        }
        System.out.println("Dish: " + dishName );

        for (DisplayRestaurantDish display :
                displayRestaurantDishes
        ) {
            System.out.println("-".repeat(20));
            System.out.println(display);
        }
        System.out.println("-".repeat(20));

        // Get itemNo to add a new order to food_order relation
        System.out.println("Enter itemNo to order: ");
        var itemNoStr = scanner.nextLine();
        int itemNo;
        try {
            itemNo = Integer.parseInt(itemNoStr);
        } catch (NumberFormatException e) {
            throw new InputMismatchException("input of " + itemNoStr + " was not able to be translated to an itemNo");
        }
        FoodOrderEntity newOrder = new FoodOrderEntity();
        newOrder.setItemNo(itemNo);
        newOrder.setDateTimeNow();
        tx.beginTransaction();
        tx.persist(newOrder);
        tx.getTransaction().commit();
        tx.flush();
        System.out.println("Stored a new order: ");
        System.out.println(newOrder);
        tx.close();
    }


    // Uses scan to fill a record. Can pass in loaded scanner with space seperated column numbers to auto-select
    // columns to write.
//    private boolean scanInRecord(Scanner scanner, TitanicRecord record, boolean includeId) throws InputMismatchException {
//        boolean hasBeenUpdated = false;
//        // If ID should be included, start on column 0
//        for (int i = includeId? 0: 1; i < record.getNumAttributes(); i++) {
//            System.out.printf("\n\t%s:\t%s -->", record.getAttributeName(i), record.getValue(i));
//            String in = scanner.nextLine();
//            // If the user didn't just press 'enter' and update attribute value succeeds
//            if (!Objects.equals(in, "") && record.setAttributeValue(i, in)) {
//                hasBeenUpdated = true;
//            }
//        }
//        return hasBeenUpdated;
//    }
}

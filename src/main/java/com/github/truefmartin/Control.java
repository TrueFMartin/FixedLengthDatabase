package com.github.truefmartin;

import com.github.truefmartin.exceptions.EmptyResultsException;
import com.github.truefmartin.model.*;
import com.github.truefmartin.views.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
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
                addOrder(lines, tx, scanner);
                break;

            case GET_ORDERS:
                getOrdersByRestaurant(lines, tx);
                 break;
            case DELETE_ORDER:
                deleteOrder(tx, scanner);
                break;
            case ADD_DISH:
                addDish(lines, tx, scanner);
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
//        List<DisplayDishMenu> dishMenus = tx.createQuery(
//                "select new com.github.truefmartin.views.DisplayDishMenu(d, m) " +
//                "from MenuItemEntity m join RestaurantEntity r on m.restaurantNo = r.restaurantId " +
//                        "join DishEntity d on m.dishNo = d.dishNo " +
//                  "where r.restaurantName = :rName " +
//                  "and r.city = :rCity",
//                        DisplayDishMenu.class
//        )
//                .setParameter("rName", restaurantName)
//                .setParameter("rCity", cityName)
//                .getResultList();
//
        List<MenuItemEntity> menus = tx.createQuery(
                "select elements(r.menuItems) " +
                        "from RestaurantEntity r " +
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
        for (MenuItemEntity dishMenu :
                menus
        ) {
            System.out.println("-".repeat(20));
            if (dishMenu.getDish() != null) {
                System.out.println(new DisplayDishMenu(dishMenu.getDish(), dishMenu));
            } else {
                System.out.printf("**Menu item_no=%d, with price %.2f has no associated dish**\n",
                        dishMenu.getItemNo(), dishMenu.getPrice());
            }
        }
        System.out.println("-".repeat(20));
    }

    /*
    Prompt the user for the dishName of the item that they want to order.
    If the dish is found, display the itemNo, restaurantName, city and price for all matches.
    Prompt the user for the itemNo for the MenuItem that they want to order.
    Add the itemNo, current time, and current date to the FoodOrder table.
     */
    private static void addOrder(String[] lines, Session tx, Scanner scanner) throws EmptyResultsException {
        if (lines.length != 1 || Objects.equals(lines[0], "")) {
            throw new InputMismatchException("Invalid input, please enter a dish name.");
        }
        var dishName = lines[0];
        List<MenuItemEntity> menus = tx.createQuery(
                "select d.menuItems " +
                        "from DishEntity d " +
                        "where d.dishName = :dishName ",
                        MenuItemEntity.class
                )
                .setParameter("dishName", dishName)
                .getResultList();
        if (menus.isEmpty()) {
            throw EmptyResultsException.fromInput(dishName, " or no 'menu_items' with that dishNo");
        }
        System.out.println("Dish: " + dishName );

        for (MenuItemEntity menu :
                menus
        ) {
            System.out.println("-".repeat(20));
            System.out.println(new DisplayRestaurantMenu(menu.getRestaurant(), menu));
        }
        System.out.println("-".repeat(20));

        // Get itemNo to add a new order to food_order relation
        System.out.print("Enter itemNo to add to orders: ");
        var itemNoStr = scanner.nextLine();
        int itemNo;
        try {
            itemNo = Integer.parseInt(itemNoStr);
        } catch (NumberFormatException e) {
            throw new InputMismatchException("input of " + itemNoStr + " was not able to be translated to an itemNo");
        }

        var possibleMenu= menus.stream().filter((var m) -> m.getItemNo() == itemNo).findFirst();
        if (possibleMenu.isEmpty()) {
            throw new EmptyResultsException("input of " + itemNo + " did not match a menuItem");
        }
        var menu = possibleMenu.get();
        tx.beginTransaction();
        FoodOrderEntity newOrder = new FoodOrderEntity();
        newOrder.setMenu(menu);
        newOrder.setDateTimeNow();
        menu.getFoodOrders().add(newOrder);
        tx.merge(menu);
        tx.getTransaction().commit();
        System.out.println("Stored a new order of : ");
        System.out.printf("{menu item_no=%d, order date=%s, order time=%s}\n",
                menu.getItemNo(), newOrder.getDate().toString(), newOrder.getTime().toString());
        tx.close();
    }

    /*
    Prompt the user for the restaurantName and city .
    If the restaurant is found, display all orders for that restaurant.
    Display the restaurantName once (echo the user input) and
    then display the dishName, price, date, and time for all orders for that restaurant.
    */
    private static void getOrdersByRestaurant(String[] lines, Session tx) throws EmptyResultsException {
        if (lines.length != 2 ||
                Objects.equals(lines[0], "") ||
                Objects.equals(lines[1], "")) {
            throw new InputMismatchException("Invalid input, please enter a restaurant name and city.");
        }
        var restaurantName = lines[0];
        var cityName = lines[1];
        List<MenuItemEntity> menuItems = tx.createQuery(
                        "select r.menuItems " +
                                "from RestaurantEntity r " +
                                "where r.restaurantName = :rName " +
                                "and r.city = :rCity",
                        MenuItemEntity.class
                )
                .setParameter("rName", restaurantName)
                .setParameter("rCity", cityName)
                .getResultList();
        tx.close();
        if (menuItems.isEmpty()) {
            throw EmptyResultsException.fromInput(restaurantName, cityName, " with possibly no menus for given restaurant");
        }
        System.out.println("Restaurant: " + restaurantName + ", City: " + cityName);
        for (MenuItemEntity menuItem :
                menuItems
        ) {
            for (FoodOrderEntity foodOrder : menuItem.getFoodOrders()) {
                System.out.println("-".repeat(20));
                System.out.println(new DisplayDishMenuOrder(menuItem.getDish(), menuItem, foodOrder));
            }
        }
        System.out.println("-".repeat(20));
    }

    /*
     Display all food orders (orderNo, dishName, restaurantName, date, time).
     Prompt the user for the orderNo of the order that they wish to cancel.
     Remove that order from the FoodOrder table.
     */
    private static void deleteOrder(Session tx, Scanner scanner) throws EmptyResultsException {
        List<DisplayRestaurantDishOrder> restaurantDishOrders = tx.createQuery(
                        "select new com.github.truefmartin.views.DisplayRestaurantDishOrder(o.menu.restaurant, o.menu.dish, o)" +
                                "from FoodOrderEntity o ",
//                                "join MenuItemEntity m on o.itemNo = m.itemNo " +
//                                "join RestaurantEntity r on m.restaurantNo = r.restaurantId ",
//                                "join DishEntity d on m.dishNo = d.dishNo ",
                        DisplayRestaurantDishOrder.class
                )
                .getResultList();
        if (restaurantDishOrders.isEmpty()) {
            throw new EmptyResultsException("found no orders in food_order");
        }
        HashMap<Integer, FoodOrderEntity> orderMap = new HashMap<>();
        for (DisplayRestaurantDishOrder display :
                restaurantDishOrders
        ) {
            System.out.println("-".repeat(20));
            System.out.println(display);
            orderMap.put(display.getOrder().getOrderNo(), display.getOrder());
        }
        System.out.println("-".repeat(20));

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
        tx.beginTransaction();
        tx.remove(order);
        tx.getTransaction().commit();
        System.out.println("removed order with orderNo: " + orderNo);
        tx.close();
    }
    /*
    Prompt the user for the restaurantName and city.
    If the restaurant is found, prompt for the name, type, and price of the new dish.
    Assume that the dish is unique.
    Insert it into the Dish table. Insert it into the MenuItem table.
     */
    private void addDish(String[] lines, Session tx, Scanner scanner) throws EmptyResultsException {
        if (lines.length != 2 ||
                Objects.equals(lines[0], "") ||
                Objects.equals(lines[1], "")) {
            throw new InputMismatchException("Invalid input, please enter a restaurant name and city.");
        }
        var restaurantName = lines[0];
        var cityName = lines[1];
//        BuildDishPrequery dishBuilder = tx.createQuery(
//                "select new com.github.truefmartin.views.BuildDishPrequery(r, d) " +
//                        "from RestaurantEntity r " +
//                        "join DishEntity d on d.dishNo = (" +
//                        "select max(ld.dishNo)" +
//                        "from DishEntity ld " +
//                        ") " +
//                        "where r.restaurantName = :rName " +
//                        "and r.city = :rCity",
//                        BuildDishPrequery.class
//                )
//                .setParameter("rName", restaurantName)
//                .setParameter("rCity", cityName)
//                .getSingleResultOrNull();

        var restaurant = tx.createQuery(
                        "from RestaurantEntity r " +
                                "where r.restaurantName = :rName " +
                                "and r.city = :rCity",
                        RestaurantEntity.class
                )
                .setParameter("rName", restaurantName)
                .setParameter("rCity", cityName).getSingleResultOrNull();
        if (restaurant == null ) {
            throw EmptyResultsException.fromInput(restaurantName, cityName);
        }
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
            throw new InputMismatchException("input of " + dishTypeStr + " is not a valid dish type." );
        }

        System.out.print("Enter dish price: ");
        var dishPriceStr = scanner.nextLine();
        var dishPrice = 0.0f;
        try {
            dishPrice = Float.parseFloat(dishPriceStr);
        } catch (NumberFormatException e) {
            throw new InputMismatchException("input of " + dishPriceStr + " did not convert to a price for a menu item");
        }
        var trans = tx.beginTransaction();

        // Build dish without associating menu
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
        // Both are persisted due to their relationship
        tx.persist(dish);

        tx.getTransaction().commit();
        System.out.println("added: ");
        System.out.println(dish);
        System.out.println(menu);
        tx.close();
    }
}

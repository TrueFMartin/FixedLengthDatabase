package com.github.truefmartin;

import com.github.truefmartin.exceptions.EmptyResultsException;
import com.github.truefmartin.models.DishEntity;
import com.github.truefmartin.models.FoodOrderEntity;
import com.github.truefmartin.models.MenuItemEntity;
import com.github.truefmartin.models.RestaurantEntity;
import com.github.truefmartin.views.DisplayDishMenu;
import com.github.truefmartin.views.DisplayDishMenuOrder;
import com.github.truefmartin.views.DisplayRestaurantDishOrder;
import com.github.truefmartin.views.DisplayRestaurantMenu;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.List;
/**
 * The Model class is responsible for managing the database operations.
 * It uses Hibernate to interact with the database.
 */
public class Model implements AutoCloseable{
    private static SessionFactory sessionFactory;

    /**
     * Builds a new Hibernate SessionFactory.
     * @return a new SessionFactory
     * @throws HibernateException if there is a problem creating the SessionFactory
     */
    private static SessionFactory buildSession() throws HibernateException {
        return new Configuration().configure().buildSessionFactory();
    }

    /**
     * Constructs a new Model object.
     * If the sessionFactory is null, it builds a new one.
     */
    public Model() {
        if (sessionFactory == null) {
            sessionFactory = buildSession();
        }
    }

    /**
     * Closes the sessionFactory when the Model object is closed.
     * @throws Exception if there is a problem closing the sessionFactory
     */
    @Override
    public void close() throws Exception {
        sessionFactory.close();
    }

    /*
     * The following methods are used to interact with the database. Each method opens a new session,
     * performs the database operation, and then closes the session even if an exception is thrown.
     */

    /**
     * Retrieves the menus of a specific restaurant.
     * @param restaurantName the name of the restaurant
     * @param cityName the city where the restaurant is located
     * @return a list of menus
     * @throws EmptyResultsException if no menus are found
     */
    public List<DisplayDishMenu> getMenusOfRestaurant(String restaurantName, String cityName) throws EmptyResultsException {
        try(var tx = sessionFactory.openSession()) {
            return getMenusOfRestaurant(tx, restaurantName, cityName);
        }
    }

    /**
     * Retrieves the menus that contain a specific dish.
     * @param dishName the name of the dish
     * @return a list of menus
     * @throws EmptyResultsException if no menus are found
     */
    public List<DisplayRestaurantMenu> getMenusOfDish(String dishName) throws EmptyResultsException {
        try(var tx = sessionFactory.openSession()) {
            return getMenusOfDish(tx, dishName);
        }
    }

    /**
     * Retrieves the orders of a specific restaurant.
     * @param restaurantName the name of the restaurant
     * @param cityName the city where the restaurant is located
     * @return a list of orders
     * @throws EmptyResultsException if no orders are found
     */
    public List<DisplayDishMenuOrder> getOrdersOfRestaurant(String restaurantName, String cityName) throws EmptyResultsException {
        try(var tx = sessionFactory.openSession()) {
            return getOrdersOfRestaurant(tx, restaurantName, cityName);
        }
    }

    /**
     * Adds a new order to the database.
     * @param menu the menu item to be ordered
     */
    public void addOrder(MenuItemEntity menu) {
        try(var tx = sessionFactory.openSession()) {
            addOrder(tx, menu);
        }
    }

    /**
     * Retrieves all orders from the database.
     * @return a list of all orders
     * @throws EmptyResultsException if no orders are found
     */
    public List<DisplayRestaurantDishOrder> getAllOrders() throws EmptyResultsException {
        try(var tx = sessionFactory.openSession()) {
            return getAllOrders(tx);
        }
    }

    /**
     * Deletes an order from the database.
     * @param order the order to be deleted
     */
    public void deleteOrder(FoodOrderEntity order) {
        try(var tx = sessionFactory.openSession()) {
            deleteOrder(tx, order);
        }
    }

    /**
     * Retrieves a specific restaurant from the database.
     * @param restaurantName the name of the restaurant
     * @param cityName the city where the restaurant is located
     * @return the restaurant entity
     * @throws EmptyResultsException if the restaurant is not found
     */
    public RestaurantEntity getRestaurant(String restaurantName, String cityName) throws EmptyResultsException {
        try(var tx = sessionFactory.openSession()) {
            return getRestaurant(tx, restaurantName, cityName);
        }
    }

    /**
     * Adds a new dish to the database.
     * @param dish the dish to be added
     */
    public void addDish(DishEntity dish) {
        try(var tx = sessionFactory.openSession()) {
            addDish(tx, dish);
        }
    }

    /*
    * The following methods are helper methods that perform the actual database operations.
    */

    private List<DisplayRestaurantMenu> getMenusOfDish(Session tx, String dishName) throws EmptyResultsException {
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
        var result = new ArrayList<DisplayRestaurantMenu>();
        for (MenuItemEntity menu :
                menus
        ) {
            result.add(new DisplayRestaurantMenu(menu.getRestaurant(), menu));
        }
        return result;
    }

    private static List<DisplayDishMenu> getMenusOfRestaurant(Session tx, String restaurantName, String cityName) throws EmptyResultsException {

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

        if (menus.isEmpty()) {
            throw EmptyResultsException.fromInput(restaurantName, cityName);
        }
        List<DisplayDishMenu> result = new ArrayList<>();
        for (MenuItemEntity dishMenu :
                menus
        ) {
            if (dishMenu.getDish() != null) {
                result.add(new DisplayDishMenu(dishMenu.getDish(), dishMenu));
            } else {
                result.add(new DisplayDishMenu(String.format("**Menu item_no=%d, with price %.2f has no associated dish**\n",
                        dishMenu.getItemNo(), dishMenu.getPrice())));
            }
        }
        return result;
    }


    // Add the itemNo, current time, and current date to the FoodOrder table.
    private void addOrder(Session tx, MenuItemEntity menu) {
        tx.beginTransaction();
        // Get menu from cache to avoid LazyInitializationException
        menu = tx.get(MenuItemEntity.class, menu.getItemNo());
        FoodOrderEntity newOrder = new FoodOrderEntity();
        newOrder.setMenu(menu);
        newOrder.setDateTimeNow();
        tx.persist(newOrder);
        menu.getFoodOrders().add(newOrder);
        tx.getTransaction().commit();
    }

    private List<DisplayDishMenuOrder> getOrdersOfRestaurant(Session tx, String restaurantName, String cityName) throws EmptyResultsException {
        // The number of queries gets out of hand if we let the EAGER associations do their own thing,
        // so we will instead do a single query with raw sql instead. Trading readability and persistence for less DB strain.
        List<Object[]> results = tx.createNativeQuery(
                        "SELECT dish_name, price, date, time " +
                                "FROM food_order o " +
                                "JOIN menu_item mi on o.item_no = mi.item_no " +
                                "JOIN dish d on mi.dish_no = d.dish_no " +
                                "WHERE mi.restaurant_no in " +
                                "( " +
                                "SELECT restaurant.restaurant_id " +
                                "FROM fcmartin.restaurant " +
                                "WHERE restaurant_name = :rName " +
                                "AND city = :rCity" +
                                ")",
                        Object[].class
                )
                .setParameter("rName", restaurantName)
                .setParameter("rCity", cityName)
                .list();

        if (results.isEmpty()) {
            throw EmptyResultsException.fromInput(restaurantName, cityName, " with possibly no menus for given restaurant");
        }
        List<DisplayDishMenuOrder> displayOrders = new ArrayList<>();
        for (Object[] result :
                results
        ) {
            displayOrders.add(new DisplayDishMenuOrder(result));
        }
        return displayOrders;
    }

    private List<DisplayRestaurantDishOrder> getAllOrders(Session tx) throws EmptyResultsException {
        List<DisplayRestaurantDishOrder> restaurantDishOrders = tx.createQuery(
                        "select new com.github.truefmartin.views.DisplayRestaurantDishOrder(o.menu.restaurant, o.menu.dish, o)" +
                                "from FoodOrderEntity o ",
                        DisplayRestaurantDishOrder.class
                )
                .getResultList();
        if (restaurantDishOrders.isEmpty()) {
            throw new EmptyResultsException("found no orders in food_order");
        }
        return restaurantDishOrders;
    }

    private void deleteOrder(Session tx, FoodOrderEntity order) {
        tx.beginTransaction();
        tx.remove(order);
        tx.getTransaction().commit();
    }

    private RestaurantEntity getRestaurant(Session tx, String restaurantName, String cityName) throws EmptyResultsException {
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
        return restaurant;
    }

    private void addDish(Session tx, DishEntity dish) {
        tx.beginTransaction();
        tx.persist(dish);
        tx.getTransaction().commit();
    }

}

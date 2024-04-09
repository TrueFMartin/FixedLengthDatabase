package com.github.truefmartin;

import java.awt.*;
// Import log4j classes.
import com.github.truefmartin.model.DishEntity;
import com.github.truefmartin.model.RestaurantEntity;
import com.github.truefmartin.model.Type;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {

        try (SessionFactory session = buildSession()) {
            artIntro();
            System.out.println("\n\nWelcome, please choose an option from the following menu.");
            new Control(session).start();
            System.out.println("Thank you, goodbye.");
        } catch (HibernateException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static SessionFactory buildSession() throws HibernateException {
        return new Configuration().configure().buildSessionFactory();
    }
    private static void artIntro() {
        ArtIntro artIntro = new ArtIntro();
        ArtIntro.Settings settings = new ArtIntro.Settings(new Font(Font.SANS_SERIF, Font.PLAIN, 17), 140, 16);
        artIntro.drawString("F O O D", "*", settings);
    }
}

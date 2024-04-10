package com.github.truefmartin;

import java.awt.*;
// Import log4j classes.
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        // Create the model which creates a new SessionFactory object.
        // Autoclose the model (and the SessionFactory) when done or on exception.
        try (Model model = new Model()) {
            artIntro();
            System.out.println("\n\nWelcome, please choose an option from the following menu.");
            new Control(model).start();
            System.out.println("Thank you, goodbye.");
        } catch (HibernateException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    private static void artIntro() {
        ArtIntro artIntro = new ArtIntro();
        ArtIntro.Settings settings = new ArtIntro.Settings(new Font(Font.SANS_SERIF, Font.PLAIN, 17), 140, 16);
        artIntro.drawString("F O O D", "*", settings);
    }
}

package com.github.truefmartin;

import com.github.truefmartin.records.TitanicRecord;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
// Import log4j classes.
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
//-----------------------------------------------------
// Example code to read from fixed length records (random access file)
//-----------------------------------------------------

public class Main {
    static TitanicRecord record;
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        artIntro();
        System.out.println("\n\nWelcome, please choose an option from the following menu.");
        new Control().start();
        System.out.println("Thank you, goodbye.");
    }

    private static void artIntro() {
        ArtIntro artIntro = new ArtIntro();
        ArtIntro.Settings settings = new ArtIntro.Settings(new Font(Font.SANS_SERIF, Font.PLAIN, 17), 140, 16);
        artIntro.drawString("D A T A B A S E", "*", settings);
    }
}

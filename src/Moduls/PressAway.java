/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Moduls;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class PressAway implements Runnable {

    private static int INTERVAL_IN_MINUTES;

    public PressAway(int interval_in_minutes) {
        INTERVAL_IN_MINUTES = interval_in_minutes;
    }

    @Override
    public void run() {
        while (true) {
            press_away_errormessages(10);
            wait_(60000 * INTERVAL_IN_MINUTES);
        }
    }

    private static void press_away_errormessages(int nr_attempts) {
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(PressAway.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (int i = 0; i < nr_attempts; i++) {
            robot.keyPress(KeyEvent.VK_ESCAPE);
            wait_(50);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
            wait_(50);
        }
    }

    private static void wait_(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(PressAway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//    public static void main(String[] args) {
//        new Thread(new PressAway(1)).start();
//    }
}

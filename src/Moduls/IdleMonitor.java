package Moduls;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Administrator
 */
public class IdleMonitor implements Runnable {

    Properties p;
    boolean monitor;
    long inactive_time;
    private final static String LOGFILE = "idlemonitor_log.txt";

    public IdleMonitor(Properties props) {
        p = props;
        inactive_time = Long.parseLong(p.getProperty("idle_time"));
        monitor = Long.parseLong(p.getProperty("idle_time")) != -1;
        SimpleLogger.logg(LOGFILE, "IdleMonitor initialized,monitor on = " + monitor + "; idle_time = " + inactive_time);
//        System.out.println("" + monitor);

    }

    @Override
    public void run() {
        while (monitor) {
            try {
                idle_time_user();
            } catch (InterruptedException ex) {
                SimpleLogger.logg(LOGFILE, "error in run(): " + ex);
                Logger.getLogger(IdleMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * A very useful method to be able to evaluate if the user is idle
     *
     * @throws InterruptedException
     */
    public void idle_time_user() throws InterruptedException {
        try {
            boolean disconnected = false;
            long idleTime = 0;
            long start = System.currentTimeMillis();
            Point currLocation = MouseInfo.getPointerInfo().getLocation();
            while (true) {
                wait_(1000);
                Point newLocation = MouseInfo.getPointerInfo().getLocation();
                if (newLocation.x == currLocation.x && newLocation.y == currLocation.y) {
                    //not moved
                    idleTime = System.currentTimeMillis() - start;
                    if (idleTime > minutes_to_milliseconds_converter(inactive_time) && disconnected == false) {
//                        System.out.println("Disconnecting session");
//                        disconnect_current_session();
                        SimpleLogger.logg(LOGFILE, "inactive time =" + inactive_time + " reached");
                        disconnected = true;
                    }
                } else {
                    disconnected = false;
//                    System.out.printf("Idle time was: %s ms", idleTime);
                    idleTime = 0;
                    start = System.currentTimeMillis();
                    break;
                }
                currLocation = newLocation;
            }
        } catch (Throwable ex) {
            SimpleLogger.logg(LOGFILE, "error in idle_time_user(): " + ex);
        }
    }

    private void wait_(int millis) {
        synchronized (this) {
            try {
                wait(millis);
            } catch (InterruptedException ex) {
                Logger.getLogger(IdleMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     *
     * @param minutes
     * @return
     */
    public static long minutes_to_milliseconds_converter(long minutes) {
        return minutes * 60000;
    }

    /**
     * Disconnects the current logged on session
     *
     * @param args
     */
    public void disconnect_current_session() {
        String[] commands2 = {"c:/windows/system32/tsdiscon.exe"};// 
        try {
            Process pr = Runtime.getRuntime().exec(commands2);
            SimpleLogger.logg(LOGFILE, "Session disconnected");
        } catch (IOException ex) {
            SimpleLogger.logg(LOGFILE, "error in disconnect_current_session: " + ex);
        }
    }
}

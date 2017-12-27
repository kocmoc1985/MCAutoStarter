/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcautostarter;

import supplementary.HelpM;
import Moduls.FileCleaner;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import Moduls.SimpleLogger;

/**
 *
 * @author Administrator
 */
public class autostarterCamso implements Runnable {

    private TrayIcon trayIcon;
    private SystemTray tray;
    private Image image;
    private PopupMenu popup;
    private options op;
    private MenuItem optionsx;
    private Properties p;
    private String LINE = "";
    private final String PROPERTIES_PATH = "other/autostarter.properties";
    private String LOGFILE = "autostarter.log";
    //========
    private static String ARGUMENT_1 = "";

    /**
     * AUTO START (NORMAL START WITHOUT ARGS)
     */
    public autostarterCamso(String argument) {
        ARGUMENT_1 = argument;
        SimpleLogger.logg(LOGFILE, "Started, argument = " + ARGUMENT_1);
//        HelpM.terminate_process_no_external_apps_in_use("cmd.exe");
        toTray();
        op = new options();
        options.jLabel3.setText("no args");
        start_moduls();
    }

    private void start_moduls() {
//        new Thread(this).start();
        p = HelpM.properties_load_properties(PROPERTIES_PATH);
        new Thread(new FileCleaner(p, 1)).start();
        new Thread(new FileCleaner(p, 2)).start();
//        new Thread(new ClientLogger("3389")).start();
    }

   

    /**
     * This method contains all properties used in this class! So the properties
     * file contains properties from other modules to!
     */
   

    /**
     *
     */
    private void toTray() {
        if (SystemTray.isSupported()) {

            tray = SystemTray.getSystemTray();
            image = Toolkit.getDefaultToolkit().getImage("other/icon.png");

            ActionListener actionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                     if (e.getSource() == optionsx) {
                        System.out.println("open options");
                        op.makeVisible();
                    }
                }
            };

            popup = new PopupMenu();
            optionsx = new MenuItem("Options");
            optionsx.addActionListener(actionListener);
            popup.add(optionsx);
            //
            trayIcon = new TrayIcon(image, "MCServerTools", popup);
            //
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);
            //
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }

        } else {
            //  System Tray is not supported
        }
    }

    /**
     * Automaticly defines which line to check so fits for both line 10 & 20
     *
     * @throws AWTException
     */
    /**
     *
     * @param millis
     */
    private void threadWait(int minutes) {
        synchronized (this) {
            try {
                wait((10000 * 6) * minutes);
            } catch (InterruptedException ex) {
                Logger.getLogger(autostarterFederals.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private synchronized void wait_(int millis) {
        try {
            wait(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(autostarterCamso.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setLine(String line) {
        this.LINE = line;
        this.LOGFILE = "autostarterCompounds_" + line + ".log";
        SimpleLogger.logg(LOGFILE, "Line is set to " + LINE);
    }

    /**
     *
     * @param mccontrol
     * @param planupdate
     */
    private void display_tray_text(int mccontrol, int planupdate, int paramgen, int recalc_10, int recalc_20) {
        trayIcon.setToolTip("control= " + mccontrol + "\nplanupdt= " + planupdate + "\nparamgen= " + paramgen
                + "\nrecalc_10= " + recalc_10 + "\nrecalc_20= " + recalc_20);
    }

    /**
     *
     * @param path
     */
    private void run_application(String path) {
        try {
            Process pr = Runtime.getRuntime().exec(path);
        } catch (IOException ex) {
//            trayIcon.displayMessage("Note", "Cannot launch, check app name! ", TrayIcon.MessageType.INFO);
//            System.out.println("" + ex);
            SimpleLogger.logg(LOGFILE, "Failed to launch -> " + path);
        }
    }

    public static void main(String[] args) {
        HelpM.err_output_to_file();
        //========================

        int x = 0;
        String argument = "";

        try {
            argument = args[0];
            x = 1;
        } catch (ArrayIndexOutOfBoundsException ex) {
            x = 2;
        }
        new autostarterCamso(argument);
    }

    @Override
    public void run() {
    }
}

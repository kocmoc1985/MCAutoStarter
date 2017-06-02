/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcautostarter;

import Interfaces.Tray;
import Moduls.Backuper;
import Moduls.OtherInstanceRunning;
import Moduls.ShutDown;
import Moduls.SimpleLogger;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import supplementary.HelpM;

/**
 *
 * @author Administrator
 */
public class autoStarterBlueServer implements Tray {

    private TrayIcon trayIcon;
    private SystemTray tray;
    private Image image;
    private PopupMenu popup;
    private MenuItem exit;
    private MenuItem set_shutdown_time;
    private MenuItem do_backup_now;
    private MenuItem do_home_backup_now;
    private MenuItem do_full_backup;
    //==============
    private final static String LOGFILE = "autostarter_blue_server.log";
    private Properties p;
    private final static String PROPERTIES_PATH = "other/autostarterpblueserver.properties";
    private final static String PROPERTIES_PATH_HOME_BACKUP = "other/home_backup.properties";
    //==============
    private int interval_between_check_in_ms = 10000;
    //=============
    private final String PATH_TO_QUERY_APP = "c:/windows/system32/query.exe";
    //=============
    public static String ERR_OUTPUT_FILE_NAME = "";
    public static String ERR_OUTPUT_PATH = "";
    //=============
    private String ARGUMENT_1 = "";
    //=============
    private Backuper BACKUPER;
    private Backuper HOME_BACKUPER;
    //=============
    private ShutDown SHUT_DOWN;

    public autoStarterBlueServer(String argument) {
        this.ARGUMENT_1 = argument;
        loadProperties();
        propertiesHandler();
        check_if_to_run_2();
//        try {
//            check_if_to_run_1();
//        } catch (IOException ex) {
//            Logger.getLogger(autoStarterBlueServer.class.getName()).log(Level.SEVERE, null, ex);
//        }
        toTray();
        go();
    }

    private void go() {
//        new Thread(new ServiceRunning("browser", interval_between_check_in_ms)).start();
//        new Thread(new ServiceRunning("lanmanserver", interval_between_check_in_ms)).start();
        BACKUPER = new Backuper(PROPERTIES_PATH, this, "", "");
        new Thread(BACKUPER).start();
    }

    /**
     * Home backup is backup to an usb drive
     */
    public void do_home_backup() {
        HOME_BACKUPER = new Backuper(PROPERTIES_PATH_HOME_BACKUP, this, "", "");
        HOME_BACKUPER.do_backup_now(Backuper.RESTART);
    }

    /**
     * Full backup is backuping of "blue server" & doing "home backup"
     */
    public void do_full_backup() {
        BACKUPER.do_backup_now(Backuper.NOTHING);
        do_home_backup();
    }

    private void check_if_to_run_2() {
        if (ARGUMENT_1.toLowerCase().equals("force_run")) {
            return; // dont check sessions, start the app
        }
        new Thread(new OtherInstanceRunning(5555, autoStarterBlueServer.class.getName())).start();
    }

    private void toTray() {
        if (SystemTray.isSupported()) {

            tray = SystemTray.getSystemTray();
            image = Toolkit.getDefaultToolkit().getImage("other/icon.png");


            ActionListener actionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    if (e.getSource() == exit) {
                        verifyExit();
                    } else if (e.getSource() == set_shutdown_time) {
                        if (SHUT_DOWN == null) {
                            SHUT_DOWN = new ShutDown(image);
                        } else {
                            SHUT_DOWN.setVisible(true);
                        }
                    } else if (e.getSource() == do_backup_now) {
                        BACKUPER.do_backup_now(Backuper.RESTART);
                    } else if (e.getSource() == do_home_backup_now) {
                        do_home_backup();
                    } else if (e.getSource() == do_full_backup) {
                        do_full_backup();
                    }
                }
            };

            popup = new PopupMenu();

            exit = new MenuItem("Exit");
            set_shutdown_time = new MenuItem("Set shutdown time");
            do_backup_now = new MenuItem("Backup now");
            do_home_backup_now = new MenuItem("Home Backup now");
            do_full_backup = new MenuItem("Full Backup");

            exit.addActionListener(actionListener);
            set_shutdown_time.addActionListener(actionListener);
            do_backup_now.addActionListener(actionListener);
            do_home_backup_now.addActionListener(actionListener);
            do_full_backup.addActionListener(actionListener);

            popup.add(exit);
            popup.add(set_shutdown_time);
            popup.add(do_backup_now);
            popup.add(do_home_backup_now);
            popup.add(do_full_backup);

            trayIcon = new TrayIcon(image, "MC ServiceMonitor & Backup", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);


            try {
                tray.add(trayIcon);

            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }

        } else {
            //  System Tray is not supported
        }
    }

    private void loadProperties() {
        p = new Properties();
        try {
            p.load(new FileInputStream(PROPERTIES_PATH));
        } catch (IOException ex) {
            SimpleLogger.logg(LOGFILE, "couldn't find properties file = " + PROPERTIES_PATH);
            Logger.getLogger(autostarterPhoenix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void propertiesHandler() {
        interval_between_check_in_ms = Integer.parseInt(p.getProperty("interval_between_check_in_ms", "60000"));
    }

    private void verifyExit() {
        try {
            int x = Integer.parseInt(JOptionPane.showInputDialog(null, "Type password", "Exit", JOptionPane.WARNING_MESSAGE));
            if (x == 4765) {
                System.exit(0);
            }
        } catch (NumberFormatException ex) {
            //
        }
    }

    public static void main(String[] args) {
        //Write error stream to a file
        HelpM.create_dir_if_missing("err_output");
        try {
            ERR_OUTPUT_FILE_NAME = "err_" + HelpM.get_proper_date_time_same_format_on_all_computers() + ".txt";
            ERR_OUTPUT_PATH = "err_output/" + ERR_OUTPUT_FILE_NAME;

            PrintStream out = new PrintStream(new FileOutputStream(ERR_OUTPUT_PATH));
            System.setErr(out);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(autoStarterBackupClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        String arg_1 = "";
        try {
            arg_1 = args[0];
        } catch (Exception ex) {
//            SimpleLogger.logg(LOGFILE, "failure in main() with 'args, '" + ex);
        }
        SimpleLogger.logg(LOGFILE, "Argument = " + arg_1);
        new autoStarterBlueServer(arg_1);
    }

    @Override
    public void displayTrayWarningMessage(String title, String msg) {
        trayIcon.displayMessage(title, msg, TrayIcon.MessageType.WARNING);
    }

    @Override
    public void displayTrayErrorMessage(String title, String msg) {
        trayIcon.displayMessage(title, msg, TrayIcon.MessageType.ERROR);
    }

    @Override
    public void displayTrayInfoMessage(String title, String msg) {
        trayIcon.displayMessage(title, msg, TrayIcon.MessageType.INFO);
    }
}

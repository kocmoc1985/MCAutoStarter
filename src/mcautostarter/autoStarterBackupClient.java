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
public class autoStarterBackupClient implements Tray {

    private TrayIcon trayIcon;
    private SystemTray tray;
    private Image image;
    private PopupMenu popup;
    private MenuItem exit;
    private MenuItem set_shutdown_time;
    private MenuItem do_backup_now;
    private MenuItem do_backup_now_and_shutdown;
    private MenuItem do_backup_now_and_hibernate;
    private MenuItem set_options;
    //==============
    private final static String LOGFILE = "mc_auto_starter_modul.log";
    private Properties p;
    private final static String PROPERTIES_PATH = "other/autostarter_backup_client.properties";
    //==============
    private boolean TRAY_ENABLED = true;
    //==============
    public static String ERR_OUTPUT_FILE_NAME = "";
    public static String ERR_OUTPUT_PATH = "";
    //==============
    private String ARGUMENT_1 = "";
    //==============
    private Backuper BACKUPER;
    //==============
    private ShutDown SHUT_DOWN;

    public autoStarterBackupClient(String argument) {
        this.ARGUMENT_1 = argument;
        loadProperties();
        propertiesHandler();
        check_if_to_run();
        toTray();
        go();
    }

    private void go() {
        BACKUPER = new Backuper(PROPERTIES_PATH, this, ERR_OUTPUT_PATH, ERR_OUTPUT_FILE_NAME);
        new Thread(BACKUPER).start();
    }

    private void check_if_to_run() {
        if (ARGUMENT_1.toLowerCase().equals("force_run")) {
            return; // dont check sessions, start the app
        }
        new Thread(new OtherInstanceRunning(5555, autoStarterBackupClient.class.getName())).start();
    }

    private void toTray() {
        if (TRAY_ENABLED == false) {
            return;
        }
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
                    } else if (e.getSource() == do_backup_now_and_shutdown) {
                        boolean x = JOptionPane.showConfirmDialog(null, "Please confirm, should backup with shut down be done?", "Shut down after backup", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                        if (x) {
                            BACKUPER.do_backup_now(Backuper.SHUTDOWN);
                        }
                    } else if (e.getSource() == do_backup_now_and_hibernate) {
                        boolean x = JOptionPane.showConfirmDialog(null, "Please confirm, should backup with hibernate be done?", "Hibernate after backup", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
                        if (x) {
                            BACKUPER.do_backup_now(Backuper.HIBERNATE);
                        }
                    } else if (e.getSource() == set_options) {
                        HelpM.run_java_app("PropertiesReader.jar", "");
                    }
                }
            };

            popup = new PopupMenu();

            exit = new MenuItem("Exit");
            set_shutdown_time = new MenuItem("Set shutdown time");
            do_backup_now = new MenuItem("Backup now");
            do_backup_now_and_shutdown = new MenuItem("Backup now & shutdown");
            do_backup_now_and_hibernate = new MenuItem("Backup now & hibernate");
            set_options = new MenuItem("Options");

            exit.addActionListener(actionListener);
            set_shutdown_time.addActionListener(actionListener);
            do_backup_now.addActionListener(actionListener);
            do_backup_now_and_shutdown.addActionListener(actionListener);
            do_backup_now_and_hibernate.addActionListener(actionListener);
            set_options.addActionListener(actionListener);

            popup.add(exit);
            popup.add(set_shutdown_time);
            popup.add(do_backup_now);
            popup.add(do_backup_now_and_shutdown);
            popup.add(do_backup_now_and_hibernate);
            popup.add(set_options);

            trayIcon = new TrayIcon(image, "MCBackup", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);

            try {
                tray.add(trayIcon);

            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }
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
        TRAY_ENABLED = Boolean.parseBoolean(p.getProperty("tray_enabled", "true"));
    }

    private void verifyExit() {
        try {
            boolean x = JOptionPane.showConfirmDialog(null, "Exit program?", "Verify Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
            if (x) {
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
            //
        }
        SimpleLogger.logg(LOGFILE, "Argument = " + arg_1);
        autoStarterBackupClient backupClient = new autoStarterBackupClient(arg_1);
    }

    @Override
    public void displayTrayWarningMessage(String title, String msg) {
        if (trayIcon == null) {
            return;
        }
        trayIcon.displayMessage(title, msg, TrayIcon.MessageType.WARNING);
    }

    @Override
    public void displayTrayErrorMessage(String title, String msg) {
        if (trayIcon == null) {
            return;
        }
        trayIcon.displayMessage(title, msg, TrayIcon.MessageType.ERROR);
    }

    @Override
    public void displayTrayInfoMessage(String title, String msg) {
        if (trayIcon == null) {
            return;
        }
        trayIcon.displayMessage(title, msg, TrayIcon.MessageType.INFO);
    }
}

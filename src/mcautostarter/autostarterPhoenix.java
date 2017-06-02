/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcautostarter;

import Moduls.ClientLogger;
import java.util.Arrays;
import supplementary.HelpM;
import Moduls.LogScanner;
import Moduls.PressAway;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Administrator
 */
public final class autostarterPhoenix implements Runnable {

    private TrayIcon trayIcon;
    private SystemTray tray;
    private Image image;
    private PopupMenu popup;
    private MenuItem openBrowser;
    private MenuItem openEditor;
    private MenuItem openExport;
    private MenuItem exit;
    int nrSessions = 0;
    private Properties p;
    private final static String PROPERTIES_PATH = "other/autostarterphoenix.properties";
    //===================================================<properties>
    private String mc_control_phoenix_name;
    private String control_check_refresh_rate;
    //=================================================
    private boolean log_scanner_active;
    private boolean mccontrol_monitoring;
    private boolean press_away_error_msgs;
    private boolean client_logger_active;
    //==================================================
    private String log_scanner_path_to_log;
    private int log_scanner_failures_before_action;
    private int log_scanner_interval;
    private ArrayList<String> log_scanner_failure_messages = new ArrayList<String>();
    //==================================================
    private String mc_control_launch_path = "";
    private String mc_browser_launch_path = "";
    private String edit_param_launch_path = "";
    private String export_launch_path = "";
    //===================================================</properties>
    private final static String LOGFILE = "auto_starter_pct.log"; //for logging this class
    //===================================================
    private String ARG_OFF = "off";
    private String ARGUMENT = "";
    //===================================================
    private boolean one_time_1 = false;

    /**
     * START NORMALY WITH SESSION CHECK
     */
    public autostarterPhoenix(String argument) {
        HelpM.terminate_process_no_external_apps_in_use("cmd.exe");
        ARGUMENT = argument;
        SimpleLogger.logg(LOGFILE, "argument = " + ARGUMENT);
        loadProperties();
        propertiesHandler();
        toTray();
        startMainThread();
        startLogScannerModul();
        startPressAway();
        startClientLogger();
    }

    private void startMainThread() {
        if (ARGUMENT.equals(ARG_OFF) || mccontrol_monitoring == false) {
            trayIcon.displayMessage("Note", "control monitoring is off", TrayIcon.MessageType.INFO);
            trayIcon.setToolTip("control monitoring is off " + HelpM.get_proper_date_and_time_default_format());
            SimpleLogger.logg(LOGFILE, "control monitoring was not started, arg = " + ARGUMENT + " ; property = " + mccontrol_monitoring);
            return;
        }

        new Thread(this).start();
        SimpleLogger.logg(LOGFILE, "control monitoring is on");
    }

    private void startLogScannerModul() {
        if (log_scanner_active) {
            new Thread(new LogScanner(
                    log_scanner_path_to_log,
                    log_scanner_failure_messages,
                    log_scanner_failures_before_action,
                    log_scanner_interval,
                    mc_control_phoenix_name)).start();
            SimpleLogger.logg(LOGFILE, "logscanner modul is on");
            SimpleLogger.logg(LOGFILE, "logscanner_path_to_log = " + log_scanner_path_to_log);
            SimpleLogger.logg(LOGFILE, "log_scanner_failure_messages = " + log_scanner_failure_messages);
            SimpleLogger.logg(LOGFILE, "log_scanner_failures_before_action = " + log_scanner_failures_before_action);
            SimpleLogger.logg(LOGFILE, "log_scanner_interval = " + log_scanner_interval);
            SimpleLogger.logg(LOGFILE, "mc_control_phoenix_name = " + mc_control_phoenix_name + "\r");
        } else {
            SimpleLogger.logg(LOGFILE, "logscanner modul is off");
        }
    }

    private void startPressAway() {
        if (press_away_error_msgs) {
            new Thread(new PressAway(10)).start();
            SimpleLogger.logg(LOGFILE, "pressaway modul is on");
        } else {
            SimpleLogger.logg(LOGFILE, "pressaway modul is off");
        }
    }

    private void startClientLogger() {
        if (client_logger_active) {
            new Thread(new ClientLogger("5900")).start();
            SimpleLogger.logg(LOGFILE, "clientlogger modul is on");
        } else {
            SimpleLogger.logg(LOGFILE, "clientlogger modul is off");
        }
    }

    /**
     * This method contains all properties used in this class! So the properties
     * file contains properties from other modules to!
     */
    private void propertiesHandler() {
        mc_control_phoenix_name = p.getProperty("mc_control_phoenix_name");
        //===================
        control_check_refresh_rate = p.getProperty("control_check_refresh_rate");
        //===================
        log_scanner_path_to_log = p.getProperty("log_scanner_path_to_log", "");// For the function which checks the mixcont.log done by jura....
        log_scanner_failures_before_action = Integer.parseInt(p.getProperty("log_scanner_failures_before_action", "50"));
        log_scanner_interval = Integer.parseInt(p.getProperty("log_scanner_interval", "1"));
        log_scanner_failure_messages = build_failure_message_list(p.getProperty("log_scanner_failure_messages", ""));
        //===================
        mc_control_launch_path = p.getProperty("mc_control_launch_path");
        mc_browser_launch_path = p.getProperty("mc_browser_launch_path");
        edit_param_launch_path = p.getProperty("edit_param_launch_path");
        export_launch_path = p.getProperty("export_launch_path");
        //===================
        mccontrol_monitoring = Boolean.parseBoolean(p.getProperty("mccontrol_monitoring", "true"));
        log_scanner_active = Boolean.parseBoolean(p.getProperty("log_scanner_active", "false"));
        press_away_error_msgs = Boolean.parseBoolean(p.getProperty("press_away_error_msgs", "false"));
        client_logger_active = Boolean.parseBoolean(p.getProperty("client_logger_active", "false"));
    }

    private ArrayList<String> build_failure_message_list(String failure_msgs) {
        ArrayList<String> list = new ArrayList<String>();
        if (failure_msgs.contains(";")) {
            String arr[] = failure_msgs.split(";");
            list.addAll(Arrays.asList(arr));
            return list;
        } else {
            list.add(failure_msgs);
            return list;
        }
    }

    private void toTray() {
        if (SystemTray.isSupported()) {

            tray = SystemTray.getSystemTray();
            image = Toolkit.getDefaultToolkit().getImage("other/icon.png");


            ActionListener actionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (e.getSource() == openBrowser) {

                        run_application(mc_browser_launch_path);

                    } else if (e.getSource() == openEditor) {

                        run_application(edit_param_launch_path);

                    } else if (e.getSource() == openExport) {

                        run_application(export_launch_path);

                    } else if (e.getSource() == exit) {
                        verifyExit();
                    }
                }
            };

            popup = new PopupMenu();
            openBrowser = new MenuItem("Browser");
            openEditor = new MenuItem("Editor");
            openExport = new MenuItem("Export");
            exit = new MenuItem("Exit");
            openBrowser.addActionListener(actionListener);
            openEditor.addActionListener(actionListener);
            openExport.addActionListener(actionListener);
            exit.addActionListener(actionListener);
            popup.add(openBrowser);
            popup.add(openEditor);
            popup.add(openExport);
            popup.add(exit);

            trayIcon = new TrayIcon(image, "MCWatch", popup);
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

    /**
     *
     * @param mode
     * @throws AWTException
     */
    private void checker() throws AWTException {
        if (HelpM.processRunning(mc_control_phoenix_name)) {//#
            if (!one_time_1) {
                SimpleLogger.logg(LOGFILE, "[OBS!]startup-check, control is on");
                one_time_1 = true;
            }
            trayIcon.setToolTip("MCControl running " + HelpM.get_proper_date_and_time_default_format());
        } else {
            run_application(mc_control_launch_path);//
            SimpleLogger.logg(LOGFILE, mc_control_phoenix_name + " was turned off!");
            trayIcon.setToolTip("MCControl was not running.." + HelpM.get_proper_date_and_time_default_format());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
//                loadProperties();
                checker();
            } catch (AWTException ex) {
                Logger.getLogger(autostarterPhoenix.class.getName()).log(Level.SEVERE, null, ex);
            }
            wait_();
        }

    }

    public static void main(String[] args) {
        //Write error stream to a file
        HelpM.err_output_to_file();
       
        //==============
        String argument = "";

        try {
            argument = args[0];
        } catch (ArrayIndexOutOfBoundsException ex) {
            argument = "";
        }
        //===
        autostarterPhoenix a = new autostarterPhoenix(argument);
    }

    private void run_application(String path) {
        try {
            Process pr = Runtime.getRuntime().exec(path);
        } catch (IOException ex) {
            trayIcon.displayMessage("Note", "Cannot launch, check app name! ", TrayIcon.MessageType.INFO);
            SimpleLogger.logg(LOGFILE, ex.toString());
            Logger.getLogger(autostarterPhoenix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Note that the properties are now updated during the programm run. The
     * update interval is the same as for MCControl run-check.
     */
    private void loadProperties() {
        p = new Properties();
        try {
            p.load(new FileInputStream(PROPERTIES_PATH));
        } catch (IOException ex) {
            SimpleLogger.logg(LOGFILE, "couldn't find properties file = " + PROPERTIES_PATH);
            Logger.getLogger(autostarterPhoenix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void wait_() {
        try {
            synchronized (this) {
                wait(60000 * Integer.parseInt(control_check_refresh_rate));//#
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(autostarterPhoenix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

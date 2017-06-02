/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcautostarter;

import supplementary.HelpM;
import Moduls.FileCleaner;
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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Administrator
 */
public final class autostarterFederals implements Runnable {

    private TrayIcon trayIcon;
    private Thread tr;
    private SystemTray tray;
    private Image image;
    private PopupMenu popup;
    private String path = "";
    private String mc_control_launch_path = "";
    private String mc_browser_launch_path = "";
    private String edit_param_launch_path = "";
    private String export_launch_path = "";
    private options op;
    private MenuItem openBrowser;
    private MenuItem openEditor;
    private MenuItem openExport;
    private MenuItem optionsx;
    int nrSessions = 0;
    private Properties props;
    int MODE = 3;
    //===================================================<properties>
    private String session_prog;
    private String logonsession_prefix;
    private String remote_desktop_path;
    private String process_name_rdp;
    private String start_monitor_off;
    private String mc_control_federals_name;
    private String driver_restart_on;
    private String wait_after_driver_restart_millis;
    private String control_check_refresh_rate;
    private String driver_to_kill_1;
    private String driver_to_kill_2;
    private String path_to_mccontrol_log;
    private boolean mccontrol_monitoring;
    private boolean press_away_error_msgs;
    private String[] windows_to_press_away;
    //===================================================</properties>
    private final static String LOG_MAIN = "main_log.txt";
    //================================================================
    private boolean FIRST_TIME = true;
    /**
     * START NORMALY WITH SESSION CHECK
     */
    public autostarterFederals() {
        SimpleLogger.logg(LOG_MAIN, "started normaly with session check");
        HelpM.terminate_process_no_external_apps_in_use("cmd.exe");
        MODE = 0;
        loadProperties();
        toTray();
        op = new options();
//        sessionChecker();
        //OBS! Only for passing by a condition
        this.nrSessions = 2;
        //
        new Thread(this).start();
//        new Thread(new FileCleaner(props, 1)).start();
//        new WindowCloser(windows_to_press_away, press_away_error_msgs);
    }

    /**
     * Start with monitor on, regardless the ammount of sessions opened
     *
     * @param vv
     */
    public autostarterFederals(int vv) { // Start with MCControl.exe monitor on, regardless of number of session
        SimpleLogger.logg(LOG_MAIN, "started with monitor forced");
        HelpM.terminate_process_no_external_apps_in_use("cmd.exe");
        MODE = 3;
        loadProperties();
        toTray();
        op = new options();
        new Thread(this).start();
        new Thread(new FileCleaner(props, 1)).start();
//        new WindowCloser(windows_to_press_away, press_away_error_msgs);
    }

    /**
     * Monitoring MODE
     *
     * @param string
     */
    public autostarterFederals(String string) { // No MCControl monitoring
        SimpleLogger.logg(LOG_MAIN, "started with monitor off");
        HelpM.terminate_process_no_external_apps_in_use("cmd.exe");
        MODE = 1;
        loadProperties();
        toTray();
        op = new options();
        tr = new Thread(this);
        tr.start();
    }

    /**
     * Test mode - calculates nr of admin sessions which are active
     *
     * @param string
     * @param str
     */
    public autostarterFederals(String string, String str) { // For testing of how many sessions are currently running
        SimpleLogger.logg(LOG_MAIN, "started in test mode");
        HelpM.terminate_process_no_external_apps_in_use("cmd.exe");
        MODE = 2;
        loadProperties();
        op = new options();
        session_test_Checker();
        JOptionPane.showMessageDialog(null, "Number Sessions = " + nrSessions);
    }

    /**
     * This method contains all properties used in this class! So the properties
     * file contains properties from other modules to!
     */
    private void propertiesHandler() {
        session_prog = props.getProperty("session_prog");
        logonsession_prefix = props.getProperty("logonsession_prefix");
        remote_desktop_path = props.getProperty("remote_desktop_path");
        process_name_rdp = props.getProperty("process_name_rdp");
        start_monitor_off = props.getProperty("start_monitor_off");
        mc_control_federals_name = props.getProperty("mc_control_federals_name");
        driver_restart_on = props.getProperty("driver_restart_on", "false");
        wait_after_driver_restart_millis = props.getProperty("wait_after_driver_restart_millis");
        control_check_refresh_rate = props.getProperty("control_check_refresh_rate");
        driver_to_kill_1 = props.getProperty("driver_to_kill_1");
        driver_to_kill_2 = props.getProperty("driver_to_kill_2");
        path_to_mccontrol_log = props.getProperty("path_to_mccontrol_log", "");// For the function which checks the mixcont.log done by jura....
        mc_control_launch_path = props.getProperty("mc_control_launch_path");
        mc_browser_launch_path = props.getProperty("mc_browser_launch_path");
        edit_param_launch_path = props.getProperty("edit_param_launch_path");
        export_launch_path = props.getProperty("export_launch_path");
        mccontrol_monitoring = Boolean.parseBoolean(props.getProperty("mccontrol_monitoring", "true"));
        press_away_error_msgs = Boolean.parseBoolean(props.getProperty("press_away_error_msgs", "false"));
        windows_to_press_away = HelpM.get_windows_to_press_away_federal_mogul(props);
    }

    /**
     * The automatic session launcher method
     *
     */
    private void sessionChecker() {
        try {
            thread_sleep_n(10000);
            nrSessions = HelpM.count_sessions(session_prog, logonsession_prefix);//##
            if (nrSessions == 1) {
                HelpM.runRDP(remote_desktop_path);//#
                thread_sleep_n(50000);
                HelpM.terminate_process_no_external_apps_in_use(process_name_rdp);//#
//                run_application(start_monitor_off);//#
                SimpleLogger.logg(LOG_MAIN, "automatic start/run of session performed: " + remote_desktop_path);
                System.exit(0);
            } else {
                SimpleLogger.logg(LOG_MAIN, "automatic start/run of session NOT performed, nr sessions = " + nrSessions);
            }
        } catch (IOException ex) {
            Logger.getLogger(autostarterFederals.class.getName()).log(Level.SEVERE, null, ex);
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

                    } else if (e.getSource() == optionsx) {
                        op.makeVisible();
                    }
                }
            };

            popup = new PopupMenu();
            openBrowser = new MenuItem("Browser");
            openEditor = new MenuItem("Editor");
            openExport = new MenuItem("Export");
            optionsx = new MenuItem("Options");
            openBrowser.addActionListener(actionListener);
            openEditor.addActionListener(actionListener);
            openExport.addActionListener(actionListener);
            optionsx.addActionListener(actionListener);
            popup.add(openBrowser);
            popup.add(openEditor);
            popup.add(openExport);
            popup.add(optionsx);

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

    /**
     *
     * @param mode
     * @throws AWTException
     */
    private void checker(String mode) throws AWTException {

        boolean condition = false;

        if (mccontrol_monitoring == false) {
            trayIcon.displayMessage("Note", "MCControl monitoring option is off", TrayIcon.MessageType.INFO);
            trayIcon.setToolTip("MCControl monitoring is of " + HelpM.get_proper_date_and_time_default_format());
        }

        if (mode.equals("normal")) {
            condition = mccontrol_monitoring == true && nrSessions > 1;
        } else if (mode.equals("forcedmonitor")) {
            condition = mccontrol_monitoring == true;
        }


        while (condition) {
            if (HelpM.processRunning(mc_control_federals_name)) {//#

                if (checkIfHangingAndPerformActions()) { // Checks if MCControl is hanging
                    trayIcon.setToolTip("MCControl reseted " + HelpM.get_proper_date_and_time_default_format());
                } else {
                    trayIcon.setToolTip("MCControl running " + HelpM.get_proper_date_and_time_default_format());
                }

            } else {
                //
                if (driver_restart_on.equals("true") && FIRST_TIME == false) {//#
                    killDrivers(2);
                    thread_sleep_n(Integer.parseInt(wait_after_driver_restart_millis));//#
                }
                //
                FIRST_TIME = false;
                //
                run_application(mc_control_launch_path);//
                //
                trayIcon.setToolTip("MCControl was not running.." + HelpM.get_proper_date_and_time_default_format());
            }

            if (press_away_error_msgs) {
                HelpM.press_away_errormessages(20);
            }

            waitForRefresh();
        }
    }

    private boolean checkIfHangingAndPerformActions() {
        if (HelpM.checkIfHanging(path_to_mccontrol_log)) {
            HelpM.terminate_process(mc_control_federals_name);
            SimpleLogger.logg(LOG_MAIN, "mccontrol was terminated, due to process not responded");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Killing the drivers works better with the external app (pskill.exe)
     *
     * @param mode 1 = with java api, mode 2 = close with external app
     */
    private void killDrivers(int mode) {

        if (mode == 1) {
            HelpM.terminate_process_no_external_apps_in_use(driver_to_kill_1);
            HelpM.terminate_process_no_external_apps_in_use(driver_to_kill_2);
            SimpleLogger.logg(LOG_MAIN, "opc drivers terminated with java library");
        } else {
            HelpM.terminate_process(driver_to_kill_1);
            HelpM.terminate_process(driver_to_kill_2);
            SimpleLogger.logg(LOG_MAIN, "opc drivers terminated with non-java library");
        }
    }

    public static void main(String[] args) {
        //Write error stream to a file
        HelpM.err_output_to_file();

        //======================================================================

        int x = 0;
        String argument = "";

        //DEBUGG
//        int x = 1;
//        String argument = "forcemonitor";

        try {
            argument = args[0];
            x = 1;
        } catch (ArrayIndexOutOfBoundsException ex) {
            x = 2;
        }
        if (x == 1) {
            if (argument.equals("off")) {
                autostarterFederals a = new autostarterFederals("No monitoring just app launching");
                //
                options.jLabel3.setText("Args = off");
            } else if (argument.equals("sessions")) {
                autostarterFederals a = new autostarterFederals("Only for", "testing how many sessions are currently running");
                //
                options.jLabel3.setText("Args = sessions");
                System.exit(0);
            } else if (argument.equals("forcemonitor")) {
                autostarterFederals a = new autostarterFederals(0);
                //
                options.jLabel3.setText("Args = forcemonitor");
            } else if (argument.equals("/?")) {
                System.out.println("[off] monitoring of MCControl.exe off");
                System.out.println("[sessions] Just to show number of sessions currently running");
                System.out.println("[forcemonitor] Start with MCControl.exe monitor on regardless number off session");
            }
        } else {
            autostarterFederals a = new autostarterFederals();
        }
    }

    @Override
    public void run() {
        try {
            if (MODE == 0) {
                loadProperties();
                checker("normal");
            } else if (MODE == 3) {
                loadProperties();
                checker("forcedmonitor");
            } else {
                trayIcon.setToolTip("MCControl monitoring is off");
            }
        } catch (AWTException ex) {

            Logger.getLogger(autostarterFederals.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void run_application(String path) {
        try {
            Process pr = Runtime.getRuntime().exec(path);
        } catch (IOException ex) {
            trayIcon.displayMessage("Note", "Cannot launch, check app name! ", TrayIcon.MessageType.INFO);
            Logger.getLogger(autostarterFederals.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Note that the properties are now updated during the programm run. The
     * update interval is the same as for MCControl run-check.
     */
    private void loadProperties() {
        props = new Properties();
        try {
            props.load(new FileInputStream("other/autostarterfed.properties"));
            propertiesHandler();
        } catch (IOException ex) {
            Logger.getLogger(autostarterFederals.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void session_test_Checker() {
        try {
            nrSessions = HelpM.count_sessions(session_prog, logonsession_prefix);
        } catch (IOException ex) {
            Logger.getLogger(autostarterFederals.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void waitForRefresh() {
        try {
            synchronized (this) {
                wait((10000 * 6) * Integer.parseInt(control_check_refresh_rate));//#
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(autostarterFederals.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void thread_sleep_n(int millis) {
        synchronized (this) {
            try {
                wait(millis);
            } catch (InterruptedException ex) {
                Logger.getLogger(autostarterFederals.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import Moduls.ClientLogger;
import Moduls.SimpleLogger;

/**
 *
 * @author Administrator
 */
public class autostarterCompounds implements Runnable {

    private TrayIcon trayIcon;
    private SystemTray tray;
    private Image image;
    private PopupMenu popup;
    private String mccontrolname = "";
    private String browsername = "";
    private String editparname = "";
    private String exportname = "";
    private options op;
    private MenuItem openBrowser;
    private MenuItem openEditor;
    private MenuItem openExport;
    private MenuItem optionsx;
    private int nrSessions = 0;
    private Properties p;
    private String LINE = "";
    private final String LINE_10 = "10";
    private final String LINE_20 = "20";
    private final String PROPERTIES_PATH = "other/autostartercp.properties";
    ///<properties>
    private String session_prog;
    private String logonsession_prefix;
    private String remote_desktop_path;
    private String process_name_rdp;
    private String mc_browser_10;
    private String edit_param_10;
    private String export_10;
    private String mc_control_compounds_name_line20;
    private String mc_control_compounds_name_line10;
    private String mc_control_compounds_run_line10;
    private String planupdate_app_name;
    private String planupdate_app_start;
    private String param_gen_app_name;
    private String param_gen_app_start;
    private String recalc_app_name_line_10;
    private String recalc_app_start_line_10;
    private String recalc_app_name_line_20;
    private String recalc_app_start_line_20;
    private int check_mc_control_interval;
    private boolean mccontrol_monitoring;
    ///</properties>
    private String LOGFILE = "autostarterCompounds.log";
    //========
    private static String ARGUMENT_1 = "";
    private static final String CONTROL_MONITOR_OFF = "monitoroff10";

    /**
     * AUTO START (NORMAL START WITHOUT ARGS)
     */
    public autostarterCompounds(String argument) {
        ARGUMENT_1 = argument;
        SimpleLogger.logg(LOGFILE, "Started, argument = " + ARGUMENT_1);
//        HelpM.terminate_process_no_external_apps_in_use("cmd.exe");
        loadProperties();
        toTray();
        op = new options();
        options.jLabel3.setText("no args");
        sessionChecker();

        start_moduls();

    }

    private void start_moduls() {
        new Thread(this).start();

        if (LINE.equals(LINE_10)) { // other moduls shouldnt run on both lines
            return;
        }
        new Thread(new FileCleaner(p, 1)).start();
        new Thread(new FileCleaner(p, 2)).start();
        new Thread(new ClientLogger("3389")).start();
    }

    /**
     *
     */
    private void loadProperties() {
        p = new Properties();
        try {
            p.load(new FileInputStream(PROPERTIES_PATH));
            propertiesHandler();
        } catch (IOException ex) {
            Logger.getLogger(autostarterCompounds.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method contains all properties used in this class! So the properties
     * file contains properties from other modules to!
     */
    private void propertiesHandler() {
        session_prog = p.getProperty("session_prog");
        logonsession_prefix = p.getProperty("logonsession_prefix");
        remote_desktop_path = p.getProperty("remote_desktop_path");
        process_name_rdp = p.getProperty("process_name_rdp");
        mc_browser_10 = p.getProperty("mc_browser_10");
        edit_param_10 = p.getProperty("edit_param_10");
        export_10 = p.getProperty("export_10");
        mc_control_compounds_name_line20 = p.getProperty("mc_control_compounds_name_line20");
        mc_control_compounds_name_line10 = p.getProperty("mc_control_compounds_name_line10");
        mc_control_compounds_run_line10 = p.getProperty("mc_control_compounds_run_line10");
        planupdate_app_name = p.getProperty("planupdate_app_name");
        planupdate_app_start = p.getProperty("planupdate_app_start");
        param_gen_app_name = p.getProperty("param_gen_app_name");
        param_gen_app_start = p.getProperty("param_gen_app_start");
        recalc_app_name_line_10 = p.getProperty("recalc_app_name_line_10");
        recalc_app_start_line_10 = p.getProperty("recalc_app_start_line_10");
        recalc_app_name_line_20 = p.getProperty("recalc_app_name_line_20");
        recalc_app_start_line_20 = p.getProperty("recalc_app_start_line_20");

        check_mc_control_interval = Integer.parseInt(p.getProperty("check_mc_control_interval", "3"));
        mccontrolname = p.getProperty("mc_control_compounds_run_line20");
        browsername = p.getProperty("mc_browser_20");
        editparname = p.getProperty("edit_param_20");
        exportname = p.getProperty("export_20");
        mccontrol_monitoring = Boolean.parseBoolean(p.getProperty("mccontrol_monitoring", "true"));
        if (ARGUMENT_1.equals(CONTROL_MONITOR_OFF)) {
            mccontrol_monitoring = false;
        }
        SimpleLogger.logg(LOGFILE, "Control monitoring = " + mccontrol_monitoring);
    }

    /**
     *
     */
    private void sessionChecker() {
        nrSessions = 2;
        try {//*****************Changes regarding Session Opener
            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException ex) {
                Logger.getLogger(autostarterCompounds.class.getName()).log(Level.SEVERE, null, ex);
            }
            nrSessions = HelpM.count_sessions(session_prog, logonsession_prefix);

            boolean console_session = HelpM.check_if_console_session(session_prog);

            SimpleLogger.logg(LOGFILE, "nr sessions = " + nrSessions + "  console session = " + console_session);

            if (nrSessions == 1 && console_session) {
                HelpM.runRDP(remote_desktop_path);
                SimpleLogger.logg(LOGFILE, "sessionChecker()-> session = console ; nr_sessions = " + nrSessions + "; session for line 20 will be opened");
                try {
                    Thread.sleep(1000 * 50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(autostarterCompounds.class.getName()).log(Level.SEVERE, null, ex);
                }
                HelpM.terminate_process_no_external_apps_in_use(process_name_rdp);
                setLine(LINE_10);
            } else if (console_session) {
                setLine(LINE_10);
            } else {
                setLine(LINE_20);
            }
        } catch (IOException ex) {
            Logger.getLogger(autostarterCompounds.class.getName()).log(Level.SEVERE, null, ex);

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
                        if (LINE.equals(LINE_20)) {
                            run_application(browsername);
                        } else {
                            run_application(mc_browser_10);
                        }
                        System.out.println("open browser");
                    } else if (e.getSource() == openEditor) {
                        if (LINE.equals(LINE_20)) {
                            run_application(editparname);
                        } else {
                            run_application(edit_param_10);
                        }
                    } else if (e.getSource() == openExport) {
                        if (LINE.equals(LINE_20)) {
                            run_application(exportname);
                        } else {
                            run_application(export_10);
                        }
                    } else if (e.getSource() == optionsx) {
                        System.out.println("open options");
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
     * Automaticly defines which line to check so fits for both line 10 & 20
     *
     * @throws AWTException
     */
    private void checker_20() throws AWTException, IOException {
        if (mccontrol_monitoring == false) {
            trayIcon.displayMessage("Note", "MCControl monitoring option is of", TrayIcon.MessageType.INFO);
            trayIcon.setToolTip("MCControl monitoring is of " + HelpM.get_proper_date_and_time_default_format());
        }

        while (mccontrol_monitoring == true) { // line 20
            if (HelpM.processRunning(mc_control_compounds_name_line20)) {
                trayIcon.setToolTip("MCControl running " + HelpM.get_proper_date_and_time_default_format());
            } else {
                run_application(mccontrolname);
                trayIcon.setToolTip("MCControl not running " + HelpM.get_proper_date_and_time_default_format());
                SimpleLogger.logg(LOGFILE, "Control Line 20 is off");
            }
            threadWait(check_mc_control_interval);
        }
    }

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

    /**
     * To be able to launch MCControl checking only on line 10
     *
     * @throws AWTException
     */
    private void checker_10() throws AWTException { // Line 10 MCControl is monitored (can be changed)

        if (mccontrol_monitoring == false) {
            trayIcon.displayMessage("Note", "MCControl monitoring option is of", TrayIcon.MessageType.INFO);
            trayIcon.setToolTip("MCControl monitoring is of " + HelpM.get_proper_date_and_time_default_format());
        }

        while (mccontrol_monitoring == true) { //*****************Changes regarding Session Opener
            checker_10_ext();
        }
    }

    /**
     *
     */
    private void checker_10_ext() {

        if (HelpM.processRunning(mc_control_compounds_name_line10)) {
            //
        } else {
            SimpleLogger.logg(LOGFILE, "Control Line 10 is off");
            //
            boolean paramGenClosed = HelpM.terminate_process_no_external_apps_in_use("ParamGen.exe");
            SimpleLogger.logg(LOGFILE, "ParamGen.exe closed: " + paramGenClosed);
            //
            wait_(3000);
            //
            run_application(mc_control_compounds_run_line10);
        }


//        display_tray_text(mccontrol, planupdate, paramgen, recalc_10, recalc_20);

        threadWait(check_mc_control_interval);
    }

    private synchronized void wait_(int millis) {
        try {
            wait(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(autostarterCompounds.class.getName()).log(Level.SEVERE, null, ex);
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
        new autostarterCompounds(argument);
    }

    @Override
    public void run() {
        try {
            if (LINE.equals(LINE_20)) {
                loadProperties();
                checker_20();
            } else if (LINE.equals(LINE_10)) {
                loadProperties();
                checker_10();
            }
        } catch (AWTException ex) {
            Logger.getLogger(autostarterCompounds.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(autostarterCompounds.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

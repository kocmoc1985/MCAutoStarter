/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package supplementary;

import com.jezhumble.javasysmon.JavaSysMon;
import com.jezhumble.javasysmon.ProcessInfo;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import mcautostarter.autostarterFederals;

/**
 *
 * @author Administrator
 */
public final class manualyStartMCControlSession {

    Properties p;
    int nrSessions;
    boolean mc_control_running;

    public manualyStartMCControlSession() {
        loadProperties();
        try {
            nrSessions = HelpM.count_sessions(p.getProperty("session_prog"), p.getProperty("logonsession_prefix"));
        } catch (IOException ex) {
            Logger.getLogger(manualyStartMCControlSession.class.getName()).log(Level.SEVERE, null, ex);
        }
        mc_control_running = processRunning(p.getProperty("mc_control_federals_name"));
        check();
    }

    public void check() {
        if (nrSessions == 1 && mc_control_running == false) {
            HelpM.runRDP(p.getProperty("remote_desktop_path"));
            try {
                Thread.sleep(4000);
            } catch (InterruptedException ex) {
                Logger.getLogger(manualyStartMCControlSession.class.getName()).log(Level.SEVERE, null, ex);
            }
            HelpM.terminate_process_no_external_apps_in_use(p.getProperty("process_name_rdp"));
            JOptionPane.showMessageDialog(null, "MCControl session was succesfully started");
        } else if (mc_control_running) {
            JOptionPane.showMessageDialog(null, "MCControl is allready running");
        }
    }

    /**
     *
     */
    private void loadProperties() {
        p = new Properties();
        try {
            p.load(new FileInputStream("autostarterfed.properties"));
            p.list(System.out);
        } catch (IOException ex) {
            Logger.getLogger(autostarterFederals.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Checks if the given process is running
     *
     * @processName str the process name to search for "Browser.exe"
     * @return
     */
    public static boolean processRunning(String processName) {
        JavaSysMon monitor = new JavaSysMon();
        ProcessInfo[] pinfo = monitor.processTable();

        for (int i = 0; i < pinfo.length; i++) {
            String pname = pinfo[i].getName();
            if (pname.equals(processName)) {
                return true;
            }

        }
        return false;
    }

    public static void main(String[] args) {
        new manualyStartMCControlSession();
    }
}

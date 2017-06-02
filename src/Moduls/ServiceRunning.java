/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Moduls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Is able to monitor windows services Service names to monitor "browser
 * (Computer Browser)" & "lanmanserver (Server)"
 *
 * @author Administrator
 */
public class ServiceRunning implements Runnable {

    private String SERVICE_NAME = "";
    private int INTERVAL_BETWEEN_CHECK_IN_MS = 0;
    private final String PATH_TO_SC_APP = "c:/windows/system32/sc.exe";
    private final String PARAM_1 = "query";
    private boolean first_time_1 = true;
    private final String LOGGFILE = "service_running_modul.log";

    public ServiceRunning(String service_name, int interval_between_check) {
        this.SERVICE_NAME = service_name;
        INTERVAL_BETWEEN_CHECK_IN_MS = interval_between_check;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (check_if_service_running(PATH_TO_SC_APP, SERVICE_NAME, "STATE") == false) {
                    run_service(PATH_TO_SC_APP, "start", SERVICE_NAME);
                    SimpleLogger.logg(LOGGFILE, "Service = " + SERVICE_NAME + " was turned of, turning it on");
                } else {
                    if (first_time_1) {
                        SimpleLogger.logg(LOGGFILE, "Service = " + SERVICE_NAME + " is on at startup");
                        first_time_1 = false;
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ServiceRunning.class.getName()).log(Level.SEVERE, null, ex);
            }
            wait_(INTERVAL_BETWEEN_CHECK_IN_MS);
        }
    }

    public static void run_service(String path_to_program, String p1, String p2) {
        String[] cmd = {path_to_program, p1, p2};
        try {
            Process process = Runtime.getRuntime().exec(cmd);
        } catch (IOException ex) {
            Logger.getLogger(ServiceRunning.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
//        try {
//            ServiceRunning running = new ServiceRunning("messenger", 1);
//            System.out.println("" + running.check_if_service_running("c:/windows/system32/sc.exe", "messenger", "STATE"));
//        } catch (IOException ex) {
//            Logger.getLogger(ServiceRunning.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        new Thread(new ServiceRunning("messenger", 10000)).start();
//        new Thread(new ServiceRunning("eventlog", 10000)).start();
//        run_service("c:/windows/system32/sc.exe", "start", "lanmanserver");
    }

    /**
     * @tags grab_output, grab_out_put, grab output
     * @param path_to_executing_app
     * @param serviceName
     * @param expressionToMatch
     * @return
     * @throws IOException
     */
    public boolean check_if_service_running(String path_to_executing_app, String serviceName, String expressionToMatch) throws IOException {
        String[] cmd = {path_to_executing_app, PARAM_1, serviceName};//c:/windows/system32/sc.exe

        String line;
        InputStream stdout = null;

        // launch EXE and grab stdin/stdout and stderr
        Process process = Runtime.getRuntime().exec(cmd);
        stdout = process.getInputStream();

        // clean up if any output in stdout
        BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(stdout));
        while ((line = brCleanUp.readLine()) != null) {
//                System.out.println("---------------------->" + line);
            if (line.toLowerCase().contains(expressionToMatch.toLowerCase())) {
                if (line.contains("RUNNING")) {
                    System.out.println("service = '" + serviceName + "' is running");
                    brCleanUp.close();
                    return true;
                }
            }
//            System.out.println("[Stdout] " + line);
        }
        return false;
    }

    private void wait_(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(PressAway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package supplementary;

import Moduls.SimpleLogger;
import com.jezhumble.javasysmon.JavaSysMon;
import com.jezhumble.javasysmon.ProcessInfo;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import mcautostarter.autoStarterBackupClient;

/**
 *
 * @author Administrator
 */
public class HelpM {

    public static int get_file_size_mb(File file) {
        File f = file;
        double rst_unrounded = (double) f.length() / 1048576;
        double rst_rounded = Double.parseDouble(String.format("%2.2f", rst_unrounded).replace(",", "."));
        return (int) rst_rounded;
    }

    public static String[] get_windows_to_press_away_federal_mogul(Properties p) {
        if (p == null) {
            String[] x = {"SCommDrv", "SAIA"};
            return x;
        }
        String windows_to_press_away = p.getProperty("windows_to_press_away", "");
        return windows_to_press_away.split(",");
    }

    /**
     * @tested @tags grab_output, grab_out_put, grab output
     * @param path_to_executing_app
     * @return
     * @throws IOException
     */
    public static boolean check_if_console_session(String path_to_executing_app) throws IOException {
        String[] cmd = {path_to_executing_app, "session"};//c:/windows/system32/query.exe

        String line;
        InputStream stdout = null;

        // launch EXE and grab stdin/stdout and stderr
        Process process = Runtime.getRuntime().exec(cmd);
        stdout = process.getInputStream();

        // clean up if any output in stdout
        BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(stdout));
        while ((line = brCleanUp.readLine()) != null) {
            String pattern_1 = ">console"; //this means that this session belongs to my active session
            if (line.toLowerCase().contains(pattern_1.toLowerCase())) {
                return true;
            }
        }
        brCleanUp.close();
        return false;
    }

    public static Properties properties_load_properties(String path_andOr_fileName) {
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(path_andOr_fileName));
        } catch (IOException ex) {
            System.out.println("" + ex);
        }
        return p;
    }

    /**
     *
     * @param pathToLogonssesionsApp
     * @param expressionToMatch
     * @return
     * @throws IOException
     */
    public static int count_sessions(String pathToLogonssesionsApp, String expressionToMatch) throws IOException {
        int nrSessions = 0;
        String[] cmd = {pathToLogonssesionsApp, "session"};//c:/windows/system32/query.exe

        String line;
        InputStream stdout = null;

        // launch EXE and grab stdin/stdout and stderr
        Process process = Runtime.getRuntime().exec(cmd);
        stdout = process.getInputStream();

        // clean up if any output in stdout
        BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(stdout));
        while ((line = brCleanUp.readLine()) != null) {
            if (checkStringContains(line.toLowerCase(), expressionToMatch.toLowerCase())) {
                nrSessions++;
            }
            System.out.println("[Stdout] " + line);
        }
        brCleanUp.close();

        brCleanUp.close();
        return nrSessions++;
    }

//    public static void main(String[] args) {
//        try {
//            int i = count_sessions("c:/windows/system32/query.exe", "administrator");
//            System.out.println("sessions_ammount = " + i);
//        } catch (IOException ex) {
//            Logger.getLogger(HelpM.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
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
            if (pname.toLowerCase().equals(processName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static String get_proper_date_time_same_format_on_all_computers() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH_mm");
        Calendar calendar = Calendar.getInstance();
        return formatter.format(calendar.getTime());
    }

    /**
     * This method is the best one to get the local default time used on the
     * computer
     *
     * @return
     */
    public static String get_proper_date_and_time_default_format() {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        DateFormat f1 = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
        Date d = cal.getTime();
//        System.out.println(f1.format(d));
        return f1.format(d);
    }

    /**
     * @deprecated @param searchedString
     * @return
     */
    public static boolean checkStringContains(String str_to_analyze, String searchedString) {
        int index1 = 0;
        index1 = str_to_analyze.indexOf(searchedString);
        if (index1 != -1) {
            return true;
        }
        return false;
    }

    /**
     * This method is done for communicating between MCControl and my app
     */
    public static boolean checkIfHanging(String path_to_mccontrol_log) {
        try {
//              BufferedReader br = new BufferedReader(new FileReader(""));//("C:/"+filename +"."+"skv")
            BufferedReader br = new BufferedReader(new FileReader(path_to_mccontrol_log));//("C:/"+filename +"."+"skv")
            String act = "";
            String prev = "";

            while (act != null) {
                prev = act;
                act = br.readLine();
            }

            if (checkStringContains(prev, "Crash")) {
                return true;
            }

        } catch (IOException ex) {
            return false;
        }
        return false;
    }

    /**
     *
     */
    public static void runRDP(String path) {
        String[] commands2 = {"c:/windows/system32/mstsc.exe", path};// "c:/black87.rdp"
        try {
            Process pr = Runtime.getRuntime().exec(commands2);
        } catch (IOException ex) {
        }
    }

    /**
     * Disconnects the current logged on session
     *
     * @param args
     */
    public static void disconnect_current_session() {
        String[] commands2 = {"c:/windows/system32/tsdiscon.exe"};// "c:/black87.rdp" ---//c:/windows/system32/logonsessions.exe
        try {
            Process pr = Runtime.getRuntime().exec(commands2);
        } catch (IOException ex) {
        }
    }

    /**
     * Launches the ping
     */
    public static void disconnect_current_session2() {
        String[] commands2 = {"cmd", "/c", "start", "\"tsdiscon\"", "tsdiscon"};
        try {
            Process pr = Runtime.getRuntime().exec(commands2);

        } catch (IOException ex) {
            System.out.println("" + ex);
        }
    }

    /**
     *
     * @param args
     */
    public static void terminate_process(String processName) {
        String[] commands2 = {"other/pskill.exe", processName};// c:/windows/system32/pskill.exe
        try {
            Process pr = Runtime.getRuntime().exec(commands2);
        } catch (IOException ex) {
        }
    }

    /**
     * Terminate a process using the process name
     *
     * @param processName
     */
    public static boolean terminate_process_no_external_apps_in_use(String processName) {
        JavaSysMon monitor = new JavaSysMon();
        ProcessInfo[] pinfo = monitor.processTable();
        //
        for (int i = 0; i < pinfo.length; i++) {
            String pname = pinfo[i].getName();
            int pid = pinfo[i].getPid();
            if (pname.toLowerCase().equals(processName.toLowerCase())) {
                monitor.killProcess(pid);
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @CalledBy: StartInRightSession
     */
    public static void press_away_errormessages(int nr_attempts) {
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(HelpM.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (int i = 0; i < nr_attempts; i++) {
            //==================================================================
            robot.keyPress(KeyEvent.VK_ALT);
            //==
            robot.keyPress(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_TAB);
            robot.keyPress(KeyEvent.VK_TAB);
            robot.keyRelease(KeyEvent.VK_TAB);
            //==
            robot.keyRelease(KeyEvent.VK_ALT);
            //==================================================================

            robot.keyPress(KeyEvent.VK_ESCAPE);
            wait_(50);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
            wait_(50);
        }
    }

    public static void main(String[] args) {
        press_away_errormessages(10);
    }

    public static void run_java_app(String app_name, String arg) {
        wait_(200);
        String[] commands2 = {"java", "-jar", app_name, arg};
        try {
            Process p = Runtime.getRuntime().exec(commands2);
        } catch (IOException ex) {
            SimpleLogger.logg("exceptions.log", "failed to run java_app = " + app_name + "   " + ex.toString());
            Logger.getLogger(HelpM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param err_output_file_path
     * @param backup_dir
     * @param err_output_file_name
     */
    public static void write_error_output_to_backup_dir(String err_output_file_path, String backup_dir, String err_output_file_name) {
        //Copy error output to the backup directory
        if (err_output_file_path.isEmpty() || err_output_file_name.isEmpty()) {
            return;
        }
        try {
            copy_file(err_output_file_path, backup_dir + "/" + autoStarterBackupClient.ERR_OUTPUT_FILE_NAME);
        } catch (Exception ex) {
            Logger.getLogger(HelpM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void err_output_to_file() {
        //Write error stream to a file
        HelpM.create_dir_if_missing("err_output");
        try {
            String err_file = "err_" + HelpM.get_proper_date_time_same_format_on_all_computers() + ".txt";
            String output_path = "err_output/" + err_file;

            PrintStream out = new PrintStream(new FileOutputStream(output_path));
            System.setErr(out);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(HelpM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void create_dir_if_missing(String path_and_folder_name) {
        File f = new File(path_and_folder_name);
        if (f.exists() == false) {
            f.mkdir();
        }
    }

    /**
     * The best method for copying files, it is now very fast to! But!!! This
     * method can copy a file which is beeing used by another program. Copy
     * files
     *
     * @param file_to_copy
     * @param name_of_duplicate
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void copy_file(String file_to_copy, String name_of_duplicate) throws Exception {
        File inputFile = new File(file_to_copy);
        File outputFile = new File(name_of_duplicate);

        FileInputStream in = new FileInputStream(inputFile);
        FileOutputStream out = new FileOutputStream(outputFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }

    private static void wait_(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(HelpM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

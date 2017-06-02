/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Moduls;

import com.jezhumble.javasysmon.JavaSysMon;
import com.jezhumble.javasysmon.ProcessInfo;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class LogScanner implements Runnable {

    private boolean RUN = true;
    //====
    private int REFRESH_INTERVAL;
    private String PATH_TO_LOG_FILE = "";
    private static ArrayList<String> FAILURE_MESSAGE_LIST;
    private int FAILURES_BEFORE_ACTION;
    private String NAME_OF_PROGRAM_TO_TERMINATE = "";
    //====
    private final static String LOGFILE = "log_scanner_modul.log"; //for logging this class
    //====
    private boolean one_time_1 = false;

    /**
     *
     * @param path_to_log_file - the path to the log file which is to be
     * analysed
     * @param failure_msg - the failure message to search for
     * @param failures_before_action - ammount of failures to be found before
     * taking action
     * @param refresh_interval_in_minutes - interval for the check
     *
     */
    public LogScanner(
            String path_to_log_file,
            ArrayList<String> failures_msg_list,
            int failures_before_action,
            int refresh_interval_in_minutes,
            String name_of_program_to_terminate) {

        this.REFRESH_INTERVAL = refresh_interval_in_minutes;
        this.PATH_TO_LOG_FILE = path_to_log_file;
        FAILURE_MESSAGE_LIST = failures_msg_list;
        this.FAILURES_BEFORE_ACTION = failures_before_action;
        this.NAME_OF_PROGRAM_TO_TERMINATE = name_of_program_to_terminate;
    }

    private void stop() {
        RUN = false;
    }

    @Override
    public void run() {

        while (RUN) {
            try {
                if (scan_log_file(PATH_TO_LOG_FILE, FAILURE_MESSAGE_LIST, FAILURES_BEFORE_ACTION)) {
                    terminate_process_no_external_apps_in_use(NAME_OF_PROGRAM_TO_TERMINATE);
                }
                if (!one_time_1) {
                    one_time_1 = true;
                    SimpleLogger.logg(LOGFILE, "[OBS!]startup-check, log file found");
                }

            } catch (FileNotFoundException ex) {
                SimpleLogger.logg(LOGFILE, "Logfile = " + PATH_TO_LOG_FILE + " not found");
                stop();
                Logger.getLogger(LogScanner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                SimpleLogger.logg(LOGFILE, ex.toString());
                stop();
                Logger.getLogger(LogScanner.class.getName()).log(Level.SEVERE, null, ex);
            }
            //====
            wait_(60000 * REFRESH_INTERVAL);
        }
    }

    private void wait_(int millis) {
        synchronized (this) {
            try {
                wait(millis);
            } catch (InterruptedException ex) {
                Logger.getLogger(LogScanner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Analyses the log for searched failures
     *
     * @param log_path - the path of the "mixcont.log" file
     * @param failure_msg - the failure message which is ment to be the problem
     * @param failures_before_restart - nr failures after which the restart
     * occurs
     * @return true if the method defined that all the conditions are met & the
     * action is to be taken
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean scan_log_file(String log_path, ArrayList<String> failures_msg_list, int failures_before_restart) throws FileNotFoundException, IOException {
        FileReader fin = new FileReader(log_path);
        Scanner scan = new Scanner(fin);

        LinkedList<String> log_list = new LinkedList<String>();

        String CHECKED = "#mcautostarter#";

        int FAILURE_COUNTER = 0;

        //Build list
        while (scan.hasNextLine()) {
            log_list.addFirst(scan.nextLine());
            if (log_list.size() > 50) {
                log_list.removeLast();
            }
        }

        //Check for "#checked#" mark to skip reacting on same loggs twice
        if (log_list.peekFirst().equals(CHECKED)) {
            System.out.println("Found checked mark, skipping analyse!");
            return false;
        }


        //Analyze list
        long date_time_prev = 0;
        for (String logEntry : log_list) { // starting with the last entry first
//            System.out.println("" + logEntry);

            String[] arr = logEntry.split(" ");

            // this means that the act line is marked with #mcautostarter# 
            // and further analyse must be cancelled
            if (logEntry.equals(CHECKED)) {
                System.out.println("Found checked mark after starting analyse (it means not enough failures found), skipping analyse!");
                break;
            }

            //This means that the current line is not in usual format
            //which starts with date, the algorithm jumps wo next entry with "continue"
            if (arr.length == 0 || arr.length == 1) {
                continue;
            }

            //======
            String date_act_entry = "";
            date_act_entry += arr[0];
            date_act_entry = date_act_entry.trim();

            String date_and_time_act_entry = "";
            date_and_time_act_entry += arr[0] + " " + arr[1];
            date_and_time_act_entry = date_and_time_act_entry.trim();
            long date_time_act = dateToMillisConverter(date_and_time_act_entry);

            //This means current line is not what it used to be
            //therefore, this line is skipped.
            if (date_time_act == -1) {
                continue;
            }

            //=====

            long today = System.currentTimeMillis();
            String today_ = millisToDateConverter("" + today);


            //
            if (today_.trim().equals(date_act_entry)) { // this means that the last entry in log has todays date
                String last_log_msg = logEntry.substring(20); // cut date
                last_log_msg = last_log_msg.replaceAll(" ", "");


                //Here is tested wether the actual entry meets the condition to be defined as a failure

                for (String failure_msg : failures_msg_list) {
                    failure_msg = failure_msg.replaceAll(" ", "");

                    if (last_log_msg.equals(failure_msg)) {

                        if (Math.abs(date_time_act - date_time_prev) < 120000) {
//                        System.out.println("FAILURE_ADDED!");
                            FAILURE_COUNTER++;
                        } else {
//                            System.out.println("time diff to large");
                        }

                        date_time_prev = date_time_act;
                    }
                }
            } else {
                break;
            }
        }

        if (FAILURE_COUNTER >= failures_before_restart) {
            //Add mark skip reacting on same loggs twice
            FileWriter fout = new FileWriter(log_path, true);
            fout.write("\r" + CHECKED);
            fout.close();
//            System.out.println("failures = " + FAILURE_COUNTER);
//            System.out.println("action taken!");
            SimpleLogger.logg(LOGFILE, "Action taken; Searched failure = " + FAILURE_MESSAGE_LIST);
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
//        try {
//            scan_log_file("mixcont.log", "Error at reading  items values", 5);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(LogScanner.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(LogScanner.class.getName()).log(Level.SEVERE, null, ex);
//        }

//        ArrayList<String> messages = new ArrayList<String>();
//
//        messages.add("Error at writing item value");
//        messages.add("Error at reading  items values");

        Properties p = new Properties();
        try {
            p.load(new FileInputStream("autostarterphoenix.properties"));
        } catch (IOException ex) {
            Logger.getLogger(LogScanner.class.getName()).log(Level.SEVERE, null, ex);
        }

        String msg = p.getProperty("log_scanner_failure_messages", "");

        new Thread(new LogScanner(
                "mixcont.log",
                build_failure_message_list(msg),
                5,
                1,
                "control.exe")).start();
    }

    /**
     * This one only for testing the module
     *
     * @deprecated
     * @param failure_msgs
     * @return
     */
    private static ArrayList<String> build_failure_message_list(String failure_msgs) {
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

    private static long dateToMillisConverter(String date_yyyy_mm_dd) {
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        try {
            return formatter.parse(date_yyyy_mm_dd).getTime();
        } catch (ParseException ex) {
//            Logger.getLogger(LogScanner.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }

    private static String millisToDateConverter(String millis) {
        DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        long now = Long.parseLong(millis);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);

//        System.out.println(now + " = " + formatter.format(calendar.getTime()));
        return formatter.format(calendar.getTime());
    }

    /**
     * Terminate a process using the process name
     *
     * @param processName
     */
    public static void terminate_process_no_external_apps_in_use(String processName) {
        JavaSysMon monitor = new JavaSysMon();
        ProcessInfo[] pinfo = monitor.processTable();

        for (int i = 0; i < pinfo.length; i++) {
            String pname = pinfo[i].getName();
            int pid = pinfo[i].getPid();
            if (pname.equals(processName)) {
                monitor.killProcess(pid);
            }
        }
    }
}

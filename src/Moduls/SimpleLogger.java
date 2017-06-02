/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Moduls;

import com.jezhumble.javasysmon.JavaSysMon;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import supplementary.HelpM;

/**
 *
 * @author Administrator
 */
public class SimpleLogger {

    private static final int MAX_FILE_SIZE_MB = 50;
    private static JavaSysMon monitor = new JavaSysMon();
    private static final String DEFAULT_LOG_MSG = "default";
    private static final String EXCEPTION_LOG_MSG = "exception";

    public static void logg(String LOGFILE, String textToWrite) {
        if (get_file_size_mb(LOGFILE) > MAX_FILE_SIZE_MB) {
            File f = new File(LOGFILE);
            f.delete();
            return;
        }

        write_to_file(LOGFILE, textToWrite);

        //This makes that the "copied files log" is writen to the backup dir also
        if (LOGFILE.equals(Backuper.LOGGFILE_COPIED_FILES)) {
            write_to_file(Backuper.BACKUP_DIR + "/" + Backuper.LOGGFILE_COPIED_FILES, textToWrite);
        }
    }

    private static void write_to_file(String LOGFILE, String textToWrite) {
        try {
            FileWriter fstream = new FileWriter(LOGFILE, true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(build_log_message(DEFAULT_LOG_MSG, textToWrite));
            out.newLine();
            out.flush();
        } catch (Exception ex) {
            System.out.println("" + ex);
        }
    }

    public static void logg_no_append(String LOGFILE, String textToWrite) {
        if (get_file_size_mb(LOGFILE) > MAX_FILE_SIZE_MB) {
            return;
        }
        try {
            FileWriter fstream = new FileWriter(LOGFILE, false);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(build_log_message(DEFAULT_LOG_MSG, textToWrite));
            out.newLine();
            out.flush();
        } catch (Exception ex) {
            System.out.println("" + ex);
        }
    }

//    public static void loggException(String LOGFILE, String textToWrite) {
//        if (get_file_size_mb(LOGFILE) > MAX_FILE_SIZE_MB) {
//            return;
//        }
//        try {
//            FileWriter fstream = new FileWriter(LOGFILE, true);
//            BufferedWriter out = new BufferedWriter(fstream);
//            out.write(build_log_message(EXCEPTION_LOG_MSG, textToWrite));
//            out.newLine();
//            out.flush();
//        } catch (Exception ex) {
//            System.out.println("" + ex);
//        }
//    }
    private static String build_log_message(String type, String text_to_write) {
        if (type.equals(DEFAULT_LOG_MSG)) {
            return "[" + HelpM.get_proper_date_and_time_default_format() + "] " + "  " + "[pid=" + monitor.currentPid() + "]    " + text_to_write;
        } else if (type.equals(EXCEPTION_LOG_MSG)) {
            return "[" + HelpM.get_proper_date_and_time_default_format() + "] "
                    + "  " + "[pid=" + monitor.currentPid() + "]    " + "  " + " [Exception] " + text_to_write;
        } else {
            return "build_log_message(...) - > type not defined";
        }
    }

    private static double get_file_size_mb(String path) {
        File f = new File(path);
        return f.length() / 1048576;
    }

    public static double get_file_size_kb(String file_path) {
        File f = new File(file_path);
        double rst_unrounded = (double) f.length() / 1024;
        return Math.round(rst_unrounded);
    }

    public static int getLineNumber() {
        return Thread.currentThread().getStackTrace()[2].getLineNumber();
    }

    public static String getMethodName(final int depth) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[ste.length - 1 - depth].getMethodName();
    }

    public static void main(String[] args) {
        System.out.println("" + getMethodName(0));
    }
}

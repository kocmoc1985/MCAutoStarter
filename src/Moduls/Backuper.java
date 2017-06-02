/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Moduls;

import Interfaces.Tray;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import mcautostarter.autoStarterBackupClient;
import supplementary.HelpM;

/**
 *
 * @author Administrator
 */
public class Backuper implements Runnable {

    private ArrayList<String> folders_to_copy = new ArrayList<String>();
    private ArrayList<String> folders_not_to_copy = new ArrayList<String>();
    private final static String APP_NAME = "mcautostarter.jar";
    private final static String LOGGFILE = "backuper_modul.log";
    public final static String LOGGFILE_COPIED_FILES = "backuper_modul_files_copied.log";
    private final static String LOGGFILE_EXEPTIONS = "backuper_modul_exeptions.log";
    private boolean MODUL_ON = true;
    public static String BACKUP_DIR = "";
    private String BACKUP_DIR_2 = "";
    private static String PROPERTY_PATH = "";
    private String BACKUP_TIME = "00:00";
    private boolean INTERVAL_BASED_BACKUP = false;
    private int INTERVAL_IN_HOWRS;
    private int MAX_FILE_SIZE_MB;
    //==========================================================================
    private Tray tray;
    private boolean BACKUP_NOT_DONE_DUE_TO__FOLDERS_TO_COPY__LIST_EMPTY = false;
    private boolean DESTINATION_NOT_VALID_FAILURE = false;
    private int SUCCESSFUL_FOLDER_COPY = 0;
    //==========================================================================
    public final static int RESTART = 0;
    public final static int SHUTDOWN = 1;
    public final static int HIBERNATE = 2;
    public final static int NOTHING = 3;
    //==========================================================================
    private String ERR_OUTPUT_FILE_PATH = "";
    private String ERR_OUTPUT_FILE_NAME = "";

    /**
     *
     * @param property_path - the path to the property file
     * @param inteval_based_backup - type of backup: timebased = false,
     * intervalbased = true
     */
    public Backuper(String property_path, Tray tr, String err_output_file_path, String err_output_file_name) {
        PROPERTY_PATH = property_path;
        this.ERR_OUTPUT_FILE_PATH = err_output_file_path;
        this.ERR_OUTPUT_FILE_NAME = err_output_file_name;
        tray = tr;
        loadProperties();
    }

    private void loadProperties() {
        Properties p = HelpM.properties_load_properties(PROPERTY_PATH);
        MODUL_ON = Boolean.parseBoolean(p.getProperty("backuper_modul_on", "true"));
        BACKUP_DIR = p.getProperty("backup_dir", "");
        BACKUP_DIR_2 = p.getProperty("backup_dir_2", "");
        BACKUP_TIME = p.getProperty("backup_time", "00:00");
        INTERVAL_BASED_BACKUP = Boolean.parseBoolean(p.getProperty("interval_based_backup", "false"));
        INTERVAL_IN_HOWRS = Integer.parseInt(p.getProperty("interval_in_howrs", "24"));
        MAX_FILE_SIZE_MB = Integer.parseInt(p.getProperty("max_file_size_mb", "10000"));

        //log
        SimpleLogger.logg_no_append(LOGGFILE, "LOADING PROPERTIES " + get_proper_time_default_format(1));
        SimpleLogger.logg_no_append(LOGGFILE, "property_path = " + PROPERTY_PATH);
        SimpleLogger.logg(LOGGFILE, "backuper_modul_on = " + MODUL_ON);
        SimpleLogger.logg(LOGGFILE, "interval_based_backup = " + INTERVAL_BASED_BACKUP);
        SimpleLogger.logg(LOGGFILE, "backup_dir = " + BACKUP_DIR);
        if (BACKUP_DIR_2.isEmpty() == false) {
            SimpleLogger.logg(LOGGFILE, "backup_dir_2 (in use) = " + BACKUP_DIR_2);
        }
        SimpleLogger.logg(LOGGFILE, "backup_time = " + BACKUP_TIME);
    }

    private void reload_properties_before_backup() {
        SUCCESSFUL_FOLDER_COPY = 0; //must be reset here
        //reload all other properties
        loadProperties();
        //========================================
        //reload folders_to_copy before each backup
        folders_to_copy = build_array_list_from_property("folders_to_copy");
        folders_not_to_copy = build_array_list_from_property("folders_to_skip");
        help_method_01();
    }

    /**
     * Just some logging operations
     */
    private void help_method_01() {
        if (folders_to_copy != null && folders_not_to_copy != null) {
            String paths = "";
            for (String path : folders_to_copy) {
                paths += path + ";";
            }
            SimpleLogger.logg(LOGGFILE, "folders_to_backup = " + paths);

            paths = "";
            for (String path : folders_not_to_copy) {
                paths += path + ";";
            }
            SimpleLogger.logg(LOGGFILE, "folders_to_skip = " + paths);
        }
    }

    /**
     * Build list with directories to copy or directories not to copy
     *
     * @param property_name - the name of property "folders_to_copy" or
     * "folders_not_to_copy"
     * @param arr_list
     */
    private ArrayList<String> build_array_list_from_property(String property_name) {
        try {
            Properties p = HelpM.properties_load_properties(PROPERTY_PATH);
            String dirs = p.getProperty(property_name, "");

            if (property_name.equals("folders_to_copy") && dirs.isEmpty()) {
                return null;
            }

            String[] dir_arr = dirs.split(";");
            ArrayList<String> arr_list = new ArrayList<String>();
            arr_list.addAll(Arrays.asList(dir_arr));
            return arr_list;
        } catch (Exception ex) {
            Logger.getLogger(Backuper.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    @Override
    public void run() {
        if (INTERVAL_BASED_BACKUP) {
            while (true) {
                wait_(3600000 * INTERVAL_IN_HOWRS); //3600000 = 1howr
                a01();
            }
        } else {
            while (true) {
                String time = get_proper_time_same_format_on_all_computers();
                System.out.println("time = " + time);
                if (time.contains(BACKUP_TIME)) {
                    a01();
                }
                wait_(60001);//1min - > wait 1min before checking the time
                reload_backup_time();
            }
        }
    }

    private void a01() {
        reload_properties_before_backup();
        if (MODUL_ON) {
            SimpleLogger.logg(LOGGFILE_COPIED_FILES, "====================Backup DIR 1=========================");
            System.out.println("backuping");
            backup(BACKUP_DIR);
            if (BACKUP_DIR_2.isEmpty() == false) {
                SimpleLogger.logg(LOGGFILE_COPIED_FILES, "====================Backup DIR 2=========================");
                backup(BACKUP_DIR_2);
            }


            SimpleLogger.logg(LOGGFILE_COPIED_FILES, "====================Backup READY=========================");
            restart_or_shutdown(RESTART);
        }
    }

    private void backup(String backup_dir) {
        if (folders_to_copy == null) {
            tray.displayTrayErrorMessage(null, "no folders to copy are set, backup canceled!");
            BACKUP_NOT_DONE_DUE_TO__FOLDERS_TO_COPY__LIST_EMPTY = true;
            return;
        }

        for (String path_source : folders_to_copy) {
            SimpleLogger.logg(LOGGFILE_COPIED_FILES, "\n\n\n=================dir beeing backuped is " + path_source + "======================");

            //1 - source not valid, 2 - destination not valid, 0 - ok
            int responce_code = copyDirectory(new File(path_source), new File(backup_dir));

            if (responce_code == 0) { // no failure
                SUCCESSFUL_FOLDER_COPY++;
            } else if (responce_code == 1) {// source not valid
                //
            } else if (responce_code == 2) { // destination not valid
                DESTINATION_NOT_VALID_FAILURE = true;
                tray.displayTrayErrorMessage(null, "Destination not valid: " + backup_dir);
                wait_(3000);
                break;
            }
        }
    }

    /**
     *
     * @param turn_of_after_restart - defines wether the computer is to be
     * turned off after backup
     */
    public void do_backup_now(int restart_shutdown_hibernate_nothing) {
        reload_properties_before_backup();

        SimpleLogger.logg(LOGGFILE_COPIED_FILES, "====================Backup Now DIR 1=========================");
        System.out.println("backuping");
        backup(BACKUP_DIR);
        if (BACKUP_DIR_2.isEmpty() == false) {
            SimpleLogger.logg(LOGGFILE_COPIED_FILES, "====================Backup Now DIR 2=========================");
            backup(BACKUP_DIR_2);
        }
        SimpleLogger.logg(LOGGFILE_COPIED_FILES, "====================Backup READY=========================");



        restart_or_shutdown(restart_shutdown_hibernate_nothing);
    }

    /**
     *
     * @param action - 0 = restart; 1 = shutdown; 2 = hibernate
     */
    private void restart_or_shutdown(int action) {

        HelpM.write_error_output_to_backup_dir(ERR_OUTPUT_FILE_PATH, BACKUP_DIR, ERR_OUTPUT_FILE_NAME);

        if (BACKUP_NOT_DONE_DUE_TO__FOLDERS_TO_COPY__LIST_EMPTY) {
            tray.displayTrayInfoMessage(null, "Backup failed - folders to copy list is empty!");
            wait_(3000);
            return;
        }

        if (DESTINATION_NOT_VALID_FAILURE && SUCCESSFUL_FOLDER_COPY == 0) {
            tray.displayTrayInfoMessage(null, "Backup failed - destination not valid!");
            wait_(3000);
            return;
        }

        tray.displayTrayInfoMessage(null, "Backup Ready!");
        wait_(3000);

        //==================

        if (action == RESTART) {
            restart();
        } else if (action == NOTHING) {
            //do nothing
        } else if (action == SHUTDOWN) {
            try {
                shut_down_immediately();
            } catch (IOException ex) {
                SimpleLogger.logg(LOGGFILE, "Failed to shutdown the computer: " + ex);
                Logger.getLogger(Backuper.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (action == HIBERNATE) {
            try {
                hibernate();
            } catch (IOException ex) {
                Logger.getLogger(Backuper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    //===========================================================================
    /**
     *
     * @param source
     * @param destination
     * @return 1 - source not valid, 2 - destination not valid, 0 - ok
     */
    private int copyDirectory(File source, File destination) {
        try {
            if (source.exists() == false) {
                tray.displayTrayWarningMessage(null, "Source: " + source.getPath() + " is not valid or missing");
                SimpleLogger.logg(LOGGFILE_COPIED_FILES, "Source: " + source.getPath() + " is not valid or missing");
                wait_(3000);
                return 1;
            }
            copyDirectory(source, destination, null);
        } catch (IOException ex) {
            Logger.getLogger(Backuper.class.getName()).log(Level.SEVERE, null, ex);
            return 2;
        }
        return 0;
    }

    private void copyDirectory(File source, File destination, FileFilter filter) throws IOException {
        File nextDirectory = new File(destination, source.getName());

        // create the directory if necessary
        if (!nextDirectory.exists() && !nextDirectory.mkdirs()) {
            Object[] filler = {nextDirectory.getAbsolutePath()};
            String message = "Destination: " + destination.getAbsolutePath() + " ---> not valid.";
            SimpleLogger.logg(LOGGFILE_COPIED_FILES, message);
            throw new IOException(message);
        }

        File[] files = source.listFiles();

        // and then all the items below the directory
        for (int n = 0; n < files.length; ++n) {
            if (filter == null || filter.accept(files[n])) {
                if (files[n].isDirectory()) {
                    if (dir_exists_in_skip_list(files[n]) == false) {
                        copyDirectory(files[n], nextDirectory, filter);
                    } else {
                        SimpleLogger.logg(LOGGFILE_COPIED_FILES, "skip folder: " + files[n].getAbsolutePath());
                    }
                } else {
                    int file_size = HelpM.get_file_size_mb(files[n]);
                    if (file_size < MAX_FILE_SIZE_MB) {
                        copyFile(files[n], nextDirectory);
                    } else {
                        SimpleLogger.logg(LOGGFILE_COPIED_FILES, "file not copied due size: " + files[n].getPath() + "  /  size = " + file_size + " mb" + "   /  max file_size = " + MAX_FILE_SIZE_MB);
                    }
                }
            }
        }
    }

    /**
     *
     * @param file_to_compare_with
     * @return
     */
    private boolean dir_exists_in_skip_list(File file_to_compare_with) {
        boolean retur = false;
        for (String folder_path_not_to_copy : folders_not_to_copy) {
            folder_path_not_to_copy = folder_path_not_to_copy.toLowerCase();
            folder_path_not_to_copy = folder_path_not_to_copy.replace("/", "\\");

            String file_path_to_compare_with = file_to_compare_with.getAbsolutePath().toLowerCase();

            if (folder_path_not_to_copy.equals(file_path_to_compare_with)) {
                return true;
            } else {
                retur = false;
            }
        }
        return retur;
    }

    private void copyFile(File source, File destination) {
        // if the destination is a dir, what we really want to do is create
        // a file with the same name in that dir
        if (destination.isDirectory()) {
            destination = new File(destination, source.getName());
        }
        boolean failure = false;
        FileInputStream input = null;
        try {
            input = new FileInputStream(source);
        } catch (FileNotFoundException ex) {
            SimpleLogger.logg(LOGGFILE_COPIED_FILES, "could't copy file: " + destination.getPath());
            SimpleLogger.logg(LOGGFILE_EXEPTIONS, "" + ex);
            failure = true;
            Logger.getLogger(Backuper.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (failure) {
            return;
        }

        if (source_file_newer_then_destination_file(source, destination)) {
            try {
                copyFile(input, destination);
            } catch (IOException ex) {
                SimpleLogger.logg(LOGGFILE_EXEPTIONS, "" + ex);
                Logger.getLogger(Backuper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static boolean source_file_newer_then_destination_file(File source, File destination) {
        if (source.lastModified() > destination.lastModified()) {
            return true;
        } else {
            return false;
        }
    }

    private static void copyFile(InputStream input, File destination) throws IOException {

        SimpleLogger.logg(LOGGFILE_COPIED_FILES, "copied: " + destination.getPath());

        OutputStream output = null;

        output = new FileOutputStream(destination);

        byte[] buffer = new byte[1024];

        int bytesRead = input.read(buffer);

        while (bytesRead >= 0) {
            output.write(buffer, 0, bytesRead);
            bytesRead = input.read(buffer);
        }

        input.close();

        output.close();
    }

    //===========================================================================
    private static void wait_(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(Backuper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void reload_backup_time() {
        Properties p = HelpM.properties_load_properties(PROPERTY_PATH);
        BACKUP_TIME = p.getProperty("backup_time", "00:00");
        p = null;
    }

    /**
     * The restart must be made to unblock the files used during the copy
     * process. The restart is done with help of "restarter.jar" a instance of
     * "RestarterSimpleAgentB"
     */
    private void restart() {
        SimpleLogger.logg(LOGGFILE, "Restarting program");
        run_java_app(APP_NAME, "_");
        System.exit(0);
    }

    private void run_java_app(String name, String arg) {
        wait_(200);
        String[] commands2 = {"java", "-jar", "restarter.jar", name, arg};
        try {
            Process p = Runtime.getRuntime().exec(commands2);
        } catch (IOException ex) {
            Logger.getLogger(Backuper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Shuts down the computer
     *
     * @tags shut_down_computer, shut down, shutdown,shut_down, turn of
     * computer,turn_off_computer, turn of
     */
    private static void shut_down_immediately() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec("shutdown -s -t 0");
        System.exit(0);
    }

    /**
     * Put computer into hibernation mode
     *
     * @tags sleep,hibernate,suspend
     * @throws IOException
     */
    public static void hibernate() throws IOException {
        Runtime.getRuntime().exec("Rundll32.exe powrprof.dll,SetSuspendState Sleep");
    }

    private static String get_proper_time_default_format(int style) {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = Calendar.getInstance(tz);
        DateFormat f1 = DateFormat.getTimeInstance(style);
        Date d = cal.getTime();
        return f1.format(d);
    }

    private static String get_proper_time_same_format_on_all_computers() {
        DateFormat formatter = new SimpleDateFormat("HH:mm");
        Calendar calendar = Calendar.getInstance();
        return formatter.format(calendar.getTime());
    }
}

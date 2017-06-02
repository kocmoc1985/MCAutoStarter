/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Moduls;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class FileCleaner implements Runnable {

    private Properties p;
    private int clean_db_each_x_days;
    private int number_files_not_erase_in_backup;
    private String sql_db_backup_path = "";
    private String monitor_sql_db_backup = "";
    private boolean THREAD_ON = true;
    private int CLEANER_NR;
    private final static String LOGFILE = "filecleaner.log";

    public FileCleaner(Properties p, int file_cleaner_nr) {
        this.p = p;
        this.CLEANER_NR = file_cleaner_nr;
        propertiesHandler();
    }

    private void propertiesHandler() {
        clean_db_each_x_days = Integer.parseInt(p.getProperty("clean_db_each_x_days_" + CLEANER_NR, "1"));
        number_files_not_erase_in_backup = Integer.parseInt(p.getProperty("number_files_not_erase_in_backup_" + CLEANER_NR, "200"));
        sql_db_backup_path = p.getProperty("sql_db_backup_path_" + CLEANER_NR, "");
        monitor_sql_db_backup = p.getProperty("monitor_sql_db_backup_" + CLEANER_NR, "");
        SimpleLogger.logg(LOGFILE, "x_days:" + clean_db_each_x_days
                + "\nnot_erase:" + number_files_not_erase_in_backup
                + "\nbackup_path:" + sql_db_backup_path
                + "\nmonitor:" + monitor_sql_db_backup);
    }

    @Override
    public void run() {

        while (THREAD_ON) {
            if (monitor_sql_db_backup.equals("false")) {
                break;
            }
            go();

            synchronized (this) {
                try {
                    wait(days_to_milliseconds_converter(clean_db_each_x_days));
                } catch (InterruptedException ex) {
                    Logger.getLogger(FileCleaner.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

    /**
     *
     */
    private void go() {
        int deleted = 0;
        File f = new File(sql_db_backup_path);

        if (f.exists() == false) {
            SimpleLogger.logg(LOGFILE, "sql backup dir not correct:" + sql_db_backup_path + ", cleaning not done!");
            THREAD_ON = false;
            return;
        }

        File[] flist = f.listFiles();

        ArrayList<FileD> fileList = new ArrayList<FileD>();

        for (int i = 0; i < flist.length; i++) {
            fileList.add(new FileD(flist[i]));
        }
        Collections.sort(fileList);

        for (int y = fileList.size() - 1; y >= number_files_not_erase_in_backup; y--) {
            fileList.get(y).getFile().delete();
            deleted++;
        }

        SimpleLogger.logg(LOGFILE, "files deleted = " + deleted);
    }

    /**
     * The inner class that handles the function that enables to compare files
     * aggainst their lastModify dates!
     */
    private class FileD implements Comparable {

        private File f;

        public FileD(File f) {
            this.f = f;
        }

        public File getFile() {
            return f;
        }

        @Override
        public int compareTo(Object o) {
            FileD x = (FileD) o;
            if (x.getFile().lastModified() < f.lastModified()) {
                return -1;
            } else if (x.getFile().lastModified() == f.lastModified()) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    /**
     *
     * @param minutes
     * @return
     */
    public static long days_to_milliseconds_converter(long days) {
        long minutes = 1440 * days;
        return minutes * 60000;
    }
}

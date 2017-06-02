/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Moduls;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NOTE! This class uses "jna.jar" library
 * @author KOCMOC
 */
public class WindowCloser implements Runnable {

    private String[] window_titles_to_be_closed;
    private boolean WINDOW_CLOSER_ON = true;
    private boolean RUN = true;
    private final static String LOG_FILE = "windowcloser.log";

    
    public WindowCloser(String[] window_titles, boolean on_off) {
        this.window_titles_to_be_closed = window_titles;
        this.WINDOW_CLOSER_ON = on_off;
        list_deprecated_windows();
        startThread();
    }

    private void startThread() {
        Thread x = new Thread(this);
        x.setName("WindowCloserHelper-Thr");
        x.start();
    }
    
    private void list_deprecated_windows(){
        SimpleLogger.logg(LOG_FILE, "Listing deprecated windows:");
        for (String string : window_titles_to_be_closed) {
            SimpleLogger.logg(LOG_FILE, string);
        }
    }

    private boolean go() {
        if (WINDOW_CLOSER_ON == false) {
            return false;
        }
        if (window_titles_to_be_closed == null) {
            return false;
        } else if (window_titles_to_be_closed.length == 0) {
            return false;
        }
        
        List<String> windows_currently_open = GetOpenedWindowsNative.getAllWindowNames();

        for (String curr_window_to_close : window_titles_to_be_closed) {
            for (String curr_open_window : windows_currently_open) {
                if (curr_open_window.contains(curr_window_to_close)) {
                    SimpleLogger.logg(LOG_FILE,"deprecated window detected, and will be closed: " + curr_open_window);
                    press_away_errormessages(10);
                }
            }
        }
        return true;
    }

    private void press_away_errormessages(int nr_attempts) {
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(PressAway.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (int i = 0; i < nr_attempts; i++) {
            robot.keyPress(KeyEvent.VK_ESCAPE);
            wait_(50);
            robot.keyRelease(KeyEvent.VK_ESCAPE);
            wait_(50);
        }
    }

    @Override
    public void run() {
        while (RUN) {
            wait_(10000);
            if (go() == false) {
                RUN = false;
            }
        }
        SimpleLogger.logg(LOG_FILE, "WindowCloser modul stopped running");
    }

    private synchronized void wait_(int millis) {
        try {
            wait(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(WindowCloser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

//==============================================================================
class GetOpenedWindowsNative {

    static interface User32 extends StdCallLibrary {

        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

        interface WNDENUMPROC extends StdCallCallback {
            boolean callback(Pointer hWnd, Pointer arg);
        }

        boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer userData);

        int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);

        Pointer GetWindow(Pointer hWnd, int uCmd);
    }

    public static List<String> getAllWindowNames() {
        final List<String> windowNames = new ArrayList<String>();
        final User32 user32 = User32.INSTANCE;
        user32.EnumWindows(new User32.WNDENUMPROC() {
            @Override
            public boolean callback(Pointer hWnd, Pointer arg) {
                byte[] windowText = new byte[512];
                user32.GetWindowTextA(hWnd, windowText, 512);
                String wText = Native.toString(windowText).trim();
                if (!wText.isEmpty()) {
                    windowNames.add(wText);
                }
                return true;
            }
        }, null);

        return windowNames;
    }
}

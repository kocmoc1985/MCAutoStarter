/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Moduls;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks weather another instance of the same program is running if it does the
 * program exits if not it continues
 *
 * @author Administrator
 */
public class OtherInstanceRunning implements Runnable {

    private Socket socket;//used!
    private ServerSocket serverSocket;//used!
    private int port;
    private final static String LOGFILE = "autostarter_other_instance_running_modul.log";
    private String CALLER_MODUL = "";

    public OtherInstanceRunning(int port, String caller_modul) {
        this.port = port;
        this.CALLER_MODUL = caller_modul;
    }

    @Override
    public void run() {
        go();
        while (true) {
            acceptConnections();
        }
    }

    private void go() {
        if (check_if_another_instance_of_program_is_running()) {
            SimpleLogger.logg(LOGFILE, "Another instance of program is running on port = " + port + ", the program will be closed");
            System.exit(0);
        } else {
            SimpleLogger.logg(LOGFILE, "No other instances of this program detected, the program will start.");
            run_server();
        }
    }

    private void run_server() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            SimpleLogger.logg(LOGFILE, "Server could not be run - " + ex.toString());
            Logger.getLogger(OtherInstanceRunning.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Very important, without this method the Server crashes after the first
     * connection attempt!!!
     */
    private void acceptConnections() {
        try {
            socket = serverSocket.accept();
            wait_(400);
            send("Modul " + CALLER_MODUL + " is running");
            socket = null;
        } catch (IOException ex) {
            Logger.getLogger(OtherInstanceRunning.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void send(String message) {
        try {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(message);
            output.flush(); // Måste göras!!!!
        } catch (IOException ex) {
            Logger.getLogger(OtherInstanceRunning.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return true if running
     */
    private boolean check_if_another_instance_of_program_is_running() {
        try {
            socket = new Socket("localhost", port);
            return true;
        } catch (UnknownHostException ex) {
            SimpleLogger.logg(LOGFILE, "[THIS IS NOT FAILURE! ]" + ex.toString());
//            Logger.getLogger(OtherInstanceRunning.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            SimpleLogger.logg(LOGFILE, "[THIS IS NOT FAILURE! ]" + ex.toString());
//            Logger.getLogger(OtherInstanceRunning.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }

    private void wait_(int millis) {
        synchronized (this) {
            try {
                wait(millis);
            } catch (InterruptedException ex) {
                Logger.getLogger(OtherInstanceRunning.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Moduls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class ClientLogger implements Runnable {

    private final int REFRESH_RATE_IN_MINUTES = 5;
    private String prev_record = "";
    private ArrayList<String> prev_list = new ArrayList<String>();
    private final static String LOGFILE = "clients.log";
    private String PORT_TO_TRACK = "";
    private final String PATH_TO_NETSTAT = "c:/windows/system32/netstat.exe";

    /**
     *
     * @param port_to_track - rdp usually uses 3389; vnc 5900
     */
    public ClientLogger(String port_to_track) {
        this.PORT_TO_TRACK = port_to_track;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ArrayList<String> list = check_hosts(PATH_TO_NETSTAT, PORT_TO_TRACK);
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        //pay attention to arraylist comparance which prevents from printing same data 
                        if (prev_record.equals(list.get(i)) == false && list.equals(prev_list) == false) {
                            SimpleLogger.logg(LOGFILE, "is online: " + list.get(i));
//                            loggEventUserMessage("ClientLogger-run()", list.get(i), "was online: ");
                            prev_record = list.get(i);
                        }
                    }
                    prev_list = list;
                }


                synchronized (this) {
                    try {
                        wait(60000 * REFRESH_RATE_IN_MINUTES);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ClientLogger.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(ClientLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * @tags grab_output, grab_out_put, grab output
     * @param pathToLogonssesionsApp
     * @param expressionToMatch
     * @return
     * @throws IOException
     */
    private static ArrayList<String> check_hosts(String pathToLogonssesionsApp, String expressionToMatch) throws IOException {
        ArrayList<String> host_list = new ArrayList<String>();
        try {
            String[] cmd = {pathToLogonssesionsApp, "-n"};//c:/windows/system32/netstat.exe
            String line;

            InputStream stdout = null;

            // launch EXE and grab stdin/stdout and stderr
            Process process = Runtime.getRuntime().exec(cmd);
            stdout = process.getInputStream();


            // clean up if any output in stdout
            BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(stdout));
            while ((line = brCleanUp.readLine()) != null) {
                if (line.toLowerCase().contains(expressionToMatch.toLowerCase())) {//checkStringContains(line.toLowerCase(), expressionToMatch.toLowerCase())
                    String temp = line.substring(29, 47);
                    temp = temp.replaceAll(" ", "");
                    temp = temp.replaceAll(":", "");
//                    System.out.println("" + translateAddrToHost(temp));
                    host_list.add(line + " <> " + translateAddrToHost(temp));
                }
//                System.out.println("[Stdout] " + line);
            }
            brCleanUp.close();
        } catch (Throwable ex) {
            return null;
        }
        return host_list;
    }

    private static String translateAddrToHost(String ip) {
        String host_name = "";
        try {
            // Get hostname by textual representation of IP address
            InetAddress addr = InetAddress.getByName(ip);
            host_name = addr.getCanonicalHostName();
        } catch (UnknownHostException ex) {
//            Logger.getLogger(HelpM.class.getName()).log(Level.SEVERE, null, ex);
        }
        return host_name;

    }
//    public static void main(String[] args) {
//        new Thread(new ClientLogger("443")).start();
//    }
}

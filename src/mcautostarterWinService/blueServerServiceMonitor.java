/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcautostarterWinService;

import Moduls.ServiceRunning;

/**
 *
 * @author Administrator
 */
public class blueServerServiceMonitor {

    public static void main(String[] args) {
        new Thread(new ServiceRunning("browser", 10000)).start();
        new Thread(new ServiceRunning("lanmanserver", 10000)).start();
    }
}

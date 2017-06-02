/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Interfaces;

/**
 *
 * @author Administrator
 */
public interface Tray {

    public void displayTrayWarningMessage(String title, String msg);

    public void displayTrayErrorMessage(String title, String msg);

    public void displayTrayInfoMessage(String title, String msg);
}

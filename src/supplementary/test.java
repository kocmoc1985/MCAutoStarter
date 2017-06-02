/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package supplementary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class test {

    public static void main(String[] args) {
//        try {
//            copyFile("z:/program files/microsoft sql server/mssql/data/mixcont_data.mdf", "c:/test.mdf");
//        } catch (IOException ex) {
//            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
//        }

//        try {
//            copy_file("z:/program files/microsoft sql server/mssql/data/mixcont_data.mdf", "c:/test.mdf");
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
//        }

        Copier nio = new NioCopier();
        try {
            nio.copy(new File("z:/program files/microsoft sql server/mssql/data/mixcont_data.mdf"), new File("c:/test.mdf"));
        } catch (IOException ex) {
            Logger.getLogger(test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void copy_file(String file_to_copy, String name_of_duplicate) throws FileNotFoundException, IOException {
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

    public static void copyFile(String file_to_copy, String duplicate_file_name) throws IOException {
        byte[] b_arr = filetoByteArray(file_to_copy);
        byteArrayToFile(duplicate_file_name, b_arr);
    }

    public static byte[] filetoByteArray(String path) throws FileNotFoundException, IOException {
        byte[] content;
        FileInputStream p = new FileInputStream(path);
        content = new byte[p.available()];
        p.read(content);
        p.close();
        return content;
    }

    public static void byteArrayToFile(String path, byte[] arr) throws FileNotFoundException, IOException {
        File f2 = new File(path);
        OutputStream out;
        out = new FileOutputStream(f2);
        out.write(arr);
    }
    //========================================================================

    interface Copier {

        public void copy(File s, File t) throws IOException;
    }

    static class NioCopier implements Copier {

        @Override
        public void copy(File s, File t) throws IOException {
            FileChannel in = (new FileInputStream(s)).getChannel();
            FileChannel out = (new FileOutputStream(t)).getChannel();
            in.transferTo(0, s.length(), out);
            in.close();
            out.close();
        }
    }

    static class IoCopier implements Copier {

        final int BUFF_SIZE = 5 * 1024 * 1024; // 5MB
        final byte[] buffer = new byte[BUFF_SIZE];

        @Override
        public void copy(File s, File t) throws IOException {
            InputStream in = new FileInputStream(s);
            FileOutputStream out = new FileOutputStream(t);

            while (true) {
                int count = in.read(buffer);
                if (count == -1) {
                    break;
                }
                out.write(buffer, 0, count);
            }
            out.close();
            in.close();
        }
    }
}

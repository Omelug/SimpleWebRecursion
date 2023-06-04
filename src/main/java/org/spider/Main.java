package org.spider;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    static List<String> keKontrole = new ArrayList<>();
    static final String WEB = "https://web2.mlp.cz/koweb/00/04/"; // in the end should be "/"
    static final String FOLDER = "data/koweb/00/04/"; // in the end should be "/"
    static boolean DEBUG = false;
    public static void main(String[] args) {

        String html = readURLContent(WEB);
        //System.out.println(html);
    }
    private static String readURLContent(String url) {
        //System.out.println("Projizdim: "+url);
        try {
            Scanner sc = new Scanner(new URL(url).openStream(), StandardCharsets.UTF_8);
            String retezec = "";
            while (sc.hasNextLine()) {
                String newurl = url +sc.nextLine();
                keKontrole.add(newurl);

                if(newurl.endsWith("\\")){
                    newurl = newurl.substring(0, newurl.length() - 1) + "/";
                    //System.out.println("Nova slozka:" + newurl);
                    readURLContent(newurl);
                }else{
                    String[] extensions = {".txt", ".doc",".epub", ".pdf", ".zip", ".docx", ".xls", ".mp3",".avi",".prc",".html"};
                    if (checkIfFileHasExtension(newurl, extensions)) {
                        System.out.println("         "+newurl);
                        File file = new File(FOLDER+ newurl.substring(WEB.length()));
                        if (file.exists()) {
                            System.out.println("Destination file already exists. Skipping download.");
                        } else {
                            try {
                                FileUtils.copyURLToFile(
                                        new URL(newurl),
                                        file,
                                        20000,
                                        20000);
                            } catch (IOException e) {
                                System.err.println("Problem pri stahovani: " + e.getMessage());
                            }
                        }
                    }
                }

                //retezec = retezec + sc.nextLine() + "\n";
            }
            sc.close();
            return retezec;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    public static boolean checkIfFileHasExtension(String s, String[] extn) {
        return Arrays.stream(extn).anyMatch(entry -> s.endsWith(entry));
    }

}
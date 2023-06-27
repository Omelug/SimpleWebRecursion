package org.spider;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainIndexOfHtml {
    static final String WEB = "http://www.elsat.cz/panek/"; // in the end should be "/"
    static final String FOLDER = "data/gmct/"; // in the end should be "/"
    static final String TREE = "tree/"; // in the end should be "/"
    static boolean DEBUG = false;
    static List<Boolean> vrstvy = new ArrayList<>();
    static String html = TREE + "tree.html";
    static BufferedWriter writer = null;
    public static void main(String[] args) {

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        try {
            File file = new File(html);

            if(file.exists()){
                file.delete();
            }

            File parentDirectory = file.getParentFile();

            //this creates folders for tree file
            if (parentDirectory != null) {
                if (!parentDirectory.exists()) {
                    parentDirectory.mkdirs();
                }
            }

            if (file.createNewFile()) {
                System.out.println("File created: " + html);
            } else {
                System.out.println("Failed to create the file.");
                return;
            }

            writer = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            writeLineHtml(WEB, WEB);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        readURLContent(WEB, 0);
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static String readURLContent(String url, int vrstva) {
        if (vrstvy.size() == vrstva){
            vrstvy.add(true);
        }
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a");
            String retezec = "";
            Integer pocetLinku =links.size();
            if (pocetLinku >= 1) {
                vrstvy.set(vrstva, true);
            }
            for (int i = 0; i < pocetLinku; i++) {
                Element link = links.get(i);
                String linkUrl = link.attr("href");
                if (linkUrl.startsWith("?") || linkUrl.startsWith("/")) {
                    continue;
                }
                String line = "";
                for (int i2 = 0; i2 < vrstva; i2++) {
                    if (vrstvy.get(i2)){
                        line = line + "│    ";
                    }else {
                        line = line + "    ";
                    }

                }
                String newurl = url + linkUrl;
                if (1 == (pocetLinku-i)){
                    writeLineHtml(line +"└── " +linkUrl, newurl);
                    vrstvy.set(vrstva, false);
                }else {
                    writeLineHtml(line +"├── " +linkUrl, newurl);
                }

                //System.out.println(newurl);
                if(newurl.endsWith("/")){
                    //System.out.println("New folder: " + newurl);
                    readURLContent(newurl, vrstva+1);
                }else{
                    /*String[] extensions = {".txt", ".doc",".epub", ".pdf", ".zip", ".rar", ".docx", ".xls", ".mp3",".mp4",".avi",".prc",".html"};
                    if (checkIfFileHasExtension(newurl, extensions)) {
                        //System.out.println("         "+newurl);
                        File file = new File(FOLDER+ newurl.substring(WEB.length()));
                        if (file.exists()) {
                            //System.out.println("Destination file already exists. Skipping download.");
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
                    }*/
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public static boolean checkIfFileHasExtension(String s, String[] extn) {
        return Arrays.stream(extn).anyMatch(entry -> s.endsWith(entry));
    }
    public static void writeLine(String string) throws IOException {
        System.out.println(string);
        writer.write(string);
        writer.newLine();
    }
    public static void writeLineHtml(String string, String newurl) throws IOException {
        System.out.println(string);
        String line = "<pre style=\"margin: 0px\"><a style=\"text-decoration: none\" href=" + newurl+ "> "+string+"  </a></pre>";
        writer.write(line);
        writer.newLine();
    }


}
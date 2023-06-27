package org.spider;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MainIndexOf {
    static final String WEB = "https://www.ucenibezucebnic.cz/images/ep/metody/prakticke-zaloha/"; // in the end should be "/"
    static final String FOLDER = "data/ucenibezucebnic/"; // in the end should be "/"
    static boolean DEBUG = false;
    static List<Boolean> vrstvy = new ArrayList<>();

    public static void main(String[] args) {

        //for disable validation control
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
        System.out.println(WEB);
        readURLContent(WEB, 0);
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
                if (1 == (pocetLinku-i)){
                    System.out.println(line +"└── " +linkUrl);
                    vrstvy.set(vrstva, false);
                }else {
                    System.out.println(line +"├── " +linkUrl);
                }

                String newurl = url + linkUrl;
                //System.out.println(newurl);
                if(newurl.endsWith("/")){
                    //System.out.println("New folder: " + newurl);
                    readURLContent(newurl, vrstva+1);
                }else{
                    String[] extensions = {".txt", ".doc",".epub", ".pdf", ".zip", ".rar", ".docx", ".xls", ".mp3",".mp4",".avi",".prc",".html"};
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
                    }
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

}
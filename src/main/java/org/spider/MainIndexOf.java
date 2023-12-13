package org.spider;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.spider.HelpObjects.LoggerHelper;

import javax.net.ssl.*;
import javax.swing.text.html.HTML;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainIndexOf {
    //CHANGE THIS
    static final String WEB = "https://www.umat.fekt.vut.cz/~hlinena/IDM/"; // in the end should be "/"
    static final String NAME = "IDM";
    static String[] extensions = {".txt", ".doc",".epub", ".pdf", ".zip", ".rar", ".docx", ".xls", ".mp3",".mp4",".avi",".prc",".html"};
    static TREE_TYPE tree_type = TREE_TYPE.HTML;

    //You can change this tooo, but only if you know what you're doing
    static final String FOLDER = "data/"+NAME+"/"; // in the end should be "/"
    static final String TREE = "tree/"; // in the end should be "/"
    static List<Boolean> layouts = new ArrayList<>();
    static String tree = TREE + NAME + "_tree"+ tree_type.extension;
    static BufferedWriter writer = null;
    static boolean VERBOSE = true;
    enum TREE_TYPE{TEXT(".txt"), HTML(".html");
        String extension;
        TREE_TYPE(String s) {
            extension = s;
        }
    }
    static LoggerHelper log = new LoggerHelper(true);

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
            File file = new File(tree);
            if (file.exists()) {
                log.info( "Tree already exist, skipping" + file.getAbsolutePath());
            }
            File parentDirectory = file.getParentFile();

            if (parentDirectory != null) {
                if (!parentDirectory.exists()) {
                    parentDirectory.mkdirs();
                }
            }

            if (!file.exists()) {
                if (file.createNewFile()) {
                    System.out.println("File created: " + tree);
                } else {
                    System.out.println("Failed to create the file.");
                    return;
                }
            }
            writer = new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(WEB);
        readURLContent(WEB, 0);
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static String readURLContent(String url, int layout) {
        if (layouts.size() == layout){
            layouts.add(true);
        }
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a");

            int linkCount =links.size();
            if (linkCount >= 1) {
                layouts.set(layout, true);
            }
            for (int i = 0; i < linkCount; i++) {
                Element link = links.get(i);
                String fileName = link.attr("href");
                if (fileName.startsWith("?") || fileName.startsWith("/")) {
                    continue;
                }
                String line = "";
                for (int i2 = 0; i2 < layout; i2++) {
                    if (layouts.get(i2)){
                        line = line + "│    ";
                    }else {
                        line = line + "    ";
                    }

                }
                String newurl = url + fileName;

                if (1 == (linkCount-i)){
                    writeLine(line +"└── ",fileName, newurl, tree_type);
                    layouts.set(layout, false);
                }else {
                    writeLine(line +"├── ",fileName, newurl, tree_type);
                }


                if(newurl.endsWith("/")){
                    readURLContent(newurl, layout+1);
                }else{
                    if (checkIfFileHasExtension(newurl, extensions)) {
                        File file = new File(FOLDER+ newurl.substring(WEB.length()));
                        if (file.exists()) {
                            System.out.println( file.getName()+ " already exists. Skipping.");
                        } else {
                            try {
                                FileUtils.copyURLToFile(
                                        new URL(newurl),
                                        file,
                                        20000,
                                        20000);
                            } catch (IOException e) {
                                System.err.println("Download issue: " + e.getMessage());
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
    public static boolean checkIfFileHasExtension(String s, String[] extension) {
        return Arrays.stream(extension).anyMatch(s::endsWith);
    }
    public static void writeLine(String string,String name, String newURL, TREE_TYPE tree_type) throws IOException {
        if (VERBOSE) log.info(string);
        if (tree_type == TREE_TYPE.HTML){
            writer.write("<pre style=\"margin: 0px\"><a style=\"text-decoration: none\" href="+ newURL+ "> "+string + name +"  </a></pre>");
        }else if (tree_type == TREE_TYPE.TEXT){
            writer.write(string + name);
        }
        writer.newLine();
    }


}
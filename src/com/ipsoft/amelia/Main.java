package com.ipsoft.amelia;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {
        //System.out.println("args = " + args);
        Map<String, HashSet<String>> helperMap = new HashMap<>();

        Pattern categoryMatcher = Pattern.compile("(?:#)(.*)(?:#)");

        String path = System.getProperty("user.dir") + "/sentence.txt";
        FileInputStream fis = new FileInputStream(path);
        BufferedReader in = new BufferedReader(new InputStreamReader(fis));

        String line;
        while ((line = in.readLine()) != null) {
            Matcher m = categoryMatcher.matcher(line);
            if (m.matches()) {
                helperMap.put(m.group(1), new HashSet<String>());
                while ((line = in.readLine()) != null) {
                    if (line == null || line.isEmpty() || line.trim().equalsIgnoreCase("")) {
                        break;
                    }
                    else {
                        HashSet<String> update = helperMap.get(m.group(1));
                        update.add(line);
                        helperMap.put(m.group(1), update);
                    }
                }
            }
        }
        in.close();


        List<String> lines = new ArrayList<>();
        HashSet<String> sentences = helperMap.get("SENTENCE");
        for (String sentence: sentences) {
            HashSet<String> expandedList = new HashSet<>();
            expandedList = expandSentence (sentence, expandedList);
            lines.addAll (expandedList);
        }
        //lines.addAll(helperMap.get("SENTENCE"));

        byte[] tmp = writeToFileAndRead (lines);

        final StringBuffer[] old = {new StringBuffer(new String(tmp, StandardCharsets.UTF_8))};
        StringBuffer output = new StringBuffer();

        helperMap.forEach((key, value) -> {
            if ( !key.equalsIgnoreCase("SENTENCE")) {
                HashSet<String> valMap = helperMap.get(key);
                valMap.forEach((k) -> {
                    output.append(old[0].toString().replaceAll(key, k));
                });
                old[0] = new StringBuffer(output);
                output.delete(0, output.length());
            }
        });

        Set<String> unique = new HashSet<>();
        String allines[] = old[0].toString().split("\\n");
        for(String aline: allines) {
            unique.add(aline);
        }

        List<String> thefile = new ArrayList<>();
        thefile.addAll(unique);
        Collections.sort(thefile);
        writeToFileAndRead(thefile);

        System.out.println("Output file named output.txt generated in this directory with possible sentences...");
    }

    public static byte[] writeToFileAndRead (List<String> lines) {
        Path file = Paths.get("output.txt");
        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
            byte[] tmp = Files.readAllBytes(file);
            return tmp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HashSet<String> expandSentence (String sentence, HashSet<String> expandedList) {
        Pattern expanderMatcher = Pattern.compile("(\\*\\w* *)");
        Matcher m = expanderMatcher.matcher(sentence);
        if (m.find()) {
            String match = m.group(1);
            String resultWithout = m.replaceFirst("");
            expandSentence (resultWithout, expandedList);
            String resultWith = m.replaceFirst(match.substring(1));
            expandSentence (resultWith, expandedList);
        }
        else{
            expandedList.add(sentence);
        }
        return expandedList;
    }
}

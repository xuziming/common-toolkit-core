package com.simon.credit.toolkit.lang;

import com.simon.credit.toolkit.io.IOToolkits;

import java.io.InputStreamReader;
import java.io.LineNumberReader;

public class LinuxCmdExecutor {

    public static final String exec(String cmd) {
        LineNumberReader reader = null;
        try {
            String[] cmdArray = {"/bin/sh", "-c", cmd};
            Process process = Runtime.getRuntime().exec(cmdArray);
            reader = new LineNumberReader(new InputStreamReader(process.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                buffer.append(line).append("\n");
            }
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOToolkits.close(reader);
        }
        return null;
    }

}
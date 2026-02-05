package io.papermc.paper;

import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);

            // 查看 dashboard 帮助
            System.out.println("=== OpenClaw dashboard --help ===");
            ProcessBuilder pb1 = new ProcessBuilder(nodeBin, ocBin, "dashboard", "--help");
            pb1.environment().putAll(env);
            pb1.inheritIO();
            pb1.start().waitFor();

            System.out.println("\n=== OpenClaw gateway --help ===");
            ProcessBuilder pb2 = new ProcessBuilder(nodeBin, ocBin, "gateway", "--help");
            pb2.environment().putAll(env);
            pb2.inheritIO();
            pb2.start().waitFor();

        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
}

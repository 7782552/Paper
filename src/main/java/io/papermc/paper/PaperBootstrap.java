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

            // 查看 onboard 帮助
            System.out.println("========================================");
            System.out.println("=== OpenClaw onboard --help ===");
            System.out.println("========================================");
            
            ProcessBuilder pb = new ProcessBuilder(nodeBin, ocBin, "onboard", "--help");
            pb.environment().putAll(env);
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) { 
            e.printStackTrace();
        }
    }
}

package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🎣 [OpenClaw] 正在执行“报错钓鱼”法，请观察下方 Problem 提示...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            // 写入一个必然报错但能触发校验器的 JSON
            // 我们故意把 agents 写成 agent，把 channels 写成 channel
            // 目的是让它的 Doctor 告诉我们正确答案
            String fishJson = "{\n" +
                "  \"agent\": {},\n" +
                "  \"channel\": {}\n" +
                "}";

            File configDir = new File(baseDir, ".openclaw");
            if (!configDir.exists()) configDir.mkdirs();
            Files.write(Paths.get(baseDir, ".openclaw/openclaw.json"), fishJson.getBytes());

            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            pb.environment().put("HOME", baseDir);
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

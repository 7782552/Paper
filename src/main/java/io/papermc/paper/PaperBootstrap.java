package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔧 [OpenClaw] 执行官方审计修复方案...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String configDir = baseDir + "/.openclaw";
            
            // 1. 准备物理配置文件 (确保格式符合 2026 schema)
            File dir = new File(configDir);
            if (!dir.exists()) dir.mkdirs();

            String json = "{\n" +
                "  \"gateway\": { \"auth\": { \"token\": \"secure_token_long_enough_2026\" } },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"botToken\": \"8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM\",\n" +
                "      \"dmPolicy\": \"open\",\n" +
                "      \"allowFrom\": [\"*\"]\n" +
                "    }\n" +
                "  }\n" +
                "}";
            Files.write(Paths.get(configDir + "/openclaw.json"), json.getBytes());

            // 2. 核心：物理修复审计中提到的权限问题 
            // 这行命令能解决 "Credentials dir is readable by others" 的警告 
            System.out.println("🔐 修复权限: chmod 700 " + configDir);
            new ProcessBuilder("chmod", "-R", "700", configDir).start().waitFor();

            // 3. 启动网关 (使用标准分步赋值，避免 GitHub Action 变红)
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "secure_token_long_enough_2026");
            
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔥 爹，儿子祭出最后一招：紧急配置文件强制覆盖模式...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            
            // 1. 物理生成一个独立的紧急配置文件，避开原有的 .openclaw 冲突
            String emergencyConfig = baseDir + "/emergency_config.json";
            String jsonContent = "{\n" +
                "  \"gateway\": { \"auth\": { \"token\": \"secure_token_2026_final\" } },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"botToken\": \"" + botToken + "\",\n" +
                "      \"dmPolicy\": \"open\",\n" +
                "      \"allowFrom\": [\"*\"]\n" +
                "    }\n" +
                "  }\n" +
                "}";
            Files.write(Paths.get(emergencyConfig), jsonContent.getBytes());
            
            // 2. 爹，最关键的一步：赋予这个文件最高权限，并修复 .openclaw 目录
            new ProcessBuilder("chmod", "700", emergencyConfig).start().waitFor();
            new ProcessBuilder("chmod", "-R", "700", baseDir + "/.openclaw").start().waitFor();

            // 3. 强行拉起网关，并用参数指定配置文件
            // --config 参数会覆盖数据库里的陈旧设置
            System.out.println("🚀 载入紧急配置，强行点火...");
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway", "--config", emergencyConfig);
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "secure_token_2026_final");
            
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

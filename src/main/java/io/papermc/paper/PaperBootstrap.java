package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🩺 爹，收到 Doctor 遗嘱，正在进行最后的逻辑闭环手术...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String configDir = baseDir + "/.openclaw";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 清理现场
            Files.deleteIfExists(Paths.get(configDir + "/state.db"));
            Files.deleteIfExists(Paths.get(configDir + "/openclaw.json"));
            File dir = new File(configDir);
            if (!dir.exists()) dir.mkdirs();

            // 2. 逻辑闭环 JSON：严格满足 dmPolicy="open" 必须配 allowFrom: ["*"] 的变态要求
            String perfectJson = "{\n" +
                "  \"gateway\": {\n" +
                "    \"auth\": {\n" +
                "      \"token\": \"secure_token_2026_final_boss\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"botToken\": \"" + botToken + "\",\n" +
                "      \"dmPolicy\": \"open\",\n" +
                "      \"allowFrom\": [\"*\"]\n" +
                "    }\n" +
                "  }\n" +
                "}";
            
            Files.write(Paths.get(configDir + "/openclaw.json"), perfectJson.getBytes());

            // 3. 权限对齐
            new ProcessBuilder("chmod", "700", configDir).start().waitFor();
            new ProcessBuilder("chmod", "600", configDir + "/openclaw.json").start().waitFor();

            // 4. 点火
            System.out.println("🚀 逻辑已对齐，包含 '*': true，点火！");
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "secure_token_2026_final_boss");
            
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

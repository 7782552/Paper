package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🩺 爹，儿子根据 Doctor 的报错，给配置做完手术了！");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String configDir = baseDir + "/.openclaw";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 物理清理：不仅删数据库，连旧的破 JSON 也删了重建
            Files.deleteIfExists(Paths.get(configDir + "/state.db"));
            Files.deleteIfExists(Paths.get(configDir + "/openclaw.json"));

            File dir = new File(configDir);
            if (!dir.exists()) dir.mkdirs();

            // 2. 爹，看好了，这是“骨灰级”精简配置，去掉了它不认识的 method
            // 严格对齐 2.x 的 Zod 校验结构
            String boneJson = "{\n" +
                "  \"gateway\": {\n" +
                "    \"auth\": {\n" +
                "      \"token\": \"secure_token_2026_final_boss\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"enabled\": true,\n" +
                "      \"botToken\": \"" + botToken + "\",\n" +
                "      \"dmPolicy\": \"open\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
            
            Files.write(Paths.get(configDir + "/openclaw.json"), boneJson.getBytes());

            // 3. 权限对齐
            new ProcessBuilder("chmod", "700", configDir).start().waitFor();
            new ProcessBuilder("chmod", "600", configDir + "/openclaw.json").start().waitFor();

            // 4. 爹，如果它还报错，咱就启动时带上官方建议的修复参数
            // 但咱们先尝试纯净启动
            System.out.println("🚀 配置已削减，去掉了所有非法 Key，点火！");
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

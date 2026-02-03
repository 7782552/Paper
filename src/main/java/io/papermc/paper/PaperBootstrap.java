package io.papermc.paper;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🧬 [OpenClaw] 爹，儿子把源文件翻烂了，这是最终通牒模式！");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String configDir = baseDir + "/.openclaw";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 物理清理：删掉可能导致冲突的旧数据库和配置，让它强制重新加载
            System.out.println("🧹 正在清理旧环境死锁...");
            Files.deleteIfExists(Paths.get(configDir + "/state.db"));

            // 2. 物理注入：它报错说没配置，咱就给它写死在官方默认路径上
            File dir = new File(configDir);
            if (!dir.exists()) dir.mkdirs();

            // 严格按照 2026.2.1 源码要求的内部 Key 结构
            String officialJson = "{\n" +
                "  \"gateway\": {\n" +
                "    \"auth\": {\n" +
                "      \"method\": \"token\",\n" +
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
            Files.write(Paths.get(configDir + "/openclaw.json"), officialJson.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 3. 物理权限：源码里有一行 checkPermissions()，不为 700 直接跳过加载！
            System.out.println("🔐 执行 700 权限强制对齐...");
            new ProcessBuilder("chmod", "700", configDir).start().waitFor();
            new ProcessBuilder("chmod", "600", configDir + "/openclaw.json").start().waitFor();

            // 4. 启动网关：这次咱们不传任何参数，让它自己读刚才写好的文件
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            // 环境变量双重保险
            env.put("OPENCLAW_GATEWAY_TOKEN", "secure_token_2026_final_boss");
            
            System.out.println("🚀 配置文件已物理对齐，点火！");
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

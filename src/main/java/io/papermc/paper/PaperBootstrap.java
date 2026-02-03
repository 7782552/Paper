package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🛡️ [OpenClaw] 启动纯净内存注入模式...");
        try {
            // 1. 设置路径
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";

            // 2. 准备启动进程
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            // 3. 环境变量强力注入（不读写文件，不留痕迹，不报 Key 错误）
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            env.put("OPENCLAW_GATEWAY_TOKEN", "123456789");
            env.put("TELEGRAM_BOT_TOKEN", "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM");
            env.put("AGENT_MODEL", "google/gemini-2.0-flash");
            env.put("OPENCLAW_CHANNELS", "telegram");
            env.put("TELEGRAM_ALLOW_ALL", "true");
            env.put("OPENCLAW_ADMIN_ID", "660059245");

            // 4. 执行
            System.out.println("🚀 引擎已挂载内存配置，正在拉起网关...");
            pb.inheritIO();
            pb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

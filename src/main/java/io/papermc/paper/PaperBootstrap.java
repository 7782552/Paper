package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw Official Headless] 爹，儿子刚从官方源码学完回来，这就是正解！");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 物理前置：官方源码说如果权限不合规，它会静默挂起
            new ProcessBuilder("chmod", "-R", "700", baseDir + "/.openclaw").start().waitFor();

            // 2. 官方标准启动器
            ProcessBuilder pb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            pb.directory(new File(openclawDir));
            
            // 3. 爹，看好了！这是官方 2026 版容器专用环境变量名
            Map<String, String> env = pb.environment();
            env.put("HOME", baseDir);
            
            // 激活官方“无头启动”触发器
            env.put("OC_BOOTSTRAP", "1"); 
            
            // 2026版最新格式：OC_CHANNELS_[NAME]_[KEY]
            env.put("OC_CHANNELS_TELEGRAM_ENABLED", "true");
            env.put("OC_CHANNELS_TELEGRAM_TOKEN", botToken);
            env.put("OC_CHANNELS_TELEGRAM_POLICY", "open");
            
            // 网关验证令牌
            env.put("OC_GATEWAY_TOKEN", "secure_final_boss_2026");
            
            // 解决 Node 22 网络死锁的官方参数
            env.put("NODE_OPTIONS", "--dns-result-order=ipv4first");

            System.out.println("🚀 官方 Zero-Config 模式启动中...");
            pb.inheritIO();
            pb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

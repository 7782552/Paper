package io.papermc.paper;
import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🛠️ [OpenClaw] 正在修复 Java 语法错误并注入凭据...");
        try {
            String baseDir = "/home/container";
            String openclawDir = baseDir + "/openclaw";
            String nodePath = baseDir + "/node-v22.12.0-linux-x64/bin/node";
            String botToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            // 1. 预设配置项
            String[][] configs = {
                {"channels.telegram.enabled", "true"},
                {"channels.telegram.botToken", botToken},
                {"channels.telegram.dmPolicy", "open"},
                {"channels.telegram.allowFrom", "*"}
            };

            // 2. 执行 config set 循环
            for (String[] config : configs) {
                System.out.println("💾 Setting " + config[0] + "...");
                ProcessBuilder configPb = new ProcessBuilder(nodePath, "dist/index.js", "config", "set", config[0], config[1]);
                configPb.directory(new File(openclawDir));
                // 修正点：分开设置环境变量，不要写在链式调用里
                configPb.environment().put("HOME", baseDir); 
                configPb.inheritIO();
                
                Process p = configPb.start();
                p.waitFor();
            }

            // 3. 启动网关
            System.out.println("🚀 凭据注入完成，正在拉起网关...");
            ProcessBuilder gatewayPb = new ProcessBuilder(nodePath, "dist/index.js", "gateway");
            gatewayPb.directory(new File(openclawDir));
            gatewayPb.environment().put("HOME", baseDir);
            gatewayPb.environment().put("OPENCLAW_GATEWAY_TOKEN", "123456789");
            gatewayPb.inheritIO();
            
            gatewayPb.start().waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

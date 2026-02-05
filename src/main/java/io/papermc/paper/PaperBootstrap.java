package io.papermc.paper;

import java.io.*;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22/bin";
        String nodeBin = nodeBinDir + "/node";
        String n8nBin = baseDir + "/node_modules/n8n/bin/n8n";
        
        try {
            System.out.println("🛠️ [Final-Config] 正在解决安全 Cookie 限制并绑定域名...");

            File n8nFile = new File(n8nBin);
            if (n8nFile.exists()) {
                // 4G 内存深度优化
                ProcessBuilder pb = new ProcessBuilder(nodeBin, "--max-old-space-size=3072", n8nBin, "start");
                pb.directory(new File(baseDir));
                
                Map<String, String> env = pb.environment();
                env.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                
                // --- 核心网络与域名配置 ---
                env.put("N8N_PORT", "30196");
                env.put("N8N_HOST", "0.0.0.0");
                
                // 关键修复：关闭安全 Cookie 限制，允许从 http://node.zenix.sg 登录
                env.put("N8N_SECURE_COOKIE", "false"); 
                
                // 域名绑定 (指向你的主域名)
                env.put("N8N_PROTOCOL", "https");
                env.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
                env.put("N8N_EDITOR_BASE_URL", "https://8.8855.cc.cd/");

                // 数据库优化
                env.put("DB_TYPE", "sqlite");
                env.put("N8N_METRICS", "true");

                pb.inheritIO().start();
                
                System.out.println("✅ 配置已更新！");
                System.out.println("📢 现在你可以任选一个地址进入了：");
                System.out.println("1️⃣ 稳定域名：https://8.8855.cc.cd");
                System.out.println("2️⃣ 调试地址：http://node.zenix.sg:30196");
            } else {
                System.out.println("❌ 找不到 n8n 文件，请检查路径。");
            }

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

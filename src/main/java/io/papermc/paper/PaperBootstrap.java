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
            System.out.println("🚀 [System-Ready] 正在以 4GB 内存优化模式拉起 n8n...");

            File n8nFile = new File(n8nBin);
            if (n8nFile.exists()) {
                // 分配 3GB 内存给 Node.js 确保大型工作流不崩溃
                ProcessBuilder pb = new ProcessBuilder(nodeBin, "--max-old-space-size=3072", n8nBin, "start");
                pb.directory(new File(baseDir));
                
                Map<String, String> n8nEnv = pb.environment();
                n8nEnv.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                
                // --- 网络与域名配置 ---
                n8nEnv.put("N8N_PORT", "30196");
                n8nEnv.put("N8N_HOST", "0.0.0.0");
                n8nEnv.put("N8N_PROTOCOL", "https");
                n8nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
                n8nEnv.put("N8N_EDITOR_BASE_URL", "https://8.8855.cc.cd/");
                
                // --- 兼容性修复 ---
                n8nEnv.put("N8N_SECURE_COOKIE", "false"); 
                n8nEnv.put("N8N_PROXY_HOPS", "1"); // 信任 Cloudflare 代理
                
                pb.inheritIO().start();
                
                System.out.println("✅ n8n 已成功启动！");
                System.out.println("🔗 访问地址: https://8.8855.cc.cd");
            } else {
                System.out.println("❌ 错误：未找到 n8n 运行文件，请检查安装目录。");
            }

            // 保持进程运行
            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

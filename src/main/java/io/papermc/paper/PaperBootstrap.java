package io.papermc.paper;

import java.io.*;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22/bin";
        String nodeBin = nodeBinDir + "/node";
        String n8nBin = baseDir + "/node_modules/n8n/bin/n8n";
        
        // 【关键】请把这里换成你面板显示的那个原始长域名（例如 node.zenix.sg）
        String originalDomain = "node.zenix.sg"; 

        try {
            System.out.println("🚀 [Domain-Fix] 正在以原始域名重新拉起 n8n...");

            File n8nFile = new File(n8nBin);
            if (n8nFile.exists()) {
                ProcessBuilder pb = new ProcessBuilder(nodeBin, n8nBin, "start");
                pb.directory(new File(baseDir));
                
                Map<String, String> env = pb.environment();
                env.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                
                // 核心环境变量修复
                env.put("N8N_PORT", "30196");
                env.put("N8N_HOST", "0.0.0.0"); // 允许外部访问
                env.put("N8N_LISTEN_ADDRESS", "0.0.0.0");
                
                // 域名相关设置
                env.put("N8N_EDITOR_BASE_URL", "https://" + originalDomain + ":30196/");
                env.put("WEBHOOK_URL", "https://" + originalDomain + ":30196/");
                
                pb.inheritIO().start();
                
                System.out.println("✨ 启动指令已发出！");
                System.out.println("🔗 请尝试通过以下两个地址访问：");
                System.out.println("1. https://" + originalDomain + ":30196");
                System.out.println("2. https://8.8855.cc.cd");
            } else {
                System.out.println("❌ 找不到 n8n 执行文件！");
            }

            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.out.println("❌ 启动失败: " + e.getMessage());
        }
    }
}

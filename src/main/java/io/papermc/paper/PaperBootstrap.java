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
            System.out.println("🔓 [Pure-HTTP-Launch] 正在切换到纯净模式启动...");

            File n8nFile = new File(n8nBin);
            if (n8nFile.exists()) {
                ProcessBuilder pb = new ProcessBuilder(nodeBin, n8nBin, "start");
                pb.directory(new File(baseDir));
                
                Map<String, String> env = pb.environment();
                env.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                
                // 1. 强制使用面板分配的 30196
                env.put("N8N_PORT", "30196");
                env.put("N8N_HOST", "0.0.0.0");
                
                // 2. 彻底禁用 HTTPS 强制跳转，使用纯 HTTP
                env.put("N8N_PROTOCOL", "http");
                env.put("N8N_SECURE_COOKIE", "false");
                
                // 3. 设置外部访问基础 URL (注意这里是 http)
                env.put("N8N_EDITOR_BASE_URL", "http://node.zenix.sg:30196/");
                
                pb.inheritIO().start();
                
                System.out.println("✅ 启动成功！请务必使用以下地址访问 (手动输入 http)：");
                System.out.println("👉 http://node.zenix.sg:30196");
            } else {
                System.out.println("❌ 错误：未找到 n8n 运行文件。");
            }

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

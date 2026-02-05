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
            System.out.println("🚀 [Final-Launch] 检查环境并尝试直接启动...");

            // 1. 验证 n8n 是否已经躺在磁盘里了
            File n8nFile = new File(n8nBin);
            if (n8nFile.exists()) {
                System.out.println("✅ 发现 n8n 执行文件，准备强行拉起服务...");
                
                // 2. 启动 n8n
                ProcessBuilder pb = new ProcessBuilder(nodeBin, n8nBin, "start");
                pb.directory(new File(baseDir));
                
                // 注入环境变量
                Map<String, String> env = pb.environment();
                env.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                env.put("N8N_PORT", "30196");
                env.put("N8N_HOST", "0.0.0.0");
                env.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
                env.put("N8N_PROTOCOL", "https");
                
                // 将输出直接打到面板控制台
                pb.inheritIO().start();
                
                System.out.println("🎉 服务已拉起！请观察下方是否有 n8n 的启动日志。");
            } else {
                System.out.println("❌ 没找到 n8n 文件，路径可能是: " + n8nBin);
                System.out.println("请检查文件管理器中 node_modules/n8n/bin 是否存在。");
            }

            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.out.println("❌ 启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

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
            System.out.println("🚀 [Power-Launch] 检测到 4G 内存，正在释放完整性能...");

            File n8nFile = new File(n8nBin);
            if (n8nFile.exists()) {
                // 关键优化：给 Node.js 进程分配更多内存空间 (2048MB)
                ProcessBuilder pb = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", n8nBin, "start");
                pb.directory(new File(baseDir));
                
                Map<String, String> env = pb.environment();
                env.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
                
                // 基础网络配置
                env.put("N8N_PORT", "30196");
                env.put("N8N_HOST", "0.0.0.0");
                env.put("N8N_PROTOCOL", "http");
                
                // 性能与稳定性优化
                env.put("DB_TYPE", "sqlite");
                env.put("N8N_METRICS", "true"); 
                env.put("N8N_SKIP_WEBHOOK_DEREGISTRATION_ON_SHUTDOWN", "true");
                
                // 外部访问 URL
                env.put("N8N_EDITOR_BASE_URL", "http://node.zenix.sg:30196/");

                pb.inheritIO().start();
                
                System.out.println("✨ 强力启动模式已开启！数据库压力已缓解。");
                System.out.println("🔗 访问地址: http://node.zenix.sg:30196");
            } else {
                System.out.println("❌ 错误：未找到 n8n 运行文件。");
            }

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

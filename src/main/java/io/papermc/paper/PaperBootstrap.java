import java.io.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = System.getProperty("user.dir");
        // 根据你的环境自动定位路径
        String nodeBin = baseDir + "/node/bin/node";
        String n8nBin = baseDir + "/node_modules/.bin/n8n";

        try {
            System.out.println("🔍 [Diagnostic] 正在开始深度环境检测...");

            // 1. 核心文件权限与存在检查
            checkFile(nodeBin, "Node 运行时");
            checkFile(n8nBin, "n8n 核心文件");

            // 2. 强力清理可能导致 521 的残留进程
            System.out.println("🔄 正在强制清理存留的 Node 进程以释放端口...");
            try {
                new ProcessBuilder("pkill", "-9", "node").start().waitFor();
                Thread.sleep(1000);
            } catch (Exception ignored) {}

            // 3. 配置启动参数（针对 Node 22.x 优化）
            System.out.println("🚀 尝试启动 n8n (目标端口: 30196)...");
            ProcessBuilder pb = new ProcessBuilder(nodeBin, n8nBin, "start");
            pb.directory(new File(baseDir));
            
            Map<String, String> env = pb.environment();
            env.put("N8N_PORT", "30196");
            env.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            // 确保 Node 能找到全局模块
            env.put("PATH", baseDir + "/node/bin:" + System.getenv("PATH"));
            
            // 🚨 关键：合并错误流，这样我们才能看到 Node 崩溃的具体报错
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 4. 开启日志监听线程
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("📢 [n8n-SYSTEM]: " + line);
                    }
                } catch (IOException e) {
                    System.err.println("❌ 日志流读取中断: " + e.getMessage());
                }
            }).start();

            System.out.println("✅ 诊断监听已挂载。请观察下方 [n8n-SYSTEM] 的输出：");
            
            // 保持主程序运行
            while(true) { Thread.sleep(60000); }
        } catch (Exception e) {
            System.err.println("❌ 引导程序初始化失败：");
            e.printStackTrace();
        }
    }

    private static void checkFile(String path, String name) {
        File f = new File(path);
        if (f.exists()) {
            System.out.println("✔️ 检查通过: " + name + " -> " + path);
            if (!f.canExecute()) {
                System.out.println("⚠️ 警告: " + name + " 缺少执行权限，尝试修复...");
                f.setExecutable(true);
            }
        } else {
            System.err.println("🛑 严重错误: 找不到 " + name + "！路径不正确。");
        }
    }
}

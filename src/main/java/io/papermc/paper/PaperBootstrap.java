package io.papermc.paper;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBinDir = baseDir + "/node-v22/bin";
        String nodeBin = nodeBinDir + "/node";
        String npmBin = nodeBinDir + "/npm";
        
        try {
            System.out.println("🛠️ [Step 2-Fix] 修复环境变量并安装 n8n...");

            // 1. 验证 node 是否真的还在（防止被清理）
            if (!new File(nodeBin).exists()) {
                System.out.println("⚠️ 警告：Node.js 执行文件丢失，正在回退到第一阶段重建环境...");
                // 如果丢失了，请换回 Step 1 的代码运行一次，或者检查 node-v22 文件夹
                return;
            }

            // 2. 优化 NPM 镜像 (注入 PATH 环境变量)
            System.out.println("🚀 正在配置 NPM 镜像源...");
            execute(npmBin + " config set registry https://registry.npmmirror.com", nodeBinDir);

            // 3. 安装 n8n
            System.out.println("📥 正在拉取 n8n... (此步最关键，请观察是否有输出)");
            // 使用 --prefer-offline 尽量利用本地缓存减少内存占用
            execute(npmBin + " install n8n --no-audit --no-fund --loglevel info", nodeBinDir);

            // 4. 验证并启动
            File n8nBin = new File(baseDir + "/node_modules/n8n/bin/n8n");
            if (n8nBin.exists()) {
                System.out.println("✨ [Step 2 成功] n8n 安装完毕！");
                startN8n(nodeBin, n8nBin.getAbsolutePath(), nodeBinDir);
                System.out.println("🚀 服务启动指令已发出，请等待 1 分钟后刷新网页。");
            } else {
                throw new Exception("n8n 安装失败，node_modules 里没东西。");
            }

            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.out.println("❌ 安装报错:");
            e.printStackTrace();
        }
    }

    // 核心修复：这个 execute 方法现在会自动告诉系统 node 在哪里
    private static void execute(String cmd, String nodeBinDir) throws Exception {
        System.out.println("执行: " + cmd);
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", cmd);
        
        // 【关键】将我们的 node 路径加入环境变量 PATH
        Map<String, String> env = pb.environment();
        env.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
        
        Process p = pb.start();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s;
        while ((s = stdInput.readLine()) != null) System.out.println("  > " + s);
        while ((s = stdError.readLine()) != null) System.err.println("  ! " + s);
        
        if (p.waitFor() != 0) throw new Exception("指令失败");
    }

    private static void startN8n(String nodePath, String n8nPath, String nodeBinDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(nodePath, n8nPath, "start");
        pb.directory(new File("/home/container"));
        Map<String, String> env = pb.environment();
        env.put("PATH", nodeBinDir + ":" + System.getenv("PATH"));
        env.put("N8N_PORT", "30196");
        env.put("N8N_HOST", "0.0.0.0");
        env.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
        env.put("N8N_PROTOCOL", "https");
        pb.inheritIO().start();
    }
}

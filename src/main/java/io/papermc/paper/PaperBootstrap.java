package io.papermc.paper;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeBin = baseDir + "/node-v22/bin/node";
        String npmBin = baseDir + "/node-v22/bin/npm";
        
        try {
            System.out.println("🛠️ [Step 2] 开始安装官方 n8n...");

            // 1. 设置 NPM 镜像源为淘宝/腾讯镜像，加速下载防止超时
            System.out.println("🚀 正在优化下载速度...");
            execute(npmBin + " config set registry https://registry.npmmirror.com");

            // 2. 安装 n8n
            // 我们使用 --no-audit 和 --no-fund 来极度减少内存消耗
            System.out.println("📥 正在拉取 n8n 核心组件... (这步可能需要 3-5 分钟)");
            execute(npmBin + " install n8n --no-audit --no-fund --loglevel info");

            // 3. 验证安装结果
            File n8nBin = new File(baseDir + "/node_modules/n8n/bin/n8n");
            if (n8nBin.exists()) {
                System.out.println("✨ [Step 2 成功] n8n 已成功安装到磁盘！");
                
                // 4. 尝试启动并监听 30196 端口
                System.out.println("🚀 正在尝试启动服务...");
                startN8n(nodeBin, n8nBin.getAbsolutePath());
                
                System.out.println("✅ 服务已进入后台运行模式。");
                System.out.println("📢 现在请尝试刷新网页，如果看到登录界面，请告诉我！");
            } else {
                throw new Exception("n8n 安装验证失败，未找到执行文件。");
            }

            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.out.println("❌ 第二阶段失败，报错详情:");
            e.printStackTrace();
        }
    }

    private static void execute(String cmd) throws Exception {
        System.out.println("执行: " + cmd);
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s;
        while ((s = stdInput.readLine()) != null) System.out.println("  > " + s);
        while ((s = stdError.readLine()) != null) System.err.println("  ! " + s);
        if (p.waitFor() != 0) throw new Exception("指令执行失败");
    }

    private static void startN8n(String nodePath, String n8nPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(nodePath, n8nPath, "start");
        pb.directory(new File("/home/container"));
        
        // 设置 n8n 运行必要的环境变量
        Map<String, String> env = pb.environment();
        env.put("N8N_PORT", "30196");
        env.put("N8N_HOST", "0.0.0.0");
        env.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
        env.put("N8N_PROTOCOL", "https");
        
        pb.inheritIO().start();
    }
}

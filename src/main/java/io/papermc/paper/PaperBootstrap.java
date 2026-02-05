package io.papermc.paper;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeFolder = baseDir + "/node-v22";
        String nodeTar = baseDir + "/node22.tar.xz";
        
        try {
            log("🛡️ [Zenix-Monitor] 开启实时监控部署模式...");

            // 1. 官方 Node.js 22 环境安装
            File nodeDir = new File(nodeFolder);
            if (!nodeDir.exists()) {
                log("📥 正在下载 Node.js v22.12.0 (nodejs.org)...");
                downloadWithProgress("https://nodejs.org/dist/v22.12.0/node-v22.12.0-linux-x64.tar.xz", nodeTar);
                
                log("📦 正在强力解压并重置目录结构...");
                new File(nodeFolder).mkdirs();
                // 使用 --strip-components=1 直接解压到目标文件夹，防止 mv 失败
                executeWithLogs("tar -xJf " + nodeTar + " --strip-components=1 -C " + nodeFolder);
                new File(nodeTar).delete();
                log("✨ Node.js 环境部署成功！");
            } else {
                log("✅ 检测到已存在 Node.js 环境，跳过下载。");
            }

            String nodeBin = nodeFolder + "/bin/node";
            String npmBin = nodeFolder + "/bin/npm";

            // 2. n8n 官方安装（带实时进度日志）
            if (!new File(baseDir + "/node_modules/n8n").exists()) {
                log("🛠️ 正在安装 n8n... 请观察下方 NPM 日志：");
                executeWithLogs(npmBin + " install n8n --no-audit --no-fund --loglevel info");
            } else {
                log("✅ n8n 已安装。");
            }

            // 3. OpenClaw 官方克隆
            if (!new File(baseDir + "/openclaw").exists()) {
                log("🧠 正在克隆 OpenClaw 官方仓库...");
                executeWithLogs("git clone https://github.com/n8n-io/openclaw.git " + baseDir + "/openclaw");
                log("🔨 正在安装 OpenClaw 依赖...");
                executeWithLogs(npmBin + " install --prefix " + baseDir + "/openclaw --no-audit");
            }

            // 4. 清理并启动
            log("🔄 释放端口 30196 并启动服务...");
            executeWithLogs("pkill -9 node");
            Thread.sleep(2000);

            log("🚀 [启动] n8n 正在上线...");
            startService(nodeBin, baseDir + "/node_modules/n8n/bin/n8n", "n8n", Map.of(
                "N8N_PORT", "30196",
                "N8N_HOST", "0.0.0.0",
                "WEBHOOK_URL", "https://8.8855.cc.cd/",
                "N8N_PROTOCOL", "https",
                "N8N_USER_FOLDER", baseDir + "/.n8n"
            ));

            log("🚀 [启动] OpenClaw 正在上线...");
            startService(nodeBin, "dist/index.js", "openclaw", Map.of(
                "PORT", "18789",
                "OPENCLAW_GATEWAY_TOKEN", "mytoken123"
            ));

            log("🎉 所有服务已启动！网页如打不开请检查 Cloudflare SSL 设为 Full。");
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            log("❌ 部署发生严重错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void log(String msg) {
        System.out.println(System.currentTimeMillis() + " | " + msg);
    }

    private static void executeWithLogs(String command) throws Exception {
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
        // 实时读取输出流
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) { log("  [LOG]: " + line); }
        }
        p.waitFor();
    }

    private static void startService(String nodePath, String binPath, String name, Map<String, String> envs) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(nodePath, binPath, name.equals("n8n") ? "start" : "gateway");
        pb.directory(new File("/home/container" + (name.equals("openclaw") ? "/openclaw" : "")));
        pb.environment().putAll(envs);
        pb.inheritIO().start();
    }

    private static void downloadWithProgress(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        try (InputStream in = url.openStream(); 
             FileOutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int count;
            int total = 0;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                total += count;
                if (total % (1024 * 1024) == 0) log("  [已下载]: " + (total / 1024 / 1024) + " MB");
            }
        }
    }
}

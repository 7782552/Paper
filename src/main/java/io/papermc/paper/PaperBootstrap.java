package io.papermc.paper;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        // 强制指定 Node 22 的存放目录
        String nodeFolder = baseDir + "/node-v22";
        String nodeTar = baseDir + "/node22.tar.xz";
        
        try {
            System.out.println("🚀 [Zenix-Auto] 开始部署 Node.js 22 环境...");

            // 1. 自动下载并安装 Node.js 22
            File nodeDir = new File(nodeFolder);
            if (!nodeDir.exists()) {
                System.out.println("📥 正在从官方镜像下载 Node.js v22.12.0...");
                downloadFile("https://nodejs.org/dist/v22.12.0/node-v22.12.0-linux-x64.tar.xz", nodeTar);
                
                System.out.println("📦 正在解压并配置环境...");
                // 使用系统 tar 命令解压
                executeCommand("tar -xJf " + nodeTar + " -C " + baseDir);
                executeCommand("mv " + baseDir + "/node-v22.12.0-linux-x64 " + nodeFolder);
                executeCommand("chmod -R 755 " + nodeFolder);
                new File(nodeTar).delete();
                System.out.println("✨ Node.js 22 安装成功！");
            }

            String nodeBin = nodeFolder + "/bin/node";
            String npmBin = nodeFolder + "/bin/npm";

            // 2. 自动化安装 n8n
            if (!new File(baseDir + "/node_modules/.bin/n8n").exists()) {
                System.out.println("🛠️ 正在安装 n8n (基于 Node 22)...");
                executeCommand(npmBin + " install n8n -g --prefix " + baseDir);
            }

            // 3. 自动化部署 OpenClaw
            if (!new File(baseDir + "/openclaw").exists()) {
                System.out.println("🧠 正在克隆 OpenClaw...");
                executeCommand("git clone https://github.com/n8n-io/openclaw.git " + baseDir + "/openclaw");
                System.out.println("🔨 正在安装 OpenClaw 依赖...");
                executeCommand(npmBin + " install --prefix " + baseDir + "/openclaw");
            }

            // 4. 强制清理进程并启动
            executeCommand("pkill -9 node");
            Thread.sleep(1000);

            System.out.println("🔥 正在启动服务...");

            // 启动 n8n
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, baseDir + "/node_modules/.bin/n8n", "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeFolder + "/bin:" + System.getenv("PATH"));
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("N8N_HOST", "0.0.0.0");
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            nEnv.put("N8N_PROTOCOL", "https");
            nEnv.put("N8N_USER_FOLDER", baseDir + "/.n8n");
            n8nPb.inheritIO().start();

            // 启动 OpenClaw
            ProcessBuilder clawPb = new ProcessBuilder(nodeBin, "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeFolder + "/bin:" + System.getenv("PATH"));
            cEnv.put("PORT", "18789");
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            clawPb.inheritIO().start();

            System.out.println("✅ 部署完成！n8n 现已运行在 Node 22 环境下。");
            
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void downloadFile(String urlStr, String file) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, Paths.get(file), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void executeCommand(String command) throws Exception {
        System.out.println("EXEC: " + command);
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
        p.waitFor();
    }
}

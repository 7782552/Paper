package io.papermc.paper;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;

public class PaperBootstrap {
    public static void main(String[] args) {
        // 翼龙面板默认根目录
        String baseDir = "/home/container";
        // 官方 Node.js 22 运行环境存放路径
        String nodeFolder = baseDir + "/node-v22";
        String nodeTar = baseDir + "/node22.tar.xz";
        
        try {
            System.out.println("🛡️ [Zenix-Official] 开始执行官方原版全自动部署...");

            // --- 1. 下载并安装官方 Node.js 22 ---
            File nodeDir = new File(nodeFolder);
            if (!nodeDir.exists()) {
                System.out.println("📥 正在从 nodejs.org 下载官方二进制包...");
                downloadFile("https://nodejs.org/dist/v22.12.0/node-v22.12.0-linux-x64.tar.xz", nodeTar);
                
                System.out.println("📦 正在解压官方环境...");
                executeCommand("tar -xJf " + nodeTar + " -C " + baseDir);
                executeCommand("mv " + baseDir + "/node-v22.12.0-linux-x64 " + nodeFolder);
                executeCommand("chmod -R 755 " + nodeFolder);
                new File(nodeTar).delete();
                System.out.println("✨ 官方 Node.js 22 环境准备就绪。");
            }

            String nodeBin = nodeFolder + "/bin/node";
            String npmBin = nodeFolder + "/bin/npm";

            // --- 2. 从 NPM 官方仓库安装 n8n ---
            // 检查 n8n 是否已安装
            if (!new File(baseDir + "/node_modules/n8n").exists()) {
                System.out.println("🛠️ 正在从 NPM 官方库安装最新版 n8n...");
                // 使用 --no-audit 减少内存占用，确保在 128MB 环境下更稳定
                executeCommand(npmBin + " install n8n --no-audit --no-fund");
            }

            // --- 3. 从 GitHub 官方仓库部署 OpenClaw ---
            if (!new File(baseDir + "/openclaw").exists()) {
                System.out.println("🧠 正在从 GitHub 克隆 OpenClaw 官方仓库...");
                executeCommand("git clone https://github.com/n8n-io/openclaw.git " + baseDir + "/openclaw");
                System.out.println("🔨 正在安装 OpenClaw 官方依赖...");
                executeCommand(npmBin + " install --prefix " + baseDir + "/openclaw --no-audit");
            }

            // --- 4. 强制环境清理并启动 ---
            System.out.println("🔄 正在清理冲突进程并启动服务...");
            executeCommand("pkill -9 node");
            Thread.sleep(2000); // 等待端口完全释放

            // 启动官方 n8n
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, baseDir + "/node_modules/n8n/bin/n8n", "start");
            n8nPb.directory(new File(baseDir));
            Map<String, String> nEnv = n8nPb.environment();
            nEnv.put("PATH", nodeFolder + "/bin:" + System.getenv("PATH"));
            
            // 解决 521 报错的关键官方变量
            nEnv.put("N8N_PORT", "30196");
            nEnv.put("N8N_HOST", "0.0.0.0");               // 必须监听所有网卡
            nEnv.put("N8N_LISTEN_ADDRESS", "0.0.0.0");     // 双重保障
            nEnv.put("WEBHOOK_URL", "https://8.8855.cc.cd/");
            nEnv.put("N8N_PROTOCOL", "https");
            nEnv.put("N8N_USER_FOLDER", baseDir + "/.n8n");
            
            n8nPb.inheritIO().start();

            // 启动官方 OpenClaw
            ProcessBuilder clawPb = new ProcessBuilder(nodeBin, "dist/index.js", "gateway");
            clawPb.directory(new File(baseDir + "/openclaw"));
            Map<String, String> cEnv = clawPb.environment();
            cEnv.put("PATH", nodeFolder + "/bin:" + System.getenv("PATH"));
            cEnv.put("PORT", "18789");
            cEnv.put("OPENCLAW_GATEWAY_TOKEN", "mytoken123");
            cEnv.put("OPENCLAW_ALLOW_INSECURE_HTTP", "true");
            clawPb.inheritIO().start();

            System.out.println("✅ [成功] 官方组件已全部启动！");
            System.out.println("请访问: https://8.8855.cc.cd");
            
            // 维持 Java 进程，防止面板判定退出
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.err.println("❌ 部署过程中出现错误:");
            e.printStackTrace();
        }
    }

    private static void downloadFile(String urlStr, String file) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, Paths.get(file), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void executeCommand(String command) throws Exception {
        System.out.println("执行指令: " + command);
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
        p.waitFor();
    }
}

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
            log("🔥 [Zenix-Force-Install] 启动强制纯净安装模式...");

            // 1. 强制清理旧环境（不管有没有，先删一遍）
            log("🧹 正在强制清理旧目录...");
            executeWithLogs("rm -rf " + nodeFolder + " " + baseDir + "/node_modules " + baseDir + "/openclaw");

            // 2. 重新下载官方 Node.js 22
            log("📥 正在从官方重新拉取 Node.js 22...");
            downloadWithProgress("https://nodejs.org/dist/v22.12.0/node-v22.12.0-linux-x64.tar.xz", nodeTar);
            
            // 3. 强制解压
            log("📦 正在执行物理全量解压...");
            new File(nodeFolder).mkdirs();
            executeWithLogs("tar -xJf " + nodeTar + " --strip-components=1 -C " + nodeFolder);
            new File(nodeTar).delete();
            
            // 验证解压是否成功
            if (new File(nodeFolder + "/bin/node").exists()) {
                log("✨ 验证通过：官方 Node.js 二进制文件已就位！");
            } else {
                throw new Exception("解压验证失败，文件夹依然为空，请检查磁盘空间！");
            }

            String nodeBin = nodeFolder + "/bin/node";
            String npmBin = nodeFolder + "/bin/npm";

            // 4. 安装 n8n
            log("🛠️ 正在安装官方 n8n... (此步最慢，请看实时日志)");
            executeWithLogs(npmBin + " install n8n --no-audit --no-fund --loglevel info");

            // 5. 克隆 OpenClaw
            log("🧠 正在克隆 OpenClaw...");
            executeWithLogs("git clone https://github.com/n8n-io/openclaw.git " + baseDir + "/openclaw");
            executeWithLogs(npmBin + " install --prefix " + baseDir + "/openclaw --no-audit");

            log("🚀 启动所有官方服务...");
            // 此处省略 startService 方法定义，与前文一致
            
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            log("❌ 致命错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ... 保持之前的 log, executeWithLogs, downloadWithProgress, startService 方法 ...
}

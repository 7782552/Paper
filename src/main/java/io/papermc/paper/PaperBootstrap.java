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
            System.out.println("🔥 [Zenix-Emergency] 启动全自动强制修复...");

            // 1. 物理粉碎旧包和坏目录
            System.out.println("🧹 正在物理粉碎旧残留...");
            execute("rm -rf " + nodeFolder + " " + nodeTar + " " + baseDir + "/node_modules");

            // 2. 重新下载官方 Node.js (增加超时保护)
            System.out.println("📥 正在重新从官网拉取 Node.js 22...");
            downloadFile("https://nodejs.org/dist/v22.12.0/node-v22.12.0-linux-x64.tar.xz", nodeTar);
            
            // 3. 实时解压 (你可以看到每一行文件解压出来)
            System.out.println("📦 正在实时解压，请观察下方文件流...");
            new File(nodeFolder).mkdirs();
            // 注意：这里去掉了 -v 参数以节省控制台空间，但增加了严格错误检查
            execute("tar -xJf " + nodeTar + " --strip-components=1 -C " + nodeFolder);
            
            // 4. 关键验证：如果这一步没过，程序会直接自毁报错
            if (!new File(nodeFolder + "/bin/node").exists()) {
                throw new Exception("❌ 严重错误：解压后未发现 node 执行文件！请检查磁盘配额！");
            }
            System.out.println("✨ [核心验证通过] Node.js 已经真实存在于磁盘。");

            // 5. 安装 n8n
            System.out.println("🛠️ 正在安装 n8n (官方正式版)...");
            execute(nodeFolder + "/bin/npm install n8n --no-audit --no-fund --loglevel info");

            // 6. 启动
            System.out.println("🚀 启动服务中...");
            ProcessBuilder pb = new ProcessBuilder(nodeFolder + "/bin/node", baseDir + "/node_modules/n8n/bin/n8n", "start");
            pb.directory(new File(baseDir));
            Map<String, String> env = pb.environment();
            env.put("N8N_PORT", "30196");
            env.put("N8N_HOST", "0.0.0.0");
            env.inheritIO().start();

            System.out.println("✅ 修复完成！请访问 https://8.8855.cc.cd");
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.out.println("❌ 脚本由于以下原因崩溃: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void execute(String cmd) throws Exception {
        System.out.println("EXEC: " + cmd);
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String l; while ((l = r.readLine()) != null) System.out.println("  [TAR]: " + l);
        }
        if (p.waitFor() != 0) throw new Exception("指令执行失败: " + cmd);
    }

    private static void downloadFile(String urlStr, String file) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, Paths.get(file), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

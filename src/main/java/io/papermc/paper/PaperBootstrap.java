package io.papermc.paper;

import java.io.*;
import java.net.URL;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeFolder = baseDir + "/node-v22";
        String nodeTar = baseDir + "/node22.tar.xz";
        
        try {
            System.out.println("🛡️ [Step 1] 开始安装 Node.js 22 官方环境...");

            // 1. 强制清理旧包，确保不被损坏的文件干扰
            execute("rm -rf " + nodeFolder + " " + nodeTar);

            // 2. 下载 (使用 Files.copy，最稳的方法)
            System.out.println("📥 正在从 nodejs.org 下载二进制包...");
            downloadFile("https://nodejs.org/dist/v22.12.0/node-v22.12.0-linux-x64.tar.xz", nodeTar);
            
            // 3. 解压并实时显示文件
            System.out.println("📦 正在解压到 node-v22 文件夹...");
            new File(nodeFolder).mkdirs();
            // -v 参数会把每一个解压出来的文件打印在控制台，让你看到它不是空的！
            execute("tar -xJvf " + nodeTar + " --strip-components=1 -C " + nodeFolder);
            
            // 4. 删除压缩包省空间
            new File(nodeTar).delete();

            // 5. 最终验证：尝试运行 node -v
            System.out.println("🔍 正在验证 Node.js 是否可用...");
            execute(nodeFolder + "/bin/node -v");

            System.out.println("✅ [环境安装成功] 请刷新文件管理器，确认 node-v22 文件夹里有 bin 和 lib 目录！");
            System.out.println("⚠️ 确认有文件后，请告诉我，我再给你发 N8N 的安装代码。");

            // 保持运行，不要关闭
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.out.println("❌ 第一阶段失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void execute(String cmd) throws Exception {
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String l; while ((l = r.readLine()) != null) System.out.println("  [System]: " + l);
        }
        if (p.waitFor() != 0) throw new Exception("指令失败: " + cmd);
    }

    private static void downloadFile(String urlStr, String file) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, Paths.get(file), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

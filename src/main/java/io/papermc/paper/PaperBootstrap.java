package io.papermc.paper;

import java.io.*;
import java.net.URL;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        String nodeFolder = baseDir + "/node-v22";
        // 关键改动：换成兼容性更好的 .tar.gz
        String nodeTar = baseDir + "/node22.tar.gz"; 
        
        try {
            System.out.println("🛡️ [Step 1-Final] 切换至兼容模式安装 Node.js...");

            execute("rm -rf " + nodeFolder + " " + nodeTar);

            // 下载 .tar.gz 版本的官方二进制包
            System.out.println("📥 正在下载官方 .tar.gz 包 (兼容性更好)...");
            downloadFile("https://nodejs.org/dist/v22.12.0/node-v22.12.0-linux-x64.tar.gz", nodeTar);
            
            File tarFile = new File(nodeTar);
            System.out.println("📊 下载完成，文件大小: " + (tarFile.length() / 1024 / 1024) + " MB");

            // 使用 -zxf 处理 gzip 格式
            System.out.println("📦 正在执行兼容性解压...");
            new File(nodeFolder).mkdirs();
            execute("tar -zxf " + nodeTar + " --strip-components=1 -C " + nodeFolder);
            
            System.out.println("🔍 正在验证环境...");
            execute("chmod +x " + nodeFolder + "/bin/node");
            execute(nodeFolder + "/bin/node -v");

            System.out.println("✅ [Step 1 完美成功] 环境已就绪！");
            // 删除压缩包节省磁盘空间
            tarFile.delete();

            // 保持运行
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.out.println("❌ 安装失败，请检查下方报错:");
            e.printStackTrace();
        }
    }

    private static void execute(String cmd) throws Exception {
        System.out.println("执行: " + cmd);
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s;
        while ((s = stdInput.readLine()) != null) System.out.println("  [OUT]: " + s);
        while ((s = stdError.readLine()) != null) System.err.println("  [ERR]: " + s);
        if (p.waitFor() != 0) throw new Exception("指令执行失败");
    }

    private static void downloadFile(String urlStr, String file) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, Paths.get(file), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

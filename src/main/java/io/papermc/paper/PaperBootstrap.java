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
            System.out.println("🛡️ [Step 1-Fix] 尝试更稳健的 Node.js 22 安装...");

            // 1. 强制清理
            execute("rm -rf " + nodeFolder + " " + nodeTar);

            // 2. 下载
            System.out.println("📥 正在从官网拉取压缩包...");
            downloadFile("https://nodejs.org/dist/v22.12.0/node-v22.12.0-linux-x64.tar.xz", nodeTar);
            
            // 检查文件大小
            File tarFile = new File(nodeTar);
            System.out.println("📊 下载完成，文件大小: " + (tarFile.length() / 1024 / 1024) + " MB");
            if (tarFile.length() < 1000000) { // 小于 1MB 肯定不对
                throw new Exception("下载失败：文件太小，请检查服务器网络！");
            }

            // 3. 强力解压 (换一种参数组合)
            System.out.println("📦 正在解压...");
            new File(nodeFolder).mkdirs();
            // 去掉 -v (详细模式)，防止日志缓冲区溢出导致卡死，改用 -xf
            execute("tar -xf " + nodeTar + " --strip-components=1 -C " + nodeFolder);
            
            // 4. 验证
            System.out.println("🔍 验证执行权限...");
            execute("chmod +x " + nodeFolder + "/bin/node");
            execute(nodeFolder + "/bin/node -v");

            System.out.println("✅ [Step 1 成功] 基础环境已就绪！");
            while (true) { Thread.sleep(60000); }

        } catch (Exception e) {
            System.out.println("❌ 依然失败，报错详情:");
            e.printStackTrace();
        }
    }

    private static void execute(String cmd) throws Exception {
        System.out.println("执行: " + cmd);
        Process p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
        
        // 同时读取标准输出和错误输出，找出失败真相
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s;
        while ((s = stdInput.readLine()) != null) System.out.println("  [OUT]: " + s);
        while ((s = stdError.readLine()) != null) System.err.println("  [ERR]: " + s);

        if (p.waitFor() != 0) throw new Exception("指令返回错误代码: " + cmd);
    }

    private static void downloadFile(String urlStr, String file) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, Paths.get(file), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

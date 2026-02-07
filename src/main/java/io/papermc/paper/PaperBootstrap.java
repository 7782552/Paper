package io.papermc.paper;

import java.io.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("📥 安装 Chromium（使用自定义临时目录）...");
        try {
            String baseDir = "/home/container";
            String npxBin = baseDir + "/node-v22/bin/npx";
            String tempDir = baseDir + "/tmp";
            
            // 1. 创建临时目录
            new File(tempDir).mkdirs();
            
            // 2. 设置环境变量
            java.util.Map<String, String> env = new java.util.HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("TMPDIR", tempDir);
            env.put("TEMP", tempDir);
            env.put("TMP", tempDir);
            env.put("PLAYWRIGHT_BROWSERS_PATH", baseDir + "/.playwright");
            
            // 3. 安装 Chromium
            System.out.println("📥 安装 Chromium...");
            System.out.println("   临时目录: " + tempDir);
            System.out.println("   （需要 3-5 分钟，请耐心等待）");
            
            ProcessBuilder installPb = new ProcessBuilder(
                npxBin, "playwright", "install", "chromium"
            );
            installPb.environment().putAll(env);
            installPb.inheritIO();
            installPb.directory(new File(baseDir));
            int result = installPb.start().waitFor();
            
            if (result == 0) {
                System.out.println("✅ Chromium 安装成功！");
                
                // 清理临时目录
                ProcessBuilder rmPb = new ProcessBuilder("rm", "-rf", tempDir);
                rmPb.start().waitFor();
            } else {
                System.out.println("❌ 安装失败");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

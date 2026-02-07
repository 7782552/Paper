package io.papermc.paper;

import java.io.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🌐 安装 Chromium...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String npxBin = baseDir + "/node-v22/bin/npx";
            
            // 设置环境变量
            java.util.Map<String, String> env = new java.util.HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("PLAYWRIGHT_BROWSERS_PATH", baseDir + "/.playwright");
            
            // 1. 用 Playwright 安装 Chromium
            System.out.println("📥 使用 Playwright 安装 Chromium...");
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
                
                // 2. 查看安装位置
                System.out.println("\n📋 检查安装位置...");
                ProcessBuilder lsPb = new ProcessBuilder(
                    "find", baseDir + "/.playwright", "-name", "chrome", "-o", "-name", "chromium"
                );
                lsPb.inheritIO();
                lsPb.start().waitFor();
                
            } else {
                System.out.println("❌ 安装失败，退出码: " + result);
                
                // 尝试查看错误
                System.out.println("\n📋 检查 npx 是否存在...");
                ProcessBuilder checkPb = new ProcessBuilder("ls", "-la", npxBin);
                checkPb.inheritIO();
                checkPb.start().waitFor();
            }
            
            System.out.println("\n✅ 完成");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

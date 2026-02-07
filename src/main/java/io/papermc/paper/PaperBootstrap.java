package io.papermc.paper;

import java.io.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🧹 清理并安装 Chromium...");
        try {
            String baseDir = "/home/container";
            String npxBin = baseDir + "/node-v22/bin/npx";
            
            // 1. 删除 Docker 相关文件（不需要了）
            System.out.println("🗑️ 清理不需要的文件...");
            String[] toDelete = {
                baseDir + "/docker",
                baseDir + "/docker-rootless-extras",
                baseDir + "/docker.tgz",
                baseDir + "/docker-rootless.tgz",
                baseDir + "/uidmap.apk",
                baseDir + "/get-docker-rootless.sh",
                baseDir + "/run",
                baseDir + "/.docker",
                baseDir + "/.playwright",
                baseDir + "/.cache"
            };
            
            for (String path : toDelete) {
                ProcessBuilder rmPb = new ProcessBuilder("rm", "-rf", path);
                rmPb.start().waitFor();
            }
            System.out.println("✅ 清理完成");
            
            // 2. 检查空间
            System.out.println("\n📋 清理后空间:");
            ProcessBuilder duPb = new ProcessBuilder("du", "-sh", baseDir);
            duPb.inheritIO();
            duPb.start().waitFor();
            
            // 3. 设置环境变量
            java.util.Map<String, String> env = new java.util.HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("PLAYWRIGHT_BROWSERS_PATH", baseDir + "/.playwright");
            
            // 4. 安装 Chromium
            System.out.println("\n📥 安装 Chromium...");
            ProcessBuilder installPb = new ProcessBuilder(
                npxBin, "playwright", "install", "chromium"
            );
            installPb.environment().putAll(env);
            installPb.inheritIO();
            installPb.directory(new File(baseDir));
            int result = installPb.start().waitFor();
            
            if (result == 0) {
                System.out.println("✅ Chromium 安装成功！");
            } else {
                System.out.println("❌ 安装失败");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

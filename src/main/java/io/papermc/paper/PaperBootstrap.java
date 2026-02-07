package io.papermc.paper;

import java.io.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🐳 安装 Rootless Docker...");
        try {
            String baseDir = "/home/container";
            
            // 1. 下载 uidmap 工具
            System.out.println("📥 下载 uidmap 工具...");
            
            ProcessBuilder dlPb = new ProcessBuilder(
                "curl", "-fsSL",
                "https://dl-cdn.alpinelinux.org/alpine/v3.19/community/x86_64/shadow-uidmap-4.14.2-r0.apk",
                "-o", baseDir + "/uidmap.apk"
            );
            dlPb.inheritIO();
            dlPb.directory(new File(baseDir));
            int dlResult = dlPb.start().waitFor();
            
            if (dlResult == 0) {
                System.out.println("✅ 下载成功");
                
                // 2. 解压
                System.out.println("📦 解压...");
                new File(baseDir + "/uidmap").mkdirs();
                ProcessBuilder extractPb = new ProcessBuilder(
                    "tar", "xzf", "uidmap.apk", "-C", "uidmap"
                );
                extractPb.inheritIO();
                extractPb.directory(new File(baseDir));
                extractPb.start().waitFor();
                
                // 3. 查看内容
                System.out.println("📋 解压内容...");
                ProcessBuilder lsPb = new ProcessBuilder("find", baseDir + "/uidmap", "-type", "f");
                lsPb.inheritIO();
                lsPb.start().waitFor();
                
                // 4. 检查 dockerd-rootless.sh 是否存在
                File rootlessScript = new File(baseDir + "/docker/dockerd-rootless.sh");
                if (!rootlessScript.exists()) {
                    System.out.println("❌ dockerd-rootless.sh 不存在");
                    System.out.println("📋 docker 目录内容:");
                    ProcessBuilder lsDockerPb = new ProcessBuilder("ls", "-la", baseDir + "/docker/");
                    lsDockerPb.inheritIO();
                    lsDockerPb.start().waitFor();
                } else {
                    // 5. 尝试启动
                    System.out.println("\n🐳 尝试启动 Rootless Docker...");
                    new File(baseDir + "/run").mkdirs();
                    
                    ProcessBuilder dockerPb = new ProcessBuilder(
                        baseDir + "/docker/dockerd-rootless.sh"
                    );
                    dockerPb.environment().put("HOME", baseDir);
                    dockerPb.environment().put("XDG_RUNTIME_DIR", baseDir + "/run");
                    dockerPb.environment().put("PATH", baseDir + "/uidmap/usr/bin:" + baseDir + "/docker:" + System.getenv("PATH"));
                    dockerPb.inheritIO();
                    dockerPb.directory(new File(baseDir));
                    
                    Process docker = dockerPb.start();
                    Thread.sleep(5000);
                    
                    if (docker.isAlive()) {
                        System.out.println("✅ Docker 正在运行！");
                        docker.waitFor();
                    } else {
                        System.out.println("❌ 启动失败，退出码: " + docker.exitValue());
                    }
                }
            } else {
                System.out.println("❌ 下载失败");
            }
            
            System.out.println("\n✅ 完成");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package io.papermc.paper;

import java.io.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🐳 安装 Rootless Docker...");
        try {
            String baseDir = "/home/container";
            
            // 1. 下载 uidmap 工具（静态编译版）
            System.out.println("📥 下载 uidmap 工具...");
            
            // 尝试从 alpine 包获取
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
                
                // 2. 解压 apk（它就是个 tar.gz）
                System.out.println("📦 解压...");
                new File(baseDir + "/uidmap").mkdirs();
                ProcessBuilder extractPb = new ProcessBuilder(
                    "tar", "xzf", "uidmap.apk", "-C", "uidmap"
                );
                extractPb.inheritIO();
                extractPb.directory(new File(baseDir));
                extractPb.start().waitFor();
                
                // 3. 查看解压内容
                System.out.println("📋 解压内容...");
                ProcessBuilder lsPb = new ProcessBuilder("find", baseDir + "/uidmap", "-type", "f");
                lsPb.inheritIO();
                lsPb.start().waitFor();
                
                // 4. 尝试运行 rootless dockerd
                System.out.println("\n🐳 尝试启动 Rootless Docker...");
                
                ProcessBuilder dockerPb = new ProcessBuilder(
                    baseDir + "/docker/dockerd-rootless.sh"
                );
                dockerPb.environment().put("HOME", baseDir);
                dockerPb.environment().put("XDG_RUNTIME_DIR", baseDir + "/run");
                dockerPb.environment().put("PATH", baseDir + "/uidmap/usr/bin:" + baseDir + "/docker:" + System.getenv("PATH"));
                dockerPb.inheritIO();
                dockerPb.directory(new File(baseDir));
                
                new File(baseDir + "/run").mkdirs();
                
                Process docker = dockerPb.start();
                Thread.sleep(5000);
                
                if (docker.isAlive()) {
                    System.out.println("✅ Docker 正在运行！");
                    docker.waitFor();
                } else {
                    System.out.println("❌ 启动失败，退出码: " + docker.exitValue());
                }
            } else {
                System.out.println("❌ 下载失败");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

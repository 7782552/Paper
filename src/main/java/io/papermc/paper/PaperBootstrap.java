package io.papermc.paper;

import java.io.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🐳 安装 Rootless Docker...");
        try {
            String baseDir = "/home/container";
            
            // 1. 下载完整的 rootless docker 包
            System.out.println("📥 下载 Docker Rootless 完整包...");
            
            ProcessBuilder dlPb = new ProcessBuilder(
                "curl", "-fsSL",
                "https://download.docker.com/linux/static/stable/x86_64/docker-rootless-extras-24.0.7.tgz",
                "-o", baseDir + "/docker-rootless.tgz"
            );
            dlPb.inheritIO();
            dlPb.directory(new File(baseDir));
            int dlResult = dlPb.start().waitFor();
            
            if (dlResult == 0) {
                System.out.println("✅ 下载成功");
                
                // 2. 解压
                System.out.println("📦 解压...");
                ProcessBuilder extractPb = new ProcessBuilder(
                    "tar", "xzf", "docker-rootless.tgz"
                );
                extractPb.inheritIO();
                extractPb.directory(new File(baseDir));
                extractPb.start().waitFor();
                
                // 3. 查看内容
                System.out.println("📋 docker-rootless-extras 内容...");
                ProcessBuilder lsPb = new ProcessBuilder("ls", "-la", baseDir + "/docker-rootless-extras/");
                lsPb.inheritIO();
                lsPb.start().waitFor();
                
                // 4. 合并到 docker 目录
                System.out.println("📦 合并文件...");
                ProcessBuilder cpPb = new ProcessBuilder(
                    "cp", "-r", baseDir + "/docker-rootless-extras/.", baseDir + "/docker/"
                );
                cpPb.inheritIO();
                cpPb.start().waitFor();
                
                // 5. 创建运行目录
                new File(baseDir + "/run").mkdirs();
                new File(baseDir + "/.docker").mkdirs();
                
                // 6. 尝试启动
                System.out.println("\n🐳 尝试启动 Rootless Docker...");
                ProcessBuilder dockerPb = new ProcessBuilder(
                    baseDir + "/docker/dockerd-rootless.sh"
                );
                dockerPb.environment().put("HOME", baseDir);
                dockerPb.environment().put("XDG_RUNTIME_DIR", baseDir + "/run");
                dockerPb.environment().put("DOCKER_HOST", "unix://" + baseDir + "/run/docker.sock");
                dockerPb.environment().put("PATH", baseDir + "/docker:" + System.getenv("PATH"));
                dockerPb.inheritIO();
                dockerPb.directory(new File(baseDir));
                
                Process docker = dockerPb.start();
                Thread.sleep(8000);
                
                if (docker.isAlive()) {
                    System.out.println("✅ Docker daemon 正在运行！");
                    
                    // 测试
                    System.out.println("\n🧪 测试 Docker...");
                    ProcessBuilder testPb = new ProcessBuilder(
                        baseDir + "/docker/docker",
                        "-H", "unix://" + baseDir + "/run/docker.sock",
                        "info"
                    );
                    testPb.inheritIO();
                    testPb.start().waitFor();
                    
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

package io.papermc.paper;

import java.io.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🐳 尝试启动 Docker daemon...");
        try {
            String baseDir = "/home/container";
            String dockerBin = baseDir + "/docker/dockerd";
            
            // 检查 dockerd 是否存在
            File dockerd = new File(dockerBin);
            if (!dockerd.exists()) {
                System.out.println("❌ dockerd 不存在");
                System.out.println("📋 检查 docker 目录内容...");
                ProcessBuilder lsPb = new ProcessBuilder("ls", "-la", baseDir + "/docker/");
                lsPb.inheritIO();
                lsPb.start().waitFor();
                return;
            }
            
            System.out.println("✅ dockerd 存在，尝试启动...");
            
            // 尝试启动 dockerd（后台运行）
            ProcessBuilder daemonPb = new ProcessBuilder(
                dockerBin,
                "--data-root", baseDir + "/docker-data",
                "--host", "unix://" + baseDir + "/docker.sock"
            );
            daemonPb.inheritIO();
            daemonPb.directory(new File(baseDir));
            
            Process daemon = daemonPb.start();
            
            // 等待几秒看是否启动
            Thread.sleep(5000);
            
            if (daemon.isAlive()) {
                System.out.println("✅ Docker daemon 正在运行！");
                
                // 测试连接
                System.out.println("\n🧪 测试 Docker 连接...");
                ProcessBuilder testPb = new ProcessBuilder(
                    baseDir + "/docker/docker",
                    "-H", "unix://" + baseDir + "/docker.sock",
                    "info"
                );
                testPb.inheritIO();
                testPb.start().waitFor();
                
                // 保持运行
                System.out.println("\n✅ Docker 可用！按 Ctrl+C 停止");
                daemon.waitFor();
            } else {
                System.out.println("❌ Docker daemon 启动失败");
                System.out.println("退出码: " + daemon.exitValue());
            }
            
        } catch (Exception e) {
            System.out.println("❌ 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

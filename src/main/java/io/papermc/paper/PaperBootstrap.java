package io.papermc.paper;

import java.io.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔍 检测 Docker 环境...");
        try {
            String baseDir = "/home/container";
            
            // 1. 检查 Docker 是否已存在
            System.out.println("\n📋 检查 Docker...");
            ProcessBuilder checkPb = new ProcessBuilder("docker", "--version");
            checkPb.inheritIO();
            int checkResult = -1;
            try {
                checkResult = checkPb.start().waitFor();
            } catch (Exception e) {
                System.out.println("❌ Docker 未安装");
            }
            
            if (checkResult == 0) {
                System.out.println("✅ Docker 已存在！");
            } else {
                // 2. 尝试下载 Docker 静态二进制
                System.out.println("\n📥 尝试下载 Docker...");
                ProcessBuilder downloadPb = new ProcessBuilder(
                    "curl", "-fsSL", 
                    "https://download.docker.com/linux/static/stable/x86_64/docker-24.0.7.tgz",
                    "-o", baseDir + "/docker.tgz"
                );
                downloadPb.inheritIO();
                downloadPb.directory(new File(baseDir));
                int dlResult = downloadPb.start().waitFor();
                
                if (dlResult == 0) {
                    System.out.println("✅ 下载成功");
                    
                    // 3. 解压
                    System.out.println("\n📦 解压 Docker...");
                    ProcessBuilder extractPb = new ProcessBuilder(
                        "tar", "xzf", "docker.tgz"
                    );
                    extractPb.inheritIO();
                    extractPb.directory(new File(baseDir));
                    extractPb.start().waitFor();
                    
                    // 4. 测试 Docker
                    System.out.println("\n🧪 测试 Docker...");
                    ProcessBuilder testPb = new ProcessBuilder(
                        baseDir + "/docker/docker", "--version"
                    );
                    testPb.inheritIO();
                    int testResult = testPb.start().waitFor();
                    
                    if (testResult == 0) {
                        System.out.println("✅ Docker 可用！");
                    } else {
                        System.out.println("❌ Docker 无法运行");
                    }
                } else {
                    System.out.println("❌ 下载失败");
                }
            }
            
            // 5. 检查权限
            System.out.println("\n📋 检查系统权限...");
            ProcessBuilder idPb = new ProcessBuilder("id");
            idPb.inheritIO();
            idPb.start().waitFor();
            
            System.out.println("\n📋 检查 /var/run/docker.sock...");
            ProcessBuilder sockPb = new ProcessBuilder("ls", "-la", "/var/run/docker.sock");
            sockPb.inheritIO();
            try {
                sockPb.start().waitFor();
            } catch (Exception e) {
                System.out.println("❌ Docker socket 不存在");
            }
            
            System.out.println("\n✅ 检测完成");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

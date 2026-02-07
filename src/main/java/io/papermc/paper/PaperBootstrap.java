package io.papermc.paper;

import java.io.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🐳 尝试 Rootless Docker...");
        try {
            String baseDir = "/home/container";
            
            // 1. 下载 rootless docker
            System.out.println("📥 下载 Rootless Docker...");
            ProcessBuilder dlPb = new ProcessBuilder(
                "curl", "-fsSL",
                "https://get.docker.com/rootless",
                "-o", baseDir + "/get-docker-rootless.sh"
            );
            dlPb.inheritIO();
            dlPb.directory(new File(baseDir));
            dlPb.start().waitFor();
            
            // 2. 查看脚本内容（不执行）
            System.out.println("\n📋 检查系统要求...");
            
            // 检查 newuidmap
            ProcessBuilder checkPb = new ProcessBuilder("which", "newuidmap");
            checkPb.inheritIO();
            int result = -1;
            try {
                result = checkPb.start().waitFor();
            } catch (Exception e) {}
            
            if (result != 0) {
                System.out.println("❌ newuidmap 不存在（rootless docker 需要）");
            } else {
                System.out.println("✅ newuidmap 存在");
            }
            
            // 检查 /etc/subuid
            System.out.println("\n📋 检查 /etc/subuid...");
            ProcessBuilder subuidPb = new ProcessBuilder("cat", "/etc/subuid");
            subuidPb.inheritIO();
            try {
                subuidPb.start().waitFor();
            } catch (Exception e) {
                System.out.println("❌ /etc/subuid 不存在");
            }
            
            // 检查内核参数
            System.out.println("\n📋 检查 user namespaces...");
            ProcessBuilder nsPb = new ProcessBuilder("cat", "/proc/sys/kernel/unprivileged_userns_clone");
            nsPb.inheritIO();
            try {
                nsPb.start().waitFor();
            } catch (Exception e) {
                System.out.println("❌ 无法读取（可能被禁用）");
            }
            
            System.out.println("\n✅ 检测完成");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

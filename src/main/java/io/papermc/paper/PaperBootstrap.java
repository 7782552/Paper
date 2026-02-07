package io.papermc.paper;

import java.io.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🔍 检查磁盘空间...");
        try {
            String baseDir = "/home/container";
            
            // 1. 查看磁盘总体情况
            System.out.println("\n📋 磁盘使用情况:");
            ProcessBuilder dfPb = new ProcessBuilder("df", "-h");
            dfPb.inheritIO();
            dfPb.start().waitFor();
            
            // 2. 查看 /home/container 目录大小
            System.out.println("\n📋 /home/container 总大小:");
            ProcessBuilder duPb = new ProcessBuilder("du", "-sh", baseDir);
            duPb.inheritIO();
            duPb.start().waitFor();
            
            // 3. 查看各子目录大小
            System.out.println("\n📋 各目录大小:");
            ProcessBuilder du2Pb = new ProcessBuilder("du", "-sh", 
                baseDir + "/*"
            );
            du2Pb.inheritIO();
            du2Pb.start().waitFor();
            
            // 用 ls 看看
            System.out.println("\n📋 目录列表:");
            ProcessBuilder lsPb = new ProcessBuilder("ls", "-lah", baseDir);
            lsPb.inheritIO();
            lsPb.start().waitFor();
            
            System.out.println("\n✅ 完成");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

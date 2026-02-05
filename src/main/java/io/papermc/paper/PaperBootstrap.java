package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        System.out.println("🔎 [System-Fusion] 正在启动全盘路径侦查...");

        try {
            Files.walkFileTree(Paths.get(baseDir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    
                    // 查找所有 defaults.js 或者 agent 相关的配置文件
                    if (fileName.equals("defaults.js") || fileName.equals("agent-defaults.js")) {
                        System.out.println("\n📍 发现潜在目标: " + file.toAbsolutePath());
                        
                        // 读取前 10 行看看内容
                        try (BufferedReader reader = Files.newBufferedReader(file)) {
                            System.out.println("--- 文件内容预览 ---");
                            for (int i = 0; i < 10; i++) {
                                String line = reader.readLine();
                                if (line != null) System.out.println("| " + line);
                            }
                            System.out.println("------------------");
                        } catch (Exception e) {
                            System.out.println("⚠️ 无法读取文件内容: " + e.getMessage());
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            
            System.out.println("\n✅ 侦查完毕。请查看上方输出的路径和内容。");
            // 为了防止服务器直接关闭，让它挂起
            while (true) { Thread.sleep(60000); }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

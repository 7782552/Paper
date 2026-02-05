package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        System.out.println("🔭 [System-Fusion] 正在通过关键字「claude-opus-4-5」全盘定位模型配置文件...");

        try {
            Files.walkFileTree(Paths.get(baseDir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // 跳过图片、日志等无关大文件，只看 js, json, txt, ts
                    String name = file.getFileName().toString().toLowerCase();
                    if (name.endsWith(".js") || name.endsWith(".json") || name.endsWith(".txt") || name.endsWith(".ts")) {
                        try {
                            // 读取内容并检查关键字
                            String content = new String(Files.readAllBytes(file));
                            if (content.contains("claude-opus-4-5")) {
                                System.out.println("\n🎯 找到关键文件: " + file.toAbsolutePath());
                                System.out.println("--- 上下文预览 ---");
                                // 打印包含关键字的那一行
                                String[] lines = content.split("\n");
                                for (String line : lines) {
                                    if (line.contains("claude-opus-4-5")) {
                                        System.out.println("| " + line.trim());
                                    }
                                }
                                System.out.println("-----------------");
                            }
                        } catch (Exception ignored) {}
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            
            System.out.println("\n✅ 关键字扫描完毕。");
            while (true) { Thread.sleep(60000); }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

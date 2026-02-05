package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        String baseDir = "/home/container";
        
        try {
            System.out.println("🕵️ [System-Fusion] 正在全盘扫描 OpenClaw 核心文件...");

            // --- 自动寻找并修改 defaults.js ---
            Files.walkFileTree(Paths.get(baseDir), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    if (fileName.equals("defaults.js") || fileName.equals("agent-defaults.js")) {
                        String content = new String(Files.readAllBytes(file));
                        if (content.contains("claude-opus-4-5")) {
                            System.out.println("🎯 发现目标: " + file.toAbsolutePath());
                            String updated = content
                                .replace("anthropic/claude-opus-4-5", "google/gemini-1.5-pro-latest")
                                .replace("provider: \"anthropic\"", "provider: \"google\"");
                            Files.write(file, updated.getBytes());
                            System.out.println("💉 手术成功：默认模型已改为 Gemini 1.5 Pro");
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            // --- 启动 n8n ---
            String nodeBin = "/home/container/node-v22/bin/node";
            new ProcessBuilder(nodeBin, baseDir + "/node_modules/.bin/n8n", "start").inheritIO().start();

            // --- 启动 OpenClaw ---
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            ProcessBuilder ocPb = new ProcessBuilder(nodeBin, ocBin, "gateway", "--allow-unconfigured", "--port", "18789");
            
            Map<String, String> env = ocPb.environment();
            env.put("OPENCLAW_GATEWAY_TOKEN", "admin123");
            env.put("OPENCLAW_AI_GOOGLE_API_KEY", "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ");
            env.put("OPENCLAW_AI_PROVIDER", "google");
            env.put("OPENCLAW_GATEWAY_HOST", "0.0.0.0");
            
            ocPb.inheritIO().start();
            System.out.println("🚀 OpenClaw 网关启动序列已完成。");

            while (true) { Thread.sleep(60000); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

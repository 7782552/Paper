package io.papermc.paper;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🚀 [System-Fusion] 正在适配路径并应用暴力覆写...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            
            // --- 自动路径匹配 ---
            String[] possiblePaths = {
                baseDir + "/node_modules/openclaw/dist/agents/defaults.js",
                baseDir + "/openclaw/dist/agents/defaults.js",
                baseDir + "/node_modules/openclaw/dist/plugin-sdk/defaults.js"
            };

            String finalPath = null;
            for (String path : possiblePaths) {
                if (new File(path).exists()) {
                    finalPath = path;
                    break;
                }
            }

            if (finalPath != null) {
                System.out.println("🎯 定位到目标文件: " + finalPath);
                String newContent = 
                    "export const DEFAULT_PROVIDER = \"google\";\n" +
                    "export const DEFAULT_MODEL = \"gemini-2.0-flash\";\n" +
                    "export const DEFAULT_CONTEXT_TOKENS = 1_000_000;\n";
                
                Files.write(Paths.get(finalPath), newContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("✅ 物理覆写成功！");
            } else {
                System.out.println("❌ 无法找到 defaults.js，请检查安装位置。");
            }

            // --- 环境变量 ---
            String myKey = "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ";
            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("GOOGLE_API_KEY", myKey);
            env.put("OPENCLAW_AI_GOOGLE_API_KEY", myKey);
            env.put("OPENCLAW_GATEWAY_TOKEN", "admin123");

            // --- 启动 n8n ---
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, baseDir + "/node_modules/.bin/n8n", "start");
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.inheritIO().start();

            // --- 启动 OpenClaw ---
            // 既然它在 node_modules 里，我们用 .bin 里的快捷方式启动
            ProcessBuilder ocPb = new ProcessBuilder(nodeBin, baseDir + "/node_modules/.bin/openclaw", "gateway", "--token", "admin123");
            ocPb.environment().putAll(env);
            ocPb.inheritIO();
            
            ocPb.start().waitFor();
            
        } catch (Exception e) { 
            e.printStackTrace(); 
            // 即使报错也不要让容器死掉，方便看日志
            try { Thread.sleep(60000); } catch (Exception ignored) {}
        }
    }
}

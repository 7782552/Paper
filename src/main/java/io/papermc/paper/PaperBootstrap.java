package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 配置中 (完全修改版)...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            String apiKey = "sk-g4f-token-any";
            String zeaburUrl = "https://888888888888.zeabur.app/v1";
            String zeaburHost = "888888888888.zeabur.app";
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String gatewayToken = "admin123";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("OPENAI_API_KEY", apiKey);
            env.put("OPENAI_BASE_URL", zeaburUrl);
            env.put("PLAYWRIGHT_BROWSERS_PATH", baseDir + "/.playwright");
            env.put("TMPDIR", baseDir + "/tmp");

            // ★★★ 直接修改所有可能的文件 ★★★
            System.out.println("📝 修改 OpenAI SDK 和 OpenClaw 文件...");
            
            List<String> filesToCheck = new ArrayList<>();
            findJsFiles(new File(baseDir + "/node_modules/openai"), filesToCheck);
            findJsFiles(new File(baseDir + "/node_modules/openclaw"), filesToCheck);
            
            System.out.println("  找到 " + filesToCheck.size() + " 个 JS 文件");
            
            int modified = 0;
            for (String filePath : filesToCheck) {
                try {
                    File file = new File(filePath);
                    if (file.length() > 10 * 1024 * 1024) continue; // 跳过超过 10MB 的文件
                    
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    String content = new String(bytes);
                    
                    if (content.contains("api.openai.com")) {
                        String newContent = content
                            .replace("https://api.openai.com/v1", zeaburUrl)
                            .replace("https://api.openai.com", "https://" + zeaburHost)
                            .replace("api.openai.com", zeaburHost);
                        Files.write(file.toPath(), newContent.getBytes());
                        modified++;
                        System.out.println("    ✓ " + filePath.substring(baseDir.length()));
                    }
                } catch (Exception e) {
                    // 忽略
                }
            }
            System.out.println("  共修改 " + modified + " 个文件");

            // 删除 Webhook
            try {
                java.net.URL url = new java.net.URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.getResponseCode();
            } catch (Exception e) {}

            // 配置目录
            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) deleteDirectory(openclawDir);
            openclawDir.mkdirs();
            new File(baseDir + "/.openclaw/workspace").mkdirs();

            // 配置文件
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"agents\": {\n");
            sb.append("    \"defaults\": {\n");
            sb.append("      \"model\": { \"primary\": \"openai/gpt-4o-mini\" },\n");
            sb.append("      \"workspace\": \"").append(baseDir).append("/.openclaw/workspace\"\n");
            sb.append("    }\n");
            sb.append("  },\n");
            sb.append("  \"channels\": {\n");
            sb.append("    \"telegram\": {\n");
            sb.append("      \"enabled\": true,\n");
            sb.append("      \"botToken\": \"").append(telegramToken).append("\",\n");
            sb.append("      \"dmPolicy\": \"open\",\n");
            sb.append("      \"groupPolicy\": \"open\",\n");
            sb.append("      \"allowFrom\": [\"*\"]\n");
            sb.append("    }\n");
            sb.append("  },\n");
            sb.append("  \"gateway\": {\n");
            sb.append("    \"mode\": \"local\",\n");
            sb.append("    \"port\": 18789,\n");
            sb.append("    \"bind\": \"lan\",\n");
            sb.append("    \"auth\": { \"mode\": \"token\", \"token\": \"").append(gatewayToken).append("\" }\n");
            sb.append("  }\n");
            sb.append("}");
            Files.write(new File(baseDir + "/.openclaw/openclaw.json").toPath(), sb.toString().getBytes());

            // 代理
            StringBuilder proxy = new StringBuilder();
            proxy.append("const http=require('http'),httpProxy=require('http-proxy');\n");
            proxy.append("const p=httpProxy.createProxyServer({ws:true});\n");
            proxy.append("p.on('error',(e,q,r)=>{if(r&&r.writeHead){r.writeHead(503);r.end();}});\n");
            proxy.append("http.createServer((q,r)=>p.web(q,r,{target:q.headers.host?.startsWith('5.')?'http://127.0.0.1:18789':'http://127.0.0.1:5678'})).on('upgrade',(q,s,h)=>p.ws(q,s,h,{target:q.headers.host?.startsWith('5.')?'ws://127.0.0.1:18789':'ws://127.0.0.1:5678'})).listen(30196,'0.0.0.0',()=>console.log('Proxy:30196'));\n");
            Files.write(new File(baseDir + "/proxy.js").toPath(), proxy.toString().getBytes());

            new File(baseDir + "/.n8n").mkdirs();

            System.out.println("\n🚀 启动服务...");
            
            ProcessBuilder n8n = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", baseDir + "/node_modules/.bin/n8n", "start");
            n8n.environment().putAll(env);
            n8n.environment().put("N8N_PORT", "5678");
            n8n.environment().put("N8N_HOST", "0.0.0.0");
            n8n.environment().put("N8N_SECURE_COOKIE", "false");
            n8n.environment().put("N8N_USER_FOLDER", baseDir + "/.n8n");
            n8n.directory(new File(baseDir));
            n8n.inheritIO();
            n8n.start();

            ProcessBuilder gw = new ProcessBuilder(nodeBin, ocBin, "gateway", "--port", "18789", "--bind", "lan", "--token", gatewayToken, "--verbose");
            gw.environment().putAll(env);
            gw.directory(new File(baseDir));
            gw.inheritIO();
            gw.start();

            Thread.sleep(15000);

            ProcessBuilder px = new ProcessBuilder(nodeBin, baseDir + "/proxy.js");
            px.environment().putAll(env);
            px.directory(new File(baseDir));
            px.inheritIO();
            px.start().waitFor();

        } catch (Exception e) { e.printStackTrace(); }
    }
    
    static void findJsFiles(File dir, List<String> result) {
        if (!dir.exists() || !dir.isDirectory()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                // 跳过某些目录
                String name = file.getName();
                if (!name.equals("node_modules") && !name.startsWith(".")) {
                    findJsFiles(file, result);
                }
            } else {
                String name = file.getName();
                if (name.endsWith(".js") || name.endsWith(".mjs") || name.endsWith(".cjs")) {
                    result.add(file.getAbsolutePath());
                }
            }
        }
    }

    static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) { if (f.isDirectory()) deleteDirectory(f); else f.delete(); }
        dir.delete();
    }
}

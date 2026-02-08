package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 配置中 (直接修改 SDK 版)...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            String apiKey = "sk-g4f-token-any";
            String zeaburUrl = "https://888888888888.zeabur.app/v1";
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String gatewayToken = "admin123";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("OPENAI_API_KEY", apiKey);
            env.put("OPENAI_BASE_URL", zeaburUrl);
            env.put("PLAYWRIGHT_BROWSERS_PATH", baseDir + "/.playwright");
            env.put("TMPDIR", baseDir + "/tmp");

            // ★★★ 直接查看 OpenAI SDK 结构 ★★★
            System.out.println("📝 分析 OpenAI SDK 结构...");
            
            // 读取 package.json 找入口
            File pkgJson = new File(baseDir + "/node_modules/openai/package.json");
            if (pkgJson.exists()) {
                String pkg = new String(Files.readAllBytes(pkgJson.toPath()));
                System.out.println("  package.json 存在");
                // 查找 main 字段
                if (pkg.contains("\"main\"")) {
                    int idx = pkg.indexOf("\"main\"");
                    System.out.println("  main 字段: " + pkg.substring(idx, Math.min(idx + 50, pkg.length())));
                }
            }

            // 列出 openai 目录
            System.out.println("\n📋 OpenAI SDK 目录结构:");
            ProcessBuilder lsPb = new ProcessBuilder("ls", "-la", baseDir + "/node_modules/openai/");
            lsPb.inheritIO();
            lsPb.start().waitFor();

            // ★★★ 搜索默认 URL 设置 ★★★
            System.out.println("\n📝 搜索默认 baseURL 设置...");
            ProcessBuilder grepPb = new ProcessBuilder("sh", "-c",
                "grep -rn 'api.openai.com' " + baseDir + "/node_modules/openai/ 2>/dev/null | head -30"
            );
            grepPb.inheritIO();
            grepPb.start().waitFor();

            // ★★★ 直接修改找到的文件 ★★★
            System.out.println("\n📝 修改 OpenAI SDK 文件...");
            
            // 遍历 openai 目录下所有文件
            int modified = modifyFilesRecursive(new File(baseDir + "/node_modules/openai"), zeaburUrl);
            System.out.println("  ✓ 修改了 " + modified + " 个文件");

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

    static int modifyFilesRecursive(File dir, String zeaburUrl) {
        int count = 0;
        if (!dir.exists() || !dir.isDirectory()) return 0;
        
        File[] files = dir.listFiles();
        if (files == null) return 0;
        
        for (File file : files) {
            if (file.isDirectory()) {
                count += modifyFilesRecursive(file, zeaburUrl);
            } else {
                String name = file.getName();
                if (name.endsWith(".js") || name.endsWith(".mjs") || name.endsWith(".cjs") || name.endsWith(".ts")) {
                    try {
                        if (file.length() > 5 * 1024 * 1024) continue;
                        
                        byte[] bytes = Files.readAllBytes(file.toPath());
                        String content = new String(bytes);
                        
                        if (content.contains("api.openai.com")) {
                            String newContent = content
                                .replace("https://api.openai.com/v1", zeaburUrl)
                                .replace("https://api.openai.com", zeaburUrl.replace("/v1", ""))
                                .replace("api.openai.com", "888888888888.zeabur.app");
                            Files.write(file.toPath(), newContent.getBytes());
                            count++;
                            System.out.println("    ✓ " + file.getPath());
                        }
                    } catch (Exception e) {
                        // 忽略
                    }
                }
            }
        }
        return count;
    }

    static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) { if (f.isDirectory()) deleteDirectory(f); else f.delete(); }
        dir.delete();
    }
}

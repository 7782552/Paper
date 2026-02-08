package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 配置中 (SDK 修改版)...");
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

            // ★★★ 查找 OpenAI SDK 中的默认 URL ★★★
            System.out.println("📝 搜索 OpenAI SDK 默认 URL...");
            ProcessBuilder grepPb = new ProcessBuilder("sh", "-c",
                "grep -rn 'openai.com\\|DEFAULT.*URL\\|baseURL' " + baseDir + "/node_modules/openai/ 2>/dev/null | head -50"
            );
            grepPb.inheritIO();
            grepPb.start().waitFor();

            // ★★★ 修改 OpenAI SDK 核心文件 ★★★
            System.out.println("\n📝 修改 OpenAI SDK 核心文件...");
            
            String[] coreFiles = {
                baseDir + "/node_modules/openai/index.js",
                baseDir + "/node_modules/openai/index.mjs",
                baseDir + "/node_modules/openai/core.js",
                baseDir + "/node_modules/openai/core.mjs",
                baseDir + "/node_modules/openai/client.js",
                baseDir + "/node_modules/openai/client.mjs",
                baseDir + "/node_modules/openai/dist/index.js",
                baseDir + "/node_modules/openai/dist/index.mjs"
            };
            
            for (String filePath : coreFiles) {
                File file = new File(filePath);
                if (file.exists()) {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    System.out.println("  检查: " + filePath.substring(baseDir.length()));
                    
                    // 查找各种可能的 URL 模式
                    if (content.contains("api.openai.com") || 
                        content.contains("openai.com") ||
                        content.contains("DEFAULT_BASE_URL") ||
                        content.contains("baseURL")) {
                        System.out.println("    -> 包含相关内容");
                    }
                }
            }

            // ★★★ 创建一个包装脚本来覆盖 OpenAI 默认设置 ★★★
            System.out.println("\n📝 创建 OpenAI SDK 覆盖脚本...");
            
            StringBuilder override = new StringBuilder();
            override.append("// 覆盖 OpenAI SDK 默认设置\n");
            override.append("const originalOpenAI = require('openai');\n");
            override.append("const OriginalClient = originalOpenAI.OpenAI || originalOpenAI.default?.OpenAI || originalOpenAI;\n");
            override.append("\n");
            override.append("class PatchedOpenAI extends OriginalClient {\n");
            override.append("  constructor(opts = {}) {\n");
            override.append("    super({\n");
            override.append("      ...opts,\n");
            override.append("      baseURL: opts.baseURL || '").append(zeaburUrl).append("',\n");
            override.append("      apiKey: opts.apiKey || '").append(apiKey).append("'\n");
            override.append("    });\n");
            override.append("    console.log('[OpenAI Patch] Using baseURL:', this.baseURL);\n");
            override.append("  }\n");
            override.append("}\n");
            override.append("\n");
            override.append("module.exports = { OpenAI: PatchedOpenAI, default: { OpenAI: PatchedOpenAI } };\n");
            override.append("module.exports.OpenAI = PatchedOpenAI;\n");
            
            Files.write(new File(baseDir + "/openai-override.js").toPath(), override.toString().getBytes());

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

            // ★★★ 使用 NODE_OPTIONS 来预加载覆盖脚本 ★★★
            ProcessBuilder gw = new ProcessBuilder(nodeBin, ocBin, "gateway", "--port", "18789", "--bind", "lan", "--token", gatewayToken, "--verbose");
            gw.environment().putAll(env);
            gw.environment().put("NODE_OPTIONS", "--require " + baseDir + "/openai-override.js");
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

    static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) { if (f.isDirectory()) deleteDirectory(f); else f.delete(); }
        dir.delete();
    }
}

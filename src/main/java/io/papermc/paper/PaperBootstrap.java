package io.papermc.paper;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 配置中 (API 转换版)...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            String apiKey = "sk-g4f-token-any";
            String zeaburUrl = "https://888888888888.zeabur.app";
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String gatewayToken = "admin123";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("PLAYWRIGHT_BROWSERS_PATH", baseDir + "/.playwright");
            env.put("TMPDIR", baseDir + "/tmp");

            // ★★★ 创建 API 转换代理 (关键修复) ★★★
            System.out.println("📝 创建 API 转换代理...");
            StringBuilder apiProxy = new StringBuilder();
            apiProxy.append("const http = require('http');\n");
            apiProxy.append("const https = require('https');\n");
            apiProxy.append("const { URL } = require('url');\n\n");
            
            apiProxy.append("const UPSTREAM = '").append(zeaburUrl).append("';\n");
            apiProxy.append("const API_KEY = '").append(apiKey).append("';\n\n");
            
            apiProxy.append("const server = http.createServer(async (req, res) => {\n");
            apiProxy.append("  console.log(`[API Proxy] ${req.method} ${req.url}`);\n\n");
            
            apiProxy.append("  let body = '';\n");
            apiProxy.append("  req.on('data', chunk => body += chunk);\n");
            apiProxy.append("  req.on('end', async () => {\n");
            apiProxy.append("    try {\n");
            
            // 处理 /v1/responses 端点 - 转换为 /v1/chat/completions
            apiProxy.append("      let targetPath = req.url;\n");
            apiProxy.append("      let requestBody = body ? JSON.parse(body) : {};\n");
            apiProxy.append("      let isResponsesAPI = req.url.includes('/responses');\n\n");
            
            apiProxy.append("      if (isResponsesAPI && req.method === 'POST') {\n");
            apiProxy.append("        console.log('[API Proxy] 转换 Responses API -> Chat Completions API');\n");
            apiProxy.append("        targetPath = '/v1/chat/completions';\n\n");
            
            // 转换请求格式
            apiProxy.append("        const messages = [];\n");
            apiProxy.append("        if (requestBody.instructions) {\n");
            apiProxy.append("          messages.push({ role: 'system', content: requestBody.instructions });\n");
            apiProxy.append("        }\n");
            apiProxy.append("        if (requestBody.input) {\n");
            apiProxy.append("          if (typeof requestBody.input === 'string') {\n");
            apiProxy.append("            messages.push({ role: 'user', content: requestBody.input });\n");
            apiProxy.append("          } else if (Array.isArray(requestBody.input)) {\n");
            apiProxy.append("            for (const item of requestBody.input) {\n");
            apiProxy.append("              if (typeof item === 'string') {\n");
            apiProxy.append("                messages.push({ role: 'user', content: item });\n");
            apiProxy.append("              } else if (item.role && item.content) {\n");
            apiProxy.append("                messages.push({ role: item.role, content: typeof item.content === 'string' ? item.content : JSON.stringify(item.content) });\n");
            apiProxy.append("              } else if (item.type === 'message' && item.content) {\n");
            apiProxy.append("                const textContent = Array.isArray(item.content) ? item.content.filter(c => c.type === 'input_text' || c.type === 'text').map(c => c.text).join('') : item.content;\n");
            apiProxy.append("                messages.push({ role: item.role || 'user', content: textContent });\n");
            apiProxy.append("              }\n");
            apiProxy.append("            }\n");
            apiProxy.append("          }\n");
            apiProxy.append("        }\n\n");
            
            apiProxy.append("        if (messages.length === 0) {\n");
            apiProxy.append("          messages.push({ role: 'user', content: 'Hello' });\n");
            apiProxy.append("        }\n\n");
            
            apiProxy.append("        let modelName = requestBody.model || 'gpt-4o-mini';\n");
            apiProxy.append("        modelName = modelName.replace('openai/', '').replace('anthropic/', '');\n\n");
            
            apiProxy.append("        requestBody = {\n");
            apiProxy.append("          model: modelName,\n");
            apiProxy.append("          messages: messages,\n");
            apiProxy.append("          stream: requestBody.stream || false,\n");
            apiProxy.append("          max_tokens: requestBody.max_output_tokens || 4096,\n");
            apiProxy.append("          temperature: requestBody.temperature || 0.7\n");
            apiProxy.append("        };\n");
            apiProxy.append("        console.log('[API Proxy] 转换后请求:', JSON.stringify(requestBody, null, 2).substring(0, 500));\n");
            apiProxy.append("      }\n\n");
            
            // 发送请求到上游
            apiProxy.append("      const upstreamUrl = new URL(targetPath, UPSTREAM);\n");
            apiProxy.append("      const options = {\n");
            apiProxy.append("        hostname: upstreamUrl.hostname,\n");
            apiProxy.append("        port: 443,\n");
            apiProxy.append("        path: upstreamUrl.pathname + upstreamUrl.search,\n");
            apiProxy.append("        method: req.method,\n");
            apiProxy.append("        headers: {\n");
            apiProxy.append("          'Content-Type': 'application/json',\n");
            apiProxy.append("          'Authorization': 'Bearer ' + API_KEY,\n");
            apiProxy.append("          'Accept': req.headers.accept || 'application/json'\n");
            apiProxy.append("        }\n");
            apiProxy.append("      };\n\n");
            
            apiProxy.append("      const proxyReq = https.request(options, (proxyRes) => {\n");
            apiProxy.append("        console.log(`[API Proxy] 上游响应: ${proxyRes.statusCode}`);\n\n");
            
            // 处理流式响应
            apiProxy.append("        if (requestBody.stream && isResponsesAPI) {\n");
            apiProxy.append("          res.writeHead(200, {\n");
            apiProxy.append("            'Content-Type': 'text/event-stream',\n");
            apiProxy.append("            'Cache-Control': 'no-cache',\n");
            apiProxy.append("            'Connection': 'keep-alive'\n");
            apiProxy.append("          });\n\n");
            
            apiProxy.append("          let buffer = '';\n");
            apiProxy.append("          proxyRes.on('data', (chunk) => {\n");
            apiProxy.append("            buffer += chunk.toString();\n");
            apiProxy.append("            const lines = buffer.split('\\n');\n");
            apiProxy.append("            buffer = lines.pop() || '';\n");
            apiProxy.append("            for (const line of lines) {\n");
            apiProxy.append("              if (line.startsWith('data: ')) {\n");
            apiProxy.append("                const data = line.substring(6);\n");
            apiProxy.append("                if (data === '[DONE]') {\n");
            apiProxy.append("                  res.write('data: {\"type\":\"response.completed\"}\\n\\n');\n");
            apiProxy.append("                } else {\n");
            apiProxy.append("                  try {\n");
            apiProxy.append("                    const parsed = JSON.parse(data);\n");
            apiProxy.append("                    const content = parsed.choices?.[0]?.delta?.content || '';\n");
            apiProxy.append("                    if (content) {\n");
            apiProxy.append("                      res.write(`data: {\"type\":\"response.output_text.delta\",\"delta\":\"${content.replace(/\"/g, '\\\\\"').replace(/\\n/g, '\\\\n')}\"}\\n\\n`);\n");
            apiProxy.append("                    }\n");
            apiProxy.append("                  } catch (e) {}\n");
            apiProxy.append("                }\n");
            apiProxy.append("              }\n");
            apiProxy.append("            }\n");
            apiProxy.append("          });\n");
            apiProxy.append("          proxyRes.on('end', () => res.end());\n");
            
            apiProxy.append("        } else if (isResponsesAPI) {\n");
            // 非流式响应转换
            apiProxy.append("          let responseData = '';\n");
            apiProxy.append("          proxyRes.on('data', chunk => responseData += chunk);\n");
            apiProxy.append("          proxyRes.on('end', () => {\n");
            apiProxy.append("            try {\n");
            apiProxy.append("              const parsed = JSON.parse(responseData);\n");
            apiProxy.append("              console.log('[API Proxy] 上游响应内容:', responseData.substring(0, 300));\n");
            apiProxy.append("              const content = parsed.choices?.[0]?.message?.content || parsed.error?.message || 'No response';\n");
            apiProxy.append("              const responseId = 'resp_' + Date.now();\n");
            apiProxy.append("              const responsesFormat = {\n");
            apiProxy.append("                id: responseId,\n");
            apiProxy.append("                object: 'response',\n");
            apiProxy.append("                created_at: Math.floor(Date.now() / 1000),\n");
            apiProxy.append("                status: 'completed',\n");
            apiProxy.append("                output: [{\n");
            apiProxy.append("                  type: 'message',\n");
            apiProxy.append("                  id: 'msg_' + Date.now(),\n");
            apiProxy.append("                  role: 'assistant',\n");
            apiProxy.append("                  content: [{ type: 'output_text', text: content }]\n");
            apiProxy.append("                }],\n");
            apiProxy.append("                usage: parsed.usage || { input_tokens: 0, output_tokens: 0 }\n");
            apiProxy.append("              };\n");
            apiProxy.append("              res.writeHead(200, { 'Content-Type': 'application/json' });\n");
            apiProxy.append("              res.end(JSON.stringify(responsesFormat));\n");
            apiProxy.append("            } catch (e) {\n");
            apiProxy.append("              console.error('[API Proxy] 解析错误:', e, responseData.substring(0, 200));\n");
            apiProxy.append("              res.writeHead(500);\n");
            apiProxy.append("              res.end(JSON.stringify({ error: e.message }));\n");
            apiProxy.append("            }\n");
            apiProxy.append("          });\n");
            
            apiProxy.append("        } else {\n");
            // 直接转发其他请求
            apiProxy.append("          res.writeHead(proxyRes.statusCode, proxyRes.headers);\n");
            apiProxy.append("          proxyRes.pipe(res);\n");
            apiProxy.append("        }\n");
            apiProxy.append("      });\n\n");
            
            apiProxy.append("      proxyReq.on('error', (e) => {\n");
            apiProxy.append("        console.error('[API Proxy] 请求错误:', e);\n");
            apiProxy.append("        res.writeHead(503);\n");
            apiProxy.append("        res.end(JSON.stringify({ error: e.message }));\n");
            apiProxy.append("      });\n\n");
            
            apiProxy.append("      if (body && req.method !== 'GET') {\n");
            apiProxy.append("        proxyReq.write(JSON.stringify(requestBody));\n");
            apiProxy.append("      }\n");
            apiProxy.append("      proxyReq.end();\n\n");
            
            apiProxy.append("    } catch (e) {\n");
            apiProxy.append("      console.error('[API Proxy] 处理错误:', e);\n");
            apiProxy.append("      res.writeHead(500);\n");
            apiProxy.append("      res.end(JSON.stringify({ error: e.message }));\n");
            apiProxy.append("    }\n");
            apiProxy.append("  });\n");
            apiProxy.append("});\n\n");
            
            apiProxy.append("server.listen(19999, '127.0.0.1', () => {\n");
            apiProxy.append("  console.log('[API Proxy] 监听 127.0.0.1:19999 (转换 Responses API -> Chat Completions)');\n");
            apiProxy.append("});\n");
            
            Files.write(new File(baseDir + "/api-proxy.js").toPath(), apiProxy.toString().getBytes());

            // ★★★ OpenClaw 使用本地代理 ★★★
            env.put("OPENAI_API_KEY", apiKey);
            env.put("OPENAI_BASE_URL", "http://127.0.0.1:19999/v1");

            // 删除 Webhook
            System.out.println("📝 删除 Telegram Webhook...");
            try {
                java.net.URL url = new java.net.URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook?drop_pending_updates=true");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                int code = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(code >= 400 ? conn.getErrorStream() : conn.getInputStream()));
                String line, resp = "";
                while ((line = br.readLine()) != null) resp += line;
                System.out.println("  Webhook 删除结果: " + code + " " + resp);
            } catch (Exception e) {
                System.out.println("  Webhook 删除失败: " + e.getMessage());
            }

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
            sb.append("      \"model\": { \"primary\": \"gpt-4o-mini\" },\n");
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

            // 主代理
            StringBuilder proxy = new StringBuilder();
            proxy.append("const http=require('http'),httpProxy=require('http-proxy');\n");
            proxy.append("const p=httpProxy.createProxyServer({ws:true});\n");
            proxy.append("p.on('error',(e,q,r)=>{console.error('Proxy error:',e.message);if(r&&r.writeHead){r.writeHead(503);r.end();}});\n");
            proxy.append("http.createServer((q,r)=>{\n");
            proxy.append("  const target=q.headers.host?.startsWith('5.')?'http://127.0.0.1:18789':'http://127.0.0.1:5678';\n");
            proxy.append("  console.log(`[Proxy] ${q.method} ${q.url} -> ${target}`);\n");
            proxy.append("  p.web(q,r,{target});\n");
            proxy.append("}).on('upgrade',(q,s,h)=>{\n");
            proxy.append("  const target=q.headers.host?.startsWith('5.')?'ws://127.0.0.1:18789':'ws://127.0.0.1:5678';\n");
            proxy.append("  p.ws(q,s,h,{target});\n");
            proxy.append("}).listen(30196,'0.0.0.0',()=>console.log('[Proxy] 监听 30196'));\n");
            Files.write(new File(baseDir + "/proxy.js").toPath(), proxy.toString().getBytes());

            new File(baseDir + "/.n8n").mkdirs();

            System.out.println("\n🚀 启动服务...");
            
            // 1. 启动 API 转换代理
            System.out.println("  启动 API 转换代理...");
            ProcessBuilder apiProxyPb = new ProcessBuilder(nodeBin, baseDir + "/api-proxy.js");
            apiProxyPb.environment().putAll(env);
            apiProxyPb.directory(new File(baseDir));
            apiProxyPb.inheritIO();
            apiProxyPb.start();
            Thread.sleep(2000);

            // 2. 启动 n8n
            System.out.println("  启动 n8n...");
            ProcessBuilder n8n = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", baseDir + "/node_modules/.bin/n8n", "start");
            Map<String, String> n8nEnv = new HashMap<>(env);
            n8nEnv.put("N8N_PORT", "5678");
            n8nEnv.put("N8N_HOST", "0.0.0.0");
            

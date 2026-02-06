package io.papermc.paper;

import java.io.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🧪 测试容器网络出口...\n");
        
        String[] tests = {
            "curl -s -m 10 https://www.google.com -o /dev/null -w '%{http_code}'",
            "curl -s -m 10 https://api.ipify.org",
            "curl -s -m 10 https://www.youtube.com -o /dev/null -w '%{http_code}'",
            "ping -c 3 8.8.8.8"
        };
        
        String[] names = {
            "Google",
            "获取出口IP",
            "YouTube", 
            "Ping 8.8.8.8"
        };
        
        for (int i = 0; i < tests.length; i++) {
            System.out.println("测试 " + names[i] + "...");
            try {
                ProcessBuilder pb = new ProcessBuilder("sh", "-c", tests[i]);
                pb.inheritIO();
                int code = pb.start().waitFor();
                System.out.println("退出码: " + code + "\n");
            } catch (Exception e) {
                System.out.println("失败: " + e.getMessage() + "\n");
            }
        }
        
        System.out.println("测试完成！");
        
        // 保持运行
        try { Thread.sleep(60000); } catch (Exception e) {}
    }
}

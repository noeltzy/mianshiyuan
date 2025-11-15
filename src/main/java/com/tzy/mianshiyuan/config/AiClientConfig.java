package com.tzy.mianshiyuan.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiClientConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultOptions(
                DashScopeChatOptions.builder().withModel("qwen-flash").withTopP(0.7).build()
        ).build();
    }
}


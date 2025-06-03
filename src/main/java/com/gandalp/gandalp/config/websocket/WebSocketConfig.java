package com.gandalp.gandalp.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker // stomp 사용
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final StompHandler stompHandler;


	@Override // WebSocket 메시지 브로커를 구성 (즉 중간역할을 하는 거임. 어떤 신호가 오면 그걸 구독하는 client 에게 신호(?) 를 주는 것 )
				// configureMessageBroker 의 경우 서버가 클라이언트에게 메시지를 보낼 수 있도록 구성하는 것.
	public void configureMessageBroker(MessageBrokerRegistry registry) {


		// 특정 목적지 경로로 들어오는 메시지를, 구독중인 클라이언트에게 브로드캐스트한다.
		registry.enableSimpleBroker("/topic", "/queue");

		// 특정 목적지로 메시지를 보낼 수 있도록 한다.
		registry.setApplicationDestinationPrefixes("/publish");

		registry.setUserDestinationPrefix("/user");

	}


	// 클라이언트의 엔드포인트를 등록하고 SockJs 를 사용하도록 설정한다.
	// registerStompEndPoints 의 경우, 클라리언트로부터 서버가 메시지를 받을 수 있도록 엔드포인트를 설정하는 부분이다.
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {

		registry.addEndpoint("/connect")
			.setAllowedOrigins("https://www.gandalp-service.com")
			.withSockJS();
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(stompHandler);
	}

}

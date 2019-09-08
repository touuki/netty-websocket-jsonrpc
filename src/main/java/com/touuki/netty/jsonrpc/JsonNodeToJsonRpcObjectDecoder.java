package com.touuki.netty.jsonrpc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.touuki.netty.jsonrpc.JsonRpcException;
import com.touuki.netty.jsonrpc.JsonRpcObject;
import com.touuki.netty.jsonrpc.JsonRpcRequest;
import com.touuki.netty.jsonrpc.JsonRpcResponse;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;

@Sharable
public class JsonNodeToJsonRpcObjectDecoder extends MessageToMessageDecoder<JsonNode> {
	private static final Logger log = LoggerFactory.getLogger(JsonNodeToJsonRpcObjectDecoder.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void decode(ChannelHandlerContext ctx, JsonNode msg, List<Object> out) {
		if (msg.has("method")) {
			try {
				out.add(objectMapper.treeToValue(msg, JsonRpcRequest.class));
			} catch (JsonProcessingException e) {
				throw JsonRpcException.INVALID_REQUEST;
			}
		} else if (msg.has("error") || msg.has("result")) {
			try {
				out.add(objectMapper.treeToValue(msg, JsonRpcResponse.class));
			} catch (JsonProcessingException e) {
				log.warn("Invalid response received: channel:{}; remoteAddress:{}; cause:{}",
						ctx.channel().id().asLongText(), ctx.channel().remoteAddress(), e.toString());
			}
		} else {
			throw JsonRpcException.INVALID_REQUEST;
		}
	}
}
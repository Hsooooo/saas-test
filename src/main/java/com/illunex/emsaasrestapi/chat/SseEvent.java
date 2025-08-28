package com.illunex.emsaasrestapi.chat;

public sealed interface SseEvent permits SseEvent.ToolProgress, SseEvent.AssistantDelta {
    record ToolProgress(String toolType, String payloadJson) implements SseEvent {}
    record AssistantDelta(String text) implements SseEvent {}
}
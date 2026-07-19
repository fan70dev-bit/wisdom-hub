export interface AiChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  traceId?: string
}

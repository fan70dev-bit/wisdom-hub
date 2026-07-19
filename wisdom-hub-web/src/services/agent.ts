import request from '@/utils/request'

export type AgentRole = 'user' | 'assistant'

export interface AgentChatRequest {
  message: string
  conversationId?: string
  provider?: string
}

export interface AgentChatResponse {
  answer: string
  traceId: string
  provider: string
  model: string
  modelAvailable: boolean
}

interface ApiEnvelope<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export const sendAgentMessage = async (payload: AgentChatRequest): Promise<AgentChatResponse> => {
  const response = await request.post<ApiEnvelope<AgentChatResponse>>('/agent/chat', payload)
  return response.data.data
}

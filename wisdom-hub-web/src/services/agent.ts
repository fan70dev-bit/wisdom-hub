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

export interface AgentStreamOptions {
  signal: AbortSignal
  onToken: (token: string) => void
}

export const streamAgentMessage = async (
  payload: AgentChatRequest,
  options: AgentStreamOptions
): Promise<void> => {
  const token = localStorage.getItem('token')
  const response = await fetch('/api/agent/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'text/event-stream',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(payload),
    signal: options.signal
  })

  if (!response.ok) {
    throw new Error(`Agent stream failed: ${response.status}`)
  }
  if (!response.body) {
    throw new Error('Agent stream response body is empty')
  }

  console.debug('[Agent Stream] response.headers', Array.from(response.headers.entries()))

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { value, done } = await reader.read()
    console.debug('[Agent Stream] reader.read()', { done, value })
    if (done) {
      break
    }
    const decoded = decoder.decode(value, { stream: true })
    console.debug('[Agent Stream] decoded chunk', decoded)
    buffer += decoded
    buffer = consumeSseBuffer(buffer, options.onToken)
  }

  const finalDecoded = decoder.decode()
  console.debug('[Agent Stream] final decoded chunk', finalDecoded)
  buffer += finalDecoded
  consumeSseBuffer(`${buffer}\n\n`, options.onToken)
}

const consumeSseBuffer = (buffer: string, onToken: (token: string) => void): string => {
  const normalized = buffer.replace(/\r\n/g, '\n')
  const events = normalized.split('\n\n')
  const rest = events.pop() || ''

  for (const event of events) {
    const rawData = event
      .split('\n')
      .filter(line => line.startsWith('data:'))
      .map(line => line.slice(5).replace(/^ /, ''))
      .join('\n')
    console.debug('[Agent Stream] parsed data', rawData)

    const chunk = normalizeSseData(rawData)
    if (chunk) {
      console.debug('[Agent Stream] callback chunk', chunk)
      onToken(chunk)
    }
  }

  return rest
}

const normalizeSseData = (data: string): string => {
  if (!data) {
    return ''
  }

  try {
    const parsed = JSON.parse(data) as unknown
    if (typeof parsed === 'string') {
      return parsed
    }
    if (parsed && typeof parsed === 'object') {
      const record = parsed as Record<string, unknown>
      const content = record.content ?? record.text ?? record.answer ?? record.data
      return typeof content === 'string' ? content : data
    }
  } catch {
    return data
  }

  return data
}

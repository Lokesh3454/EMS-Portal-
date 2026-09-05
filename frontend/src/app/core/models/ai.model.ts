export interface AiChatMessage {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
  timestamp: string;
  intent?: string;
  actions?: string[];
  suggestions?: string[];
}

export interface AiResponse {
  answer: string;
  intent: string;
  actionLinks: string[];
  suggestions: string[];
  contextData?: any;
}

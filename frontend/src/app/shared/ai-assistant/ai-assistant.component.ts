import { Component, OnInit, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { Router } from '@angular/router';
import { AiService } from '../../core/services/ai.service';
import { AuthService } from '../../core/services/auth.service';
import { AiChatMessage } from '../../core/models/ai.model';

@Component({
  selector: 'app-ai-assistant',
  templateUrl: './ai-assistant.component.html',
  styleUrls: ['./ai-assistant.component.css']
})
export class AiAssistantComponent implements OnInit, AfterViewChecked {
  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  isOpen = false;
  inputText = '';
  isThinking = false;
  messages: AiChatMessage[] = [];

  quickSuggestions: string[] = [
    'How many casual and sick leaves do I have remaining?',
    'Explain the tax deductions and allowances on my payslip',
    'What is the company probation and notice period policy?',
    'How do I view and sign my employment documents?',
    'What open job positions and candidates are in our ATS?'
  ];

  currentUser: any = null;

  constructor(
    private aiService: AiService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    const name = this.currentUser?.fullName || this.currentUser?.name || 'there';

    // Welcome greeting
    this.messages.push({
      id: 'welcome-msg',
      sender: 'assistant',
      text: `Hello ${name}! 👋 I am your **EMS Virtual HR Copilot**.\n\nI can answer enterprise policy questions, check your live leave balances, explain your payslip deductions, navigate the document vault, or provide workforce metrics. How may I assist you today?`,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      suggestions: [
        'Check my leave balance',
        'Breakdown my salary & payslip',
        'Company Work from Home & Leave policy',
        'Open Document Vault'
      ]
    });
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  toggleChat(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      setTimeout(() => this.scrollToBottom(), 100);
    }
  }

  closeChat(): void {
    this.isOpen = false;
  }

  sendMessage(textToSend?: string): void {
    const text = (textToSend || this.inputText).trim();
    if (!text || this.isThinking) return;

    // Add user message
    const userMsg: AiChatMessage = {
      id: 'user-' + Date.now(),
      sender: 'user',
      text: text,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };
    this.messages.push(userMsg);
    this.inputText = '';
    this.isThinking = true;

    // Call AI Backend Service
    this.aiService.askAssistant(text, window.location.pathname).subscribe({
      next: (res) => {
        this.isThinking = false;
        if (res.success && res.data) {
          const aiData = res.data;
          this.messages.push({
            id: 'ai-' + Date.now(),
            sender: 'assistant',
            text: aiData.answer,
            timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
            intent: aiData.intent,
            actions: aiData.actionLinks || [],
            suggestions: aiData.suggestions || []
          });
        }
      },
      error: (err) => {
        this.isThinking = false;
        this.messages.push({
          id: 'err-' + Date.now(),
          sender: 'assistant',
          text: `I apologize, I encountered a temporary connection issue reaching the workforce knowledge base. Please try asking again. (${err.error?.message || 'Server error'})`,
          timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        });
      }
    });
  }

  navigateAction(actionUrl: string): void {
    if (actionUrl.startsWith('/')) {
      this.router.navigateByUrl(actionUrl);
    } else {
      this.router.navigate([actionUrl]);
    }
    // Mobile or small screen comfort
    if (window.innerWidth < 640) {
      this.isOpen = false;
    }
  }

  clearChat(): void {
    this.messages = [];
    this.ngOnInit();
  }

  private scrollToBottom(): void {
    try {
      if (this.scrollContainer) {
        this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
      }
    } catch (err) {}
  }
}

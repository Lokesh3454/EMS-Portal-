import { Component, OnInit, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { Router } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
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
  copiedMsgId: string | null = null;

  quickSuggestions: string[] = [
    'How many casual and sick leaves do I have remaining?',
    'Explain the tax deductions and allowances on my payslip',
    'What is the company probation and notice period policy?',
    'How do I view and sign my employment documents?',
    'What are the official corporate holidays in 2026?'
  ];

  currentUser: any = null;

  constructor(
    private aiService: AiService,
    private authService: AuthService,
    private router: Router,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    const name = this.currentUser?.fullName || this.currentUser?.name || 'there';

    // Tailor quick suggestions based on user role
    this.quickSuggestions = [
      'How many casual and sick leaves do I have remaining?',
      'Explain the tax deductions and allowances on my payslip',
      'What is the company probation and notice period policy?',
      'How do I view and sign my employment documents?',
      'What are the official corporate holidays in 2026?'
    ];

    if (this.authService.canAccessManagerPortal()) {
      this.quickSuggestions.push('What open job positions and candidates are in our ATS?');
    }

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
    if (actionUrl === '/recruitment' && !this.authService.canAccessManagerPortal()) {
      this.router.navigate(['/documents']);
      return;
    }
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

  getActionLabel(action: string): string {
    const clean = (action || '').replace(/^\//, '').toLowerCase();
    switch (clean) {
      case 'leaves': return 'Open Leave Management';
      case 'payroll': return 'View Payslip & Payroll';
      case 'performance': return 'Open Performance Reviews';
      case 'attendance': return 'View Attendance Shifts';
      case 'documents': return 'Open Document Vault';
      case 'recruitment': return 'Go to Recruitment ATS';
      case 'dashboard': return 'Go to Dashboard';
      default: return `Navigate to ${clean.toUpperCase()}`;
    }
  }

  getActionIcon(action: string): string {
    const clean = (action || '').replace(/^\//, '').toLowerCase();
    switch (clean) {
      case 'leaves': return 'bi bi-calendar-check-fill text-emerald';
      case 'payroll': return 'bi bi-wallet2 text-blue';
      case 'performance': return 'bi bi-award-fill text-purple';
      case 'attendance': return 'bi bi-clock-history text-amber';
      case 'documents': return 'bi bi-file-earmark-lock-fill text-indigo';
      case 'recruitment': return 'bi bi-person-lines-fill text-rose';
      default: return 'bi bi-arrow-right-circle-fill text-indigo';
    }
  }

  copyMessage(msg: AiChatMessage): void {
    if (!msg?.text) return;
    const cleanText = msg.text.replace(/\*\*/g, '');
    navigator.clipboard.writeText(cleanText).then(() => {
      this.copiedMsgId = msg.id;
      setTimeout(() => {
        if (this.copiedMsgId === msg.id) {
          this.copiedMsgId = null;
        }
      }, 2000);
    }).catch(() => {});
  }

  formatMessage(text: string): SafeHtml {
    if (!text) return '';

    // 1. Escape HTML entities
    let formatted = text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');

    // 2. Bold markdown: **bold**
    formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

    // 3. Bullet list items: • item or * item
    formatted = formatted.replace(/^[ \t]*[•*]\s*(.+)$/gm, 
      '<div class="ai-bullet-row"><i class="bi bi-check-circle-fill ai-bullet-icon"></i><span>$1</span></div>');

    // 4. Numbered list items: 1. item
    formatted = formatted.replace(/^[ \t]*(\d+)\.\s*(.+)$/gm,
      '<div class="ai-bullet-row"><span class="ai-num-badge">$1</span><span>$2</span></div>');

    // 5. Clean newlines around divs and convert remaining newlines
    formatted = formatted.replace(/<\/div>\n/g, '</div>');
    formatted = formatted.replace(/\n<div/g, '<div');
    formatted = formatted.replace(/\n\n+/g, '<div class="ai-para-break"></div>');
    formatted = formatted.replace(/\n/g, '<br>');

    return this.sanitizer.bypassSecurityTrustHtml(formatted);
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

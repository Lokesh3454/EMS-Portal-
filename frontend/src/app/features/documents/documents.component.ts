import { Component, OnInit } from '@angular/core';
import { DocumentService } from '../../core/services/document.service';
import { AuthService } from '../../core/services/auth.service';
import { EmployeeDocument } from '../../core/models/document.model';

@Component({
  selector: 'app-documents',
  templateUrl: './documents.component.html',
  styleUrls: ['./documents.component.css']
})
export class DocumentsComponent implements OnInit {
  documents: EmployeeDocument[] = [];
  loading = true;
  selectedCategory = 'ALL';
  searchQuery = '';
  actionMessage = '';
  errorMessage = '';

  // Preview Modal
  previewModalDoc: EmployeeDocument | null = null;
  isSigning = false;
  signatureConsent = false;
  signerName = '';

  // Upload Modal
  showUploadModal = false;
  uploadTitle = '';
  uploadType: 'OFFER_LETTER' | 'POLICY_AGREEMENT' | 'TAX_DECLARATION' | 'ID_PROOF' | 'CERTIFICATE' | 'APPRAISAL_LETTER' = 'TAX_DECLARATION';
  uploadFileSize = '1.4 MB';
  uploadFileType = 'PDF';
  uploadContent = '';

  currentUser: any = null;

  constructor(
    private documentService: DocumentService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.signerName = this.currentUser?.fullName || this.currentUser?.name || 'Employee';
    this.loadDocuments();
  }

  getDocThumbnail(type?: string): string {
    switch (type) {
      case 'OFFER_LETTER':
      case 'APPRAISAL_LETTER':
        return 'assets/images/vault-digitally-signed.jpg';
      case 'POLICY_AGREEMENT':
      case 'ID_PROOF':
        return 'assets/images/vault-security-shield.jpg';
      case 'TAX_DECLARATION':
        return 'assets/images/vault-pending-sign.jpg';
      default:
        return 'assets/images/vault-total-docs.jpg';
    }
  }

  loadDocuments(): void {
    this.loading = true;
    this.documentService.getMyDocuments().subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success && res.data) {
          this.documents = res.data;
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Failed to load documents: ' + (err.error?.message || err.message);
      }
    });
  }

  activeKpiFilter: 'ALL' | 'SIGNED' | 'PENDING' | 'SECURITY' = 'ALL';

  filterByKpi(filter: 'ALL' | 'SIGNED' | 'PENDING' | 'SECURITY'): void {
    this.activeKpiFilter = filter;
    if (filter === 'SECURITY') {
      this.selectedCategory = 'POLICY_AGREEMENT';
    } else if (filter === 'ALL') {
      this.selectedCategory = 'ALL';
    }
  }

  setCategory(cat: string): void {
    this.selectedCategory = cat;
    if (this.activeKpiFilter === 'SECURITY' && cat !== 'POLICY_AGREEMENT') {
      this.activeKpiFilter = 'ALL';
    }
  }

  clearFilters(): void {
    this.selectedCategory = 'ALL';
    this.activeKpiFilter = 'ALL';
    this.searchQuery = '';
  }

  get filteredDocuments(): EmployeeDocument[] {
    return this.documents.filter(doc => {
      const matchesCat = this.selectedCategory === 'ALL' || doc.documentType === this.selectedCategory;

      let matchesKpi = true;
      if (this.activeKpiFilter === 'SIGNED') {
        matchesKpi = doc.isSigned;
      } else if (this.activeKpiFilter === 'PENDING') {
        matchesKpi = !doc.isSigned;
      } else if (this.activeKpiFilter === 'SECURITY') {
        matchesKpi = doc.documentType === 'POLICY_AGREEMENT';
      }

      const matchesSearch = !this.searchQuery ||
        doc.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        doc.documentType.toLowerCase().includes(this.searchQuery.toLowerCase());

      return matchesCat && matchesKpi && matchesSearch;
    });
  }

  get totalCount(): number {
    return this.documents.length;
  }

  get signedCount(): number {
    return this.documents.filter(d => d.isSigned).length;
  }

  get pendingSignatureCount(): number {
    return this.documents.filter(d => !d.isSigned).length;
  }

  openPreview(doc: EmployeeDocument): void {
    this.previewModalDoc = doc;
    this.signatureConsent = false;
  }

  closePreview(): void {
    this.previewModalDoc = null;
  }

  signCurrentDocument(): void {
    if (!this.previewModalDoc || this.previewModalDoc.isSigned) return;

    this.isSigning = true;
    this.documentService.signDocument(this.previewModalDoc.id).subscribe({
      next: (res) => {
        this.isSigning = false;
        this.actionMessage = `Document "${this.previewModalDoc?.title}" digitally signed and authenticated successfully!`;
        if (res.data) {
          const idx = this.documents.findIndex(d => d.id === this.previewModalDoc?.id);
          if (idx !== -1) {
            this.documents[idx] = res.data;
          }
          this.previewModalDoc = res.data;
        }
        setTimeout(() => this.actionMessage = '', 6000);
      },
      error: (err) => {
        this.isSigning = false;
        this.errorMessage = 'Failed to sign document: ' + (err.error?.message || err.message);
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }

  openUploadModal(): void {
    this.uploadTitle = '';
    this.uploadType = 'TAX_DECLARATION';
    this.uploadContent = '';
    this.showUploadModal = true;
  }

  closeUploadModal(): void {
    this.showUploadModal = false;
  }

  submitUpload(): void {
    if (!this.uploadTitle.trim()) {
      this.errorMessage = 'Please provide a document title';
      return;
    }

    const payload: Partial<EmployeeDocument> = {
      title: this.uploadTitle.trim(),
      documentType: this.uploadType,
      fileSize: '1.2 MB',
      fileType: 'PDF',
      documentContent: this.uploadContent || `Certified documentation for ${this.uploadTitle.trim()} uploaded securely to employee vault.`
    };

    this.documentService.uploadDocument(payload).subscribe({
      next: (res) => {
        this.showUploadModal = false;
        this.actionMessage = `Document "${payload.title}" securely uploaded to Vault!`;
        if (res.data) {
          this.documents.unshift(res.data);
        } else {
          this.loadDocuments();
        }
        setTimeout(() => this.actionMessage = '', 5000);
      },
      error: (err) => {
        this.errorMessage = 'Upload failed: ' + (err.error?.message || err.message);
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }

  printOrDownload(doc: EmployeeDocument): void {
    const printWindow = window.open('', '_blank');
    if (!printWindow) return;

    printWindow.document.write(`
      <html>
        <head>
          <title>${doc.title}</title>
          <style>
            body { font-family: 'Segoe UI', Arial, sans-serif; padding: 40px; color: #1e293b; }
            .header { border-bottom: 2px solid #3b82f6; padding-bottom: 20px; margin-bottom: 30px; }
            .badge { display: inline-block; padding: 4px 10px; border-radius: 4px; font-size: 12px; font-weight: bold; background: #e0f2fe; color: #0369a1; }
            .content { line-height: 1.8; white-space: pre-line; background: #f8fafc; padding: 25px; border: 1px solid #e2e8f0; border-radius: 8px; }
            .stamp { margin-top: 40px; border: 2px dashed #10b981; padding: 15px; border-radius: 6px; display: inline-block; color: #047857; }
          </style>
        </head>
        <body>
          <div class="header">
            <h2>🏢 EMS ENTERPRISE DOCUMENT VAULT</h2>
            <h1>${doc.title}</h1>
            <p><strong>Employee:</strong> ${doc.employeeName || 'Authorized Personnel'} &bull; <strong>Type:</strong> <span class="badge">${doc.documentType}</span></p>
          </div>
          <div class="content">${doc.documentContent || 'Document record on file.'}</div>
          ${doc.isSigned ? `<div class="stamp">✓ DIGITALLY SIGNED & VERIFIED BY ${doc.employeeName} ON ${doc.signedAt}</div>` : '<div class="stamp" style="border-color:#f59e0b; color:#b45309;">⚠️ PENDING DIGITAL E-SIGNATURE</div>'}
        </body>
      </html>
    `);
    printWindow.document.close();
    printWindow.focus();
    setTimeout(() => printWindow.print(), 500);
  }
}

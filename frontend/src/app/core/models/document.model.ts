export interface EmployeeDocument {
  id: number;
  employeeId: number;
  employeeName: string;
  title: string;
  documentType: 'OFFER_LETTER' | 'POLICY_AGREEMENT' | 'TAX_DECLARATION' | 'ID_PROOF' | 'CERTIFICATE' | 'APPRAISAL_LETTER';
  fileSize: string;
  fileType: string;
  uploadDate: string;
  isSigned: boolean;
  signedAt?: string;
  documentContent?: string;
}

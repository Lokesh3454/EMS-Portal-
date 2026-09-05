export interface JobPosting {
  id: number;
  title: string;
  departmentId: number;
  departmentName: string;
  location: string;
  employmentType: 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERN';
  experienceRequired: string;
  salaryRange: string;
  description: string;
  vacancies: number;
  status: 'ACTIVE' | 'CLOSED' | 'DRAFT';
  postedDate: string;
  applicantCount: number;
}

export interface Candidate {
  id: number;
  jobPostingId: number;
  jobTitle: string;
  departmentName: string;
  fullName: string;
  email: string;
  phone: string;
  currentCompany: string;
  experienceYears: number;
  stage: 'APPLIED' | 'SCREENING' | 'INTERVIEW' | 'OFFERED' | 'HIRED' | 'REJECTED';
  appliedDate: string;
  interviewFeedback?: string;
  rating: number;
  convertedToEmployee: boolean;
}

export interface AppraisalCycle {
  id: number;
  cycleName: string;
  period: string;
  year: number;
  status: 'UPCOMING' | 'ACTIVE' | 'IN_REVIEW' | 'COMPLETED';
  startDate: string;
  endDate: string;
}

export interface PerformanceReview {
  id: number;
  cycleId: number;
  cycleName: string;
  period: string;
  employeeId: number;
  empId: string;
  employeeName: string;
  departmentName: string;
  designation: string;
  email: string;
  managerId?: number;
  managerName?: string;
  selfScoreTechnical: number;
  selfScoreDelivery: number;
  selfScoreCollaboration: number;
  selfScoreLeadership: number;
  selfAverageScore: number;
  selfAchievements?: string;
  selfImprovements?: string;
  mgrScoreTechnical?: number;
  mgrScoreDelivery?: number;
  mgrScoreCollaboration?: number;
  mgrScoreLeadership?: number;
  mgrAverageScore?: number;
  finalRating?: number;
  managerFeedback?: string;
  recommendedIncrement?: number;
  recommendedPromotion?: boolean;
  recommendedDesignation?: string;
  status: 'DRAFT' | 'SELF_SUBMITTED' | 'MANAGER_REVIEWED' | 'COMPLETED';
  updatedAt?: string;
}

export interface EmployeeGoal {
  id: number;
  employeeId: number;
  employeeName?: string;
  title: string;
  description?: string;
  category: 'TECHNICAL' | 'DELIVERY' | 'LEADERSHIP' | 'INNOVATION';
  targetDate: string;
  progressPercentage: number;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED';
}

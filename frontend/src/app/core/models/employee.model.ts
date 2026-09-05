export interface Employee {
  id?: number;
  empId?: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  gender?: string;
  dateOfBirth?: string;
  address?: string;
  departmentId?: number;
  departmentName?: string;
  designation?: string;
  dateOfJoining?: string;
  employmentType?: string;
  status?: string;
  profilePhoto?: string;
  salary?: number;
  hireDate?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface Department {
  id?: number;
  name: string;
  description?: string;
}

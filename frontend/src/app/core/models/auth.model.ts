export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  email: string;
  role: string;
  fullName: string;
  userId: number;
  employeeId: number;
  profilePhoto: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

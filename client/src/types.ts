export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
}

export interface Project {
  id: number;
  name: string;
  description: string;
  status: string;
  jiraKey?: string;
  createdAt: string;
}

export interface ProjectRequest {
  name: string;
  description: string;
  status: string;
}

export interface Task {
  id: number;
  title: string;
  description: string;
  status: string;
  priority: string;
  deadline: string;
  jiraKey?: string;
}

export interface User {
  id: number;
  email: string;
  username: string;
}

export interface Comment {
  id: number;
  text: string;
  createdAt: string;
}

export interface TaskRequest {
  title: string;
  description: string;
  status: string;
  priority: string;
  deadline: string;
}

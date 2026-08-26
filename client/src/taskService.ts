import api from './api';
import type { Task, TaskRequest, Comment } from './types';

export const taskService = {
  getByProject: async (projectId: number): Promise<Task[]> => {
    const response = await api.get(`/projects/${projectId}/tasks`);
    return response.data;
  },
  create: async (projectId: number, data: TaskRequest): Promise<Task> => {
    const response = await api.post(`/projects/${projectId}/tasks`, data);
    return response.data;
  },
  update: async (taskId: number, data: TaskRequest): Promise<Task> => {
    const response = await api.put(`/tasks/${taskId}`, data);
    return response.data;
  },
  delete: async (taskId: number): Promise<void> => {
    await api.delete(`/tasks/${taskId}`);
  },
  getComments: async (taskId: number): Promise<Comment[]> => {
    const response = await api.get(`/tasks/${taskId}/comments`);
    return response.data;
  },
  addComment: async (taskId: number, text: string): Promise<Comment> => {
    const response = await api.post(`/tasks/${taskId}/comments`, { text });
    return response.data;
  },
};

import api from './api';
import type { Project, ProjectRequest } from './types';

export const projectService = {
  getAll: async (): Promise<Project[]> => {
    const response = await api.get('/projects');
    return response.data;
  },
  create: async (data: ProjectRequest): Promise<Project> => {
    console.log('Creating project with data:', data);
    const response = await api.post('/projects', data);
    return response.data;
  },
  update: async (id: number, data: ProjectRequest): Promise<Project> => {
    const response = await api.put(`/projects/${id}`, data);
    return response.data;
  },
  delete: async (id: number): Promise<void> => {
    await api.delete(`/projects/${id}`);
  },
  exportToJira: async (projectId: number): Promise<void> => {
    await api.post(`/jira/export/project/${projectId}`);
  },
};

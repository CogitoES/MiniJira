import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { projectService } from '../projectService';
import { useAuth } from '../useAuth';

const parseProjectError = (err: any): string => {
  const status = err.response?.status;
  const data = err.response?.data;
  const rawMsg = typeof data === 'string' ? data : (data?.message || err.message || '');
  const lowerMsg = rawMsg.toLowerCase();

  if (status === 409 || lowerMsg.includes('already exists') || lowerMsg.includes('duplicate key') || lowerMsg.includes('unique constraint')) {
    return 'A project with this name already exists';
  }
  if (status === 401) {
    return 'Session expired. Please login again.';
  }
  if (rawMsg.includes('could not execute statement') || rawMsg.includes('SQL [')) {
    return 'Failed to save project: Duplicate or invalid project details.';
  }

  return rawMsg ? `Project operation failed: ${rawMsg}` : 'An unexpected error occurred. Please try again.';
};

const EditProject = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { logout } = useAuth();
  
  const { data: projects } = useQuery({ queryKey: ['projects'], queryFn: projectService.getAll });
  const project = projects?.find(p => p.id === Number(id));

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (project) {
      setName(project.name);
      setDescription(project.description || '');
    }
  }, [project]);

  const mutation = useMutation({
    mutationFn: (data: any) => projectService.update(Number(id), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      navigate('/');
    },
    onError: (err: any) => {
      if (err.response?.status === 401) {
        logout();
        navigate('/login');
      } else {
        setError(parseProjectError(err));
      }
    }
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const trimmedName = name.trim().toLowerCase();
    const duplicate = (projects || []).find(
      (p) => p.id !== Number(id) && p.name.trim().toLowerCase() === trimmedName
    );

    if (duplicate) {
      setError('A project with this name already exists.');
      return;
    }

    mutation.mutate({ name, description, status: project?.status || 'ACTIVE' });
  };

  return (
    <div className="max-w-xl mx-auto bg-white p-8 rounded-2xl shadow-sm border border-slate-200 mt-6">
      <h2 className="text-3xl font-bold text-slate-900 mb-6">Edit Project</h2>
      {error && <div className="bg-red-50 text-red-600 p-3 mb-6 rounded-lg text-sm border border-red-200">{error}</div>}
      <form onSubmit={handleSubmit} className="space-y-6">
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-2">Project Name</label>
          <input className="border border-slate-300 rounded-lg p-3 w-full focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Enter project name" value={name} onChange={(e) => setName(e.target.value)} required />
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-2">Description</label>
          <textarea className="border border-slate-300 rounded-lg p-3 w-full h-32 focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Enter project description" value={description} onChange={(e) => setDescription(e.target.value)} />
        </div>
        <button className="bg-blue-600 text-white p-3 w-full rounded-lg font-semibold hover:bg-blue-700 transition" type="submit">Update Project</button>
      </form>
    </div>
  );
};

export default EditProject;

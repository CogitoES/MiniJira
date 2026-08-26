import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { projectService } from '../projectService';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

const CreateProject = () => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [status] = useState('ACTIVE');
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { logout } = useAuth();

  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: projectService.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      navigate('/');
    },
    onError: (error: any) => {
      if (error.response?.status === 409) {
        setError('A project with this name already exists.');
      } else if (error.response?.status === 401) {
        logout();
        navigate('/login');
      } else {
        setError('An unexpected error occurred. Please try again.');
      }
    }
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    mutation.mutate({ name, description, status });
  };

  return (
    <div className="p-4">
      <h2 className="text-2xl mb-4">Create Project</h2>
      {error && <div className="bg-red-100 text-red-700 p-2 mb-4 rounded">{error}</div>}
      <form onSubmit={handleSubmit} className="space-y-4">
        <input className="border p-2 w-full" placeholder="Name" onChange={(e) => setName(e.target.value)} />
        <textarea className="border p-2 w-full" placeholder="Description" onChange={(e) => setDescription(e.target.value)} />
        <button className="bg-blue-500 text-white p-2 w-full" type="submit">Create</button>
      </form>
    </div>
  );
};

export default CreateProject;

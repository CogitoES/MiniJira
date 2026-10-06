import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { taskService } from '../taskService';
import { useParams } from 'react-router-dom';
import { useState } from 'react';
import CommentList from './CommentList';

const parseTaskError = (err: any): string => {
  const status = err.response?.status;
  const data = err.response?.data;
  const rawMsg = typeof data === 'string' ? data : (data?.message || err.message || '');
  const lowerMsg = rawMsg.toLowerCase();

  if (status === 409 || lowerMsg.includes('already exists') || lowerMsg.includes('duplicate key') || lowerMsg.includes('unique constraint')) {
    return 'A task with this title already exists';
  }
  if (status === 401) {
    return 'Session expired. Please login again.';
  }
  if (rawMsg.includes('could not execute statement') || rawMsg.includes('SQL [')) {
    return 'Failed to save task: Duplicate or invalid task details.';
  }

  return rawMsg ? `Task operation failed: ${rawMsg}` : 'An unexpected error occurred. Please try again.';
};

const TaskList = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const [title, setTitle] = useState('');
  const [editingTaskId, setEditingTaskId] = useState<number | null>(null);
  const [editTitle, setEditTitle] = useState('');
  const [error, setError] = useState<string | null>(null);
  const queryClient = useQueryClient();

  const { data: tasks, isLoading } = useQuery({
    queryKey: ['tasks', projectId],
    queryFn: () => taskService.getByProject(Number(projectId)),
  });

  const mutation = useMutation({
    mutationFn: (data: any) => taskService.create(Number(projectId), data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
      setTitle('');
      setError(null);
    },
    onError: (err: any) => {
      setError(parseTaskError(err));
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ taskId, data }: { taskId: number; data: any }) => taskService.update(taskId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
      setEditingTaskId(null);
      setError(null);
    },
    onError: (err: any) => {
      setError(parseTaskError(err));
    },
  });

  const deleteMutation = useMutation({
    mutationFn: taskService.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
      setError(null);
    },
    onError: (err: any) => {
      setError(parseTaskError(err));
    },
  });

  const handleCreateTask = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const trimmedTitle = title.trim().toLowerCase();
    if (!trimmedTitle) return;

    const duplicate = (tasks || []).find(
      (t) => t.title.trim().toLowerCase() === trimmedTitle
    );

    if (duplicate) {
      setError('A task with this title already exists.');
      return;
    }

    mutation.mutate({ title, description: '', status: 'TODO', priority: 'MEDIUM', deadline: '' });
  };

  const handleUpdateTask = (taskId: number, currentTask: any) => {
    setError(null);
    const trimmedTitle = editTitle.trim().toLowerCase();
    if (!trimmedTitle) return;

    const duplicate = (tasks || []).find(
      (t) => t.id !== taskId && t.title.trim().toLowerCase() === trimmedTitle
    );

    if (duplicate) {
      setError('A task with this title already exists.');
      return;
    }

    updateMutation.mutate({ taskId, data: { ...currentTask, title: editTitle } });
  };

  if (isLoading) return <div className="p-6 text-slate-600">Loading tasks...</div>;

  return (
    <div>
      <h2 className="text-3xl font-bold text-slate-900 mb-8">Tasks</h2>
      {error && <div className="bg-red-50 text-red-600 p-3 mb-6 rounded-lg text-sm border border-red-200">{error}</div>}
      <form onSubmit={handleCreateTask} className="flex gap-3 mb-8">
        <input 
          className="border border-slate-300 rounded-lg p-3 flex-1 focus:ring-2 focus:ring-blue-500 outline-none" 
          placeholder="New Task Title" 
          value={title} 
          onChange={(e) => setTitle(e.target.value)} 
        />
        <button className="bg-blue-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition" type="submit">Add Task</button>
      </form>
      <ul className="grid gap-4">
        {(Array.isArray(tasks) ? tasks : []).map((task) => (
          <li key={task.id} className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
            <div className="flex justify-between items-center">
              {editingTaskId === task.id ? (
                <div className="flex gap-2 items-center flex-1 mr-4">
                  <input
                    className="border border-slate-300 rounded-lg p-2 flex-1 outline-none focus:ring-2 focus:ring-blue-500"
                    value={editTitle}
                    onChange={(e) => setEditTitle(e.target.value)}
                  />
                  <button onClick={() => handleUpdateTask(task.id, task)} className="text-green-600 font-medium hover:text-green-700">Save</button>
                  <button onClick={() => { setEditingTaskId(null); setError(null); }} className="text-slate-600 font-medium hover:text-slate-700">Cancel</button>
                </div>
              ) : (
                <span className="font-medium text-slate-900 cursor-pointer hover:text-blue-600" onClick={() => { setEditingTaskId(task.id); setEditTitle(task.title); setError(null); }}>{task.title}</span>
              )}
              <button onClick={() => deleteMutation.mutate(task.id)} className="text-red-600 hover:text-red-700 font-medium">Delete</button>
            </div>
            <CommentList taskId={task.id} />
          </li>
        ))}
      </ul>
    </div>
  );
};

export default TaskList;

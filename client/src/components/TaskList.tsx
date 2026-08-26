import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { taskService } from '../taskService';
import { useParams } from 'react-router-dom';
import { useState } from 'react';
import CommentList from './CommentList';

const TaskList = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const [title, setTitle] = useState('');
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
    },
  });

  const deleteMutation = useMutation({
    mutationFn: taskService.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks', projectId] });
    },
  });

  if (isLoading) return <div className="p-6 text-slate-600">Loading tasks...</div>;

  return (
    <div>
      <h2 className="text-3xl font-bold text-slate-900 mb-8">Tasks</h2>
      <form onSubmit={(e) => { e.preventDefault(); mutation.mutate({ title, description: '', status: 'TODO', priority: 'MEDIUM', deadline: '' }); }} className="flex gap-3 mb-8">
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
              <span className="font-medium text-slate-900">{task.title}</span>
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

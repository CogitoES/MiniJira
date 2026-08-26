import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { taskService } from '../taskService';
import { useState } from 'react';
import type { Comment } from '../types';

interface CommentListProps {
  taskId: number;
}

const CommentList = ({ taskId }: CommentListProps) => {
  const [text, setText] = useState('');
  const queryClient = useQueryClient();

  const { data: comments, isLoading } = useQuery({
    queryKey: ['comments', taskId],
    queryFn: () => taskService.getComments(taskId),
  });

  const mutation = useMutation({
    mutationFn: (text: string) => taskService.addComment(taskId, text),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['comments', taskId] });
      setText('');
    },
  });

  if (isLoading) return <div className="text-sm text-slate-500">Loading comments...</div>;

  return (
    <div className="mt-6 border-t border-slate-200 pt-4">
      <h3 className="text-lg font-semibold text-slate-900 mb-4">Comments</h3>
      <div className="space-y-4 mb-6">
        {(Array.isArray(comments) ? comments : []).map((comment: Comment) => (
          <div key={comment.id} className="bg-slate-50 p-3 rounded-lg border border-slate-100">
            <div className="flex justify-between text-xs text-slate-500 mb-1">
              <span>{new Date(comment.createdAt).toLocaleString()}</span>
            </div>
            <p className="text-slate-800">{comment.text}</p>
          </div>
        ))}
      </div>
      <form onSubmit={(e) => { e.preventDefault(); mutation.mutate(text); }} className="flex gap-2">
        <input 
          className="border border-slate-300 rounded-lg p-2 flex-1 text-sm focus:ring-2 focus:ring-blue-500 outline-none" 
          placeholder="Add a comment..." 
          value={text} 
          onChange={(e) => setText(e.target.value)} 
        />
        <button className="bg-slate-800 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-slate-900 transition" type="submit">Post</button>
      </form>
    </div>
  );
};

export default CommentList;

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { projectService } from '../projectService';
import { Link } from 'react-router-dom';

const ProjectList = () => {
  const queryClient = useQueryClient();

  const { data: projects, isLoading } = useQuery({
    queryKey: ['projects'],
    queryFn: projectService.getAll,
  });

  const deleteMutation = useMutation({
    mutationFn: projectService.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
    },
  });

  const exportMutation = useMutation({
    mutationFn: projectService.exportToJira,
    onError: () => {
      alert('Failed to export project to Jira. Please check your credentials and configuration.');
    },
    onSuccess: () => {
      alert('Project exported to Jira successfully!');
    }
  });

  if (isLoading) return <div className="p-6 text-slate-600">Loading projects...</div>;

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-3xl font-bold text-slate-900">Projects</h2>
        <Link 
          to="/projects/create" 
          className="bg-blue-600 text-white px-5 py-2.5 rounded-lg font-medium hover:bg-blue-700 transition"
        >
          Create Project
        </Link>
      </div>
      <ul className="grid gap-6">
        {(Array.isArray(projects) ? projects : []).map((project) => (
          <li key={project.id} className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm flex justify-between items-center hover:shadow-md transition">
            <Link to={`/projects/${project.id}/tasks`} className="flex-1">
              <h3 className="text-xl font-semibold text-slate-900 hover:text-blue-600">{project.name}</h3>
              <p className="text-slate-600 mt-1">{project.description}</p>
            </Link>
            <div className="flex gap-3">
              <button onClick={() => exportMutation.mutate(project.id)} className="text-purple-600 hover:text-purple-700 font-medium">Export to Jira</button>
              <Link to={`/projects/${project.id}/edit`} className="text-slate-600 hover:text-blue-600 font-medium">Edit</Link>
              <button onClick={() => deleteMutation.mutate(project.id)} className="text-red-600 hover:text-red-700 font-medium">Delete</button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default ProjectList;

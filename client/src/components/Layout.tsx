import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import { projectService } from '../projectService';
import { taskService } from '../taskService';
import { useState } from 'react';

const Layout = ({ children }: { children: React.ReactNode }) => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [isClearing, setIsClearing] = useState(false);

  const handleClearDb = async () => {
    if (!window.confirm("Are you sure you want to clear all databases? This action cannot be undone.")) {
      return;
    }

    setIsClearing(true);
    try {
      // Clear in order of dependencies: Comments -> Tasks -> Projects
      await taskService.clearCommentsDatabase();
      await taskService.clearDatabase();
      await projectService.clearDatabase();
      alert("All databases cleared successfully!");
      navigate('/');
      window.location.reload();
    } catch (error) {
      console.error("Failed to clear database:", error);
      alert("Error clearing database. Make sure all backend services are running in 'dev' profile.");
    } finally {
      setIsClearing(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="bg-white border-b border-slate-200">
        <nav className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <Link to="/" className="text-2xl font-extrabold text-blue-600">MiniJira</Link>
            <Link to="/" className="text-slate-600 hover:text-blue-600 font-medium transition">Projects</Link>
          </div>
          <div className="flex items-center gap-4">
            <button
              onClick={handleClearDb}
              disabled={isClearing}
              className="text-xs font-semibold px-3 h-8 bg-red-50 text-red-600 border border-red-200 rounded-md hover:bg-red-100 disabled:opacity-50 transition"
            >
              {isClearing ? "Clearing..." : "Clear DB (Dev)"}
            </button>
            <button 
              onClick={logout} 
              className="text-sm font-medium text-slate-500 hover:text-red-600 transition"
            >
              Logout
            </button>
          </div>
        </nav>
      </header>
      <main className="max-w-6xl mx-auto p-6">
        {children}
      </main>
    </div>
  );
};

export default Layout;

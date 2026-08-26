import { Link } from 'react-router-dom';
import { useAuth } from '../AuthContext';

const Layout = ({ children }: { children: React.ReactNode }) => {
  const { logout } = useAuth();

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900">
      <header className="bg-white border-b border-slate-200">
        <nav className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <Link to="/" className="text-2xl font-extrabold text-blue-600">MiniJira</Link>
            <Link to="/" className="text-slate-600 hover:text-blue-600 font-medium transition">Projects</Link>
          </div>
          <button 
            onClick={logout} 
            className="text-sm font-medium text-slate-500 hover:text-red-600 transition"
          >
            Logout
          </button>
        </nav>
      </header>
      <main className="max-w-6xl mx-auto p-6">
        {children}
      </main>
    </div>
  );
};

export default Layout;

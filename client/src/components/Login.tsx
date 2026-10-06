import { useState } from 'react';
import { useAuth } from '../useAuth';
import { useNavigate, Link } from 'react-router-dom';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    try {
      await login({ email, password });
      navigate('/');
    } catch (err: any) {
      const message = err.response?.data?.message || err.message || 'Invalid email or password';
      setErrorMsg(message);
    }
  };

  return (
    <div className="flex justify-center items-center h-screen bg-slate-100">
      <form onSubmit={handleSubmit} className="p-10 bg-white shadow-xl rounded-2xl w-full max-w-sm border border-slate-100">
        <h2 className="text-3xl font-bold mb-8 text-center text-slate-900">Sign In</h2>
        {errorMsg && <div className="bg-red-50 text-red-600 p-3 mb-4 rounded-lg text-sm border border-red-200">{errorMsg}</div>}
        <input className="border border-slate-300 rounded-lg p-3 w-full mb-4 focus:ring-2 focus:ring-blue-500 outline-none" type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <input className="border border-slate-300 rounded-lg p-3 w-full mb-6 focus:ring-2 focus:ring-blue-500 outline-none" type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <button className="bg-blue-600 text-white p-3 w-full rounded-lg font-semibold hover:bg-blue-700 transition mb-4" type="submit">Login</button>
        <div className="text-center">
          <Link to="/register" className="text-blue-600 font-medium hover:underline">Don't have an account? Register</Link>
        </div>
      </form>
    </div>
  );
};

export default Login;

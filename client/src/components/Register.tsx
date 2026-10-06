import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../authService';

const parseRegistrationError = (err: any): string => {
  const data = err.response?.data;
  const rawMsg = typeof data === 'string' ? data : (data?.message || err.message || '');
  const lowerMsg = rawMsg.toLowerCase();

  if (lowerMsg.includes('users_username_key') || (lowerMsg.includes('username') && (lowerMsg.includes('already exists') || lowerMsg.includes('constraint')))) {
    return 'Username already exists';
  }
  if (lowerMsg.includes('users_email_key') || (lowerMsg.includes('email') && (lowerMsg.includes('already exists') || lowerMsg.includes('constraint')))) {
    return 'Email already exists';
  }
  if (lowerMsg.includes('duplicate key') || lowerMsg.includes('unique constraint')) {
    return 'This email or username is already registered';
  }
  if (rawMsg.includes('could not execute statement') || rawMsg.includes('SQL [')) {
    return 'Registration failed: Duplicate or invalid account details.';
  }

  if (rawMsg.toLowerCase().includes('registration failed')) {
    return rawMsg;
  }

  return rawMsg ? `Registration failed: ${rawMsg}` : 'Registration failed. Please try again.';
};

const Register = () => {
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setSuccessMsg(null);
    try {
      await authService.register({ email, username, password });
      setSuccessMsg('Registration successful! Redirecting to login...');
      setTimeout(() => navigate('/login'), 1500);
    } catch (err: any) {
      console.error('Registration error:', err);
      setErrorMsg(parseRegistrationError(err));
    }
  };

  return (
    <div className="flex justify-center items-center h-screen bg-slate-100">
      <form onSubmit={handleSubmit} className="p-10 bg-white shadow-xl rounded-2xl w-full max-w-sm border border-slate-100">
        <h2 className="text-3xl font-bold mb-8 text-center text-slate-900">Register</h2>
        {errorMsg && <div className="bg-red-50 text-red-600 p-3 mb-4 rounded-lg text-sm border border-red-200">{errorMsg}</div>}
        {successMsg && <div className="bg-green-50 text-green-600 p-3 mb-4 rounded-lg text-sm border border-green-200">{successMsg}</div>}
        <input className="border border-slate-300 rounded-lg p-3 w-full mb-4 focus:ring-2 focus:ring-blue-500 outline-none" type="email" placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <input className="border border-slate-300 rounded-lg p-3 w-full mb-4 focus:ring-2 focus:ring-blue-500 outline-none" type="text" placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} required />
        <input className="border border-slate-300 rounded-lg p-3 w-full mb-6 focus:ring-2 focus:ring-blue-500 outline-none" type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <button className="bg-green-600 text-white p-3 w-full rounded-lg font-semibold hover:bg-green-700 transition mb-4" type="submit">Register</button>
        <div className="text-center">
          <Link to="/login" className="text-blue-600 font-medium hover:underline">Already have an account? Login</Link>
        </div>
      </form>
    </div>
  );
};

export default Register;

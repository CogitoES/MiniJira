import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api';

const Register = () => {
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/auth/register', { email, username, password });
      alert('Registration successful');
      navigate('/login');
    } catch (error: any) {
      console.error('Registration error:', error);
      const errorMessage = error.response?.data || error.message || 'An unknown error occurred';
      alert('Registration failed: ' + errorMessage);
    }
  };

  return (
    <div className="flex justify-center items-center h-screen bg-gray-100">
      <form onSubmit={handleSubmit} className="p-8 bg-white shadow-md rounded">
        <h2 className="text-2xl mb-4">Register</h2>
        <input className="border p-2 w-full mb-2" type="email" placeholder="Email" onChange={(e) => setEmail(e.target.value)} />
        <input className="border p-2 w-full mb-2" type="text" placeholder="Username" onChange={(e) => setUsername(e.target.value)} />
        <input className="border p-2 w-full mb-4" type="password" placeholder="Password" onChange={(e) => setPassword(e.target.value)} />
        <button className="bg-green-500 text-white p-2 w-full mb-2" type="submit">Register</button>
        <Link to="/login" className="text-blue-500 underline">Already have an account? Login</Link>
      </form>
    </div>
  );
};

export default Register;

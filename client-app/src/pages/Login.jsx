import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { signInUser, registerUser } from '../firebase/auth';
import { api } from '../utils/api';

export function Login() {
    const [isLogin, setIsLogin] = useState(true);
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [name, setName] = useState('');
    const [studentId, setStudentId] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            if (isLogin) {
                await signInUser(email, password);
                navigate('/');
            } else {
                const sid = studentId.trim().toUpperCase();
                const fullName = name.trim();
                if (!sid || sid.length !== 8) {
                    setError('Student ID is required (N or T + 7 digits).');
                    setLoading(false);
                    return;
                }
                if (!fullName) {
                    setError('Full name is required.');
                    setLoading(false);
                    return;
                }
                await registerUser(email, password);
                await api.registerStudent({ studentId: sid, name: fullName, email: email.trim() });
                navigate('/');
            }
        } catch (err) {
            setError(err.message || 'Authentication failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-100 py-12 px-4">
            <div className="w-full max-w-md bg-white rounded-lg border border-gray-200 shadow-sm p-8">
                <h1 className="text-2xl font-semibold text-ntu-dark text-center mb-1">
                    Student Event Booking
                </h1>
                <p className="text-center text-ntu-tundora text-sm mb-6">
                    {isLogin ? 'Sign in to your account' : 'Create a new account'}
                </p>

                <div className="flex border-b border-gray-200 mb-6">
                    <button
                        type="button"
                        onClick={() => setIsLogin(true)}
                        className={`flex-1 py-2 text-sm font-medium ${isLogin ? 'text-ntu-rose border-b-2 border-ntu-rose' : 'text-ntu-tundora'}`}
                    >
                        Login
                    </button>
                    <button
                        type="button"
                        onClick={() => setIsLogin(false)}
                        className={`flex-1 py-2 text-sm font-medium ${!isLogin ? 'text-ntu-rose border-b-2 border-ntu-rose' : 'text-ntu-tundora'}`}
                    >
                        Register
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4">
                    {!isLogin && (
                        <>
                            <div>
                                <label htmlFor="studentId" className="block text-sm font-medium text-ntu-dark mb-1">
                                    Student ID
                                </label>
                                <input
                                    id="studentId"
                                    value={studentId}
                                    onChange={(e) => setStudentId(e.target.value.toUpperCase())}
                                    placeholder="e.g. N1234567"
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                                    required={!isLogin}
                                    disabled={loading}
                                />
                            </div>
                            <div>
                                <label htmlFor="name" className="block text-sm font-medium text-ntu-dark mb-1">
                                    Full Name
                                </label>
                                <input
                                    id="name"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    placeholder="Please enter your full name as shown on your NTU ID card"
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                                    required={!isLogin}
                                    disabled={loading}
                                />
                            </div>
                        </>
                    )}
                    <div>
                        <label htmlFor="email" className="block text-sm font-medium text-ntu-dark mb-1">
                            Email
                        </label>
                        <input
                            id="email"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                            required
                            disabled={loading}
                        />
                    </div>
                    <div>
                        <label htmlFor="password" className="block text-sm font-medium text-ntu-dark mb-1">
                            Password
                        </label>
                        <input
                            id="password"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md text-ntu-dark"
                            required
                            minLength={6}
                            disabled={loading}
                        />
                    </div>
                    {error && <p className="text-sm text-red-600">{error}</p>}
                    <button
                        type="submit"
                        className="w-full py-2.5 bg-ntu-rose text-white font-medium rounded-md hover:bg-ntu-pink disabled:opacity-50"
                        disabled={loading}
                    >
                        {isLogin ? (loading ? 'Signing in...' : 'Sign In') : (loading ? 'Creating account...' : 'Create Account')}
                    </button>
                </form>
            </div>
        </div>
    );
}
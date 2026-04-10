import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { getCurrentUser, signOutUser, onAuthStateChange } from '../firebase/auth';
import {api} from '../utils/api.js';

export function Navbar() {
    const location = useLocation();
    const navigate = useNavigate();
    const [user, setUser] = useState(getCurrentUser());
    const [student, setStudent] = useState(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const unsubscribe = onAuthStateChange((u) => setUser(u));
        return unsubscribe;
    }, []);

    useEffect(() => {
        if (!user) {
            setStudent(null);
            return;
        }
        api.getCurrentStudent()
            .then((data) => setStudent(data))
            .catch(() => setStudent(null));
    }, [user]);

    const isActive = (path) => location.pathname === path;

    const linkClass = (path) =>
        [
            'px-3 py-2',
            'text-white font-bold',
            'no-underline',
            'hover:underline hover:underline-offset-4 hover:decoration-2 hover:decoration-white',
            'hover:text-white',
            isActive(path) ? 'underline underline-offset-4 decoration-2 decoration-white' : '',
        ].join(' ');

    const handleLogout = async () => {
        setLoading(true);
        try {
            await signOutUser();
            navigate('/login');
        } catch (err) {
            console.error('Logout error:', err);
        } finally {
            setLoading(false);
        }
    };

    const welcomeText = student
        ? `${student.name} (${student.studentId})`
        : (user ? 'Welcome' : null);

    return (
        <nav className="sticky top-0 z-50 bg-ntu-pink border-b border-gray-200 shadow-sm">
            <div className="mx-auto max-w-7xl px-4 sm:px-6">
                <div className="flex items-center justify-between h-16">
                    <Link
                        to="/"
                        className="font-semibold text-lg text-white hover:text-white no-underline"
                    >

                    </Link>

                    <div className="flex items-center gap-4">
                        <Link to="/" className={linkClass('/')}>
                            Browse Events
                        </Link>

                        <Link to="/events-archive" className={linkClass('/events-archive')}>
                            Events Archive
                        </Link>

                        <Link to="/create-event" className={linkClass('/create-event')}>
                            Create Event
                        </Link>

                        <Link to="/manage-events" className={linkClass('/manage-events')}>
                            Manage Events
                        </Link>

                        {user ? (
                            <div className="flex items-center gap-3">
                                {welcomeText && (
                                    <span className="text-sm text-white">{welcomeText}</span>
                                )}
                                <button
                                    type="button"
                                    onClick={handleLogout}
                                    disabled={loading}
                                    className={[
                                        'text-sm text-white font-bold',
                                        'no-underline',
                                        'hover:underline hover:underline-offset-4 hover:decoration-2 hover:decoration-white',
                                        'hover:text-white',
                                        'disabled:opacity-50 disabled:cursor-not-allowed',
                                    ].join(' ')}
                                >
                                    {loading ? 'Logging out...' : 'Logout'}
                                </button>
                            </div>
                        ) : (
                            <Link to="/login" className="px-4 py-2 text-sm border border-white/30 rounded-md text-white">
                                Login
                            </Link>
                        )}
                    </div>
                </div>
            </div>
        </nav>
    );
}
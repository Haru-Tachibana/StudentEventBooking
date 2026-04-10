import { Navigate, useLocation } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { getCurrentUser, onAuthStateChange } from '../firebase/auth';

export function AuthGuard({ children }) {
    const [user, setUser] = useState(getCurrentUser());
    const [checking, setChecking] = useState(true);
    const location = useLocation();

    useEffect(() => {
        const unsubscribe = onAuthStateChange((u) => {
            setUser(u);
            setChecking(false);
        });

        return unsubscribe;
    }, []);

    if (checking) {
        return (
            <div className="flex items-center justify-center min-h-[40vh] text-ntu-tundora">
                Loading...
            </div>
        );
    }

    if (!user) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    return children;
}
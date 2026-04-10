import { Outlet } from 'react-router-dom';
import { Navbar } from './Navbar';

export function Layout() {
    return (
        <div className="min-h-screen bg-gray-100">
            <Navbar />
            <main className="mx-auto max-w-5xl px-4 sm:px-5 py-6">
                <Outlet />
            </main>
        </div>
    );
}
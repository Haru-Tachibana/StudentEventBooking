import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/Layout';
import { AuthGuard } from './components/AuthGuard';
import { Login } from './pages/Login';
import { Home } from './pages/Home';
import { CreateEvent } from './pages/CreateEvent';
import { EventDetails } from './pages/EventDetails';
import { EditEvent } from './pages/EditEvent';
import { ManageMyEvents } from './pages/ManageMyEvents';
import { EventsArchive } from './pages/EventsArchive';

export default function App() {
    return (
        <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={<Layout />}>
                <Route index element={<AuthGuard><Home /></AuthGuard>} />
                <Route path="events-archive" element={<AuthGuard><EventsArchive /></AuthGuard>} />
                <Route path="create-event" element={<AuthGuard><CreateEvent /></AuthGuard>} />
                <Route path="manage-events" element={<AuthGuard><ManageMyEvents /></AuthGuard>} />
                <Route path="event/:eventId" element={<AuthGuard><EventDetails /></AuthGuard>} />
                <Route path="event/:eventId/edit" element={<AuthGuard><EditEvent /></AuthGuard>} />
            </Route>
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}
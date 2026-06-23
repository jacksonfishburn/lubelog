import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AppStateProvider } from './context/AppState';
import { AppShell } from './components/layout/AppShell';
import { DashboardPage } from './pages/DashboardPage';
import { FleetPage } from './pages/FleetPage';
import { CockpitPage } from './pages/CockpitPage';
import { ServiceLogPage } from './pages/ServiceLogPage';
import { ServiceTypesPage } from './pages/ServiceTypesPage';

export default function App() {
  return (
    <BrowserRouter>
      <AppStateProvider>
        <Routes>
          <Route element={<AppShell />}>
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="fleet" element={<FleetPage />} />
            <Route path="fleet/:vehicleId" element={<CockpitPage />} />
            <Route path="log" element={<ServiceLogPage />} />
            <Route path="service-types" element={<ServiceTypesPage />} />
          </Route>
        </Routes>
      </AppStateProvider>
    </BrowserRouter>
  );
}

import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AppStateProvider } from './context/AppState';
import { AppShell } from './components/layout/AppShell';
import { DashboardPage } from './pages/DashboardPage';
import { FleetPage } from './pages/FleetPage';
import { CockpitPage } from './pages/CockpitPage';
import { ServiceTypesPage } from './pages/ServiceTypesPage';
import { AiFindPartsPage } from './pages/AiFindPartsPage';

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
            <Route path="service-types" element={<ServiceTypesPage />} />
            <Route path="ai-find-parts" element={<AiFindPartsPage />} />
          </Route>
        </Routes>
      </AppStateProvider>
    </BrowserRouter>
  );
}

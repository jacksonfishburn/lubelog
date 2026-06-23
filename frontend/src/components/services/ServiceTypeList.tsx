import type { ServiceType } from '../../types';
import { Button } from '../shared/Button';

interface ServiceTypeListProps {
  serviceTypes: ServiceType[];
  onDelete: (type: ServiceType) => void;
}

// Globals (seeded) render with a GLOBAL tag and no delete; custom types are
// deletable. Mirrors the backend rule against deleting globals.
export function ServiceTypeList({ serviceTypes, onDelete }: ServiceTypeListProps) {
  return (
    <div>
      {serviceTypes.map((t) => (
        <div className="svc-row" key={t.id}>
          <div className="svc-row__main">
            <div className="svc-row__name">
              {t.name}
              <span className={`tag ${t.isGlobal ? 'tag--global' : 'tag--custom'}`}>
                {t.isGlobal ? 'Global' : 'Custom'}
              </span>
            </div>
          </div>
          <div className="svc-row__actions">
            {t.isGlobal ? (
              <span className="muted" style={{ fontSize: 11 }}>seeded default</span>
            ) : (
              <Button size="sm" variant="ghost" onClick={() => onDelete(t)}>
                ✕ Delete
              </Button>
            )}
          </div>
        </div>
      ))}
    </div>
  );
}

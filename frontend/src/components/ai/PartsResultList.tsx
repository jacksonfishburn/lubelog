import type { ServicePart } from '../../types';

interface PartsResultListProps {
  parts: ServicePart[];
}

export function PartsResultList({ parts }: PartsResultListProps) {
  return (
    <ul className="parts-list">
      {parts.map((part) => (
        <li key={part.url} className="parts-list__item">
          <a
            className="parts-list__title"
            href={part.url}
            target="_blank"
            rel="noopener noreferrer"
          >
            {part.title}
          </a>
          {part.description && (
            <p className="parts-list__desc">{part.description}</p>
          )}
          <a
            className="parts-list__url"
            href={part.url}
            target="_blank"
            rel="noopener noreferrer"
          >
            {part.url}
          </a>
        </li>
      ))}
    </ul>
  );
}

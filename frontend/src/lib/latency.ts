// Simulates network round-trip latency so the UI exercises real loading states.
// When the mock layer is replaced by fetch wrappers, this goes away entirely.
export function delay<T>(value: T, min = 100, max = 300): Promise<T> {
  const ms = Math.floor(min + Math.random() * (max - min));
  return new Promise((resolve) => setTimeout(() => resolve(value), ms));
}

// Same, but for an operation that "fails" — rejects after a latency window.
export function delayReject(error: Error, min = 100, max = 300): Promise<never> {
  const ms = Math.floor(min + Math.random() * (max - min));
  return new Promise((_resolve, reject) => setTimeout(() => reject(error), ms));
}

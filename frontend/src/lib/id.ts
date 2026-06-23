// UUID generator for newly created mock records, matching the backend's
// gen_random_uuid() primary keys. The real API assigns these server-side;
// here we mint them client-side so created records have stable identity.
export function newId(): string {
  return crypto.randomUUID();
}

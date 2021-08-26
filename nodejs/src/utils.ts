export function getEnvironmentVariable(
  name: string,
  defaultValue?: string
): string {
  const value = process.env[name] || defaultValue;
  if (typeof value === "string") {
    return value;
  }
  if (typeof defaultValue === "string") {
    return defaultValue;
  }
  throw new Error(`Environment variable ${name} is not set.`);
}

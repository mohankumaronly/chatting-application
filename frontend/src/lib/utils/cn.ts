/**
 * Tiny classname merger — safer than template strings.
 * Handles conditional classes.
 */
export function cn(...classes: (string | false | null | undefined)[]): string {
  return classes.filter(Boolean).join(" ");
}
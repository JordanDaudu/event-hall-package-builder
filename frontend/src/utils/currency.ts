/**
 * Format a number as Israeli Shekel (ILS).
 * e.g.  5000 → "₪5,000"
 */
export function formatILS(amount: number): string {
    return new Intl.NumberFormat("he-IL", {
        style: "currency",
        currency: "ILS",
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
    }).format(amount);
}

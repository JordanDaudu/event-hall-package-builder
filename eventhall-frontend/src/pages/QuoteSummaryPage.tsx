import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getQuoteById } from "../api/quoteApi";
import type { QuoteResponse } from "../types/api";

function QuoteSummaryPage() {
    const { id } = useParams();

    const [quote, setQuote] = useState<QuoteResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function loadQuote() {
            if (!id) {
                setError("Quote ID is missing.");
                setLoading(false);
                return;
            }

            try {
                const quoteData = await getQuoteById(Number(id));
                setQuote(quoteData);
            } catch {
                setError("Failed to load quote.");
            } finally {
                setLoading(false);
            }
        }

        loadQuote();
    }, [id]);

    if (loading) {
        return <p>Loading quote...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    if (!quote) {
        return <p>Quote not found.</p>;
    }

    return (
        <div>
            <h1>Quote Summary</h1>

            <p>
                <strong>Quote ID:</strong> {quote.id}
            </p>

            <h2>Customer Details</h2>

            <p>
                <strong>Customer Name:</strong> {quote.customerName}
            </p>

            <p>
                <strong>Customer Email:</strong> {quote.customerEmail}
            </p>

            <p>
                <strong>Phone:</strong> {quote.customerPhoneNumber}
            </p>

            <h2>Event Details</h2>

            <p>
                <strong>Event Type:</strong> {quote.eventTypeName}
            </p>

            <p>
                <strong>Guest Count:</strong> {quote.guestCount}
            </p>

            <p>
                <strong>Status:</strong> {quote.status}
            </p>

            <h2>Selected Upgrades</h2>

            {quote.upgrades.length === 0 ? (
                <p>No upgrades selected.</p>
            ) : (
                <ul>
                    {quote.upgrades.map((upgrade) => (
                        <li key={upgrade}>{upgrade}</li>
                    ))}
                </ul>
            )}

            <h2>Total Price</h2>
            <p>{quote.totalPrice}</p>
        </div>
    );
}

export default QuoteSummaryPage;
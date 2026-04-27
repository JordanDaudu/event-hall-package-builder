import { useEffect, useState } from "react";
import { getAllQuotes, updateQuoteStatus } from "../api/adminQuoteApi";
import type { QuoteResponse } from "../types/api";

type StatusFilter = QuoteResponse["status"] | "ALL";

function AdminQuotesPage() {
    const [quotes, setQuotes] = useState<QuoteResponse[]>([]);
    const [statusFilter, setStatusFilter] = useState<StatusFilter>("ALL");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function loadQuotes() {
            setLoading(true);

            const data =
                statusFilter === "ALL"
                    ? await getAllQuotes()
                    : await getAllQuotes(statusFilter);

            setQuotes(data);
            setLoading(false);
        }

        loadQuotes();
    }, [statusFilter]);

    async function handleStatusChange(
        id: number,
        newStatus: QuoteResponse["status"]
    ) {
        const updated = await updateQuoteStatus(id, newStatus);

        setQuotes((prev) => prev.map((q) => (q.id === id ? updated : q)));
    }

    return (
        <div>
            <h1>Admin Quotes</h1>

            <label>
                Filter by status:{" "}
                <select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value as StatusFilter)}
                >
                    <option value="ALL">ALL</option>
                    <option value="NEW">NEW</option>
                    <option value="CONTACTED">CONTACTED</option>
                    <option value="APPROVED">APPROVED</option>
                    <option value="REJECTED">REJECTED</option>
                </select>
            </label>

            {loading ? (
                <p>Loading quotes...</p>
            ) : quotes.length === 0 ? (
                <p>No quotes found.</p>
            ) : (
                quotes.map((quote) => (
                    <div
                        key={quote.id}
                        style={{ border: "1px solid gray", margin: "10px", padding: "10px" }}
                    >
                        <p>
                            <strong>ID:</strong> {quote.id}
                        </p>
                        <p>
                            <strong>Name:</strong> {quote.customerName}
                        </p>
                        <p>
                            <strong>Email:</strong> {quote.customerEmail}
                        </p>
                        <p>
                            <strong>Phone:</strong> {quote.customerPhoneNumber || "N/A"}
                        </p>
                        <p>
                            <strong>Event:</strong> {quote.eventTypeName}
                        </p>
                        <p>
                            <strong>Guests:</strong> {quote.guestCount}
                        </p>
                        <p>
                            <strong>Total:</strong> {quote.totalPrice}
                        </p>

                        <select
                            value={quote.status}
                            onChange={(e) =>
                                handleStatusChange(
                                    quote.id,
                                    e.target.value as QuoteResponse["status"]
                                )
                            }
                        >
                            <option value="NEW">NEW</option>
                            <option value="CONTACTED">CONTACTED</option>
                            <option value="APPROVED">APPROVED</option>
                            <option value="REJECTED">REJECTED</option>
                        </select>
                    </div>
                ))
            )}
        </div>
    );
}

export default AdminQuotesPage;
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
        <main className="page">
            <h1>Admin Quotes</h1>

            <section className="card">
                <label>
                    Filter by status{" "}
                    <select
                        className="select"
                        value={statusFilter}
                        onChange={(event) =>
                            setStatusFilter(event.target.value as StatusFilter)
                        }
                    >
                        <option value="ALL">ALL</option>
                        <option value="NEW">NEW</option>
                        <option value="CONTACTED">CONTACTED</option>
                        <option value="APPROVED">APPROVED</option>
                        <option value="REJECTED">REJECTED</option>
                    </select>
                </label>
            </section>

            {loading ? (
                <p>Loading quotes...</p>
            ) : quotes.length === 0 ? (
                <section className="card">
                    <p>No quotes found.</p>
                </section>
            ) : (
                quotes.map((quote) => (
                    <section key={quote.id} className="card">
                        <p>
                            <strong>ID:</strong> {quote.id}
                        </p>

                        <h2>Customer</h2>

                        <p>
                            <strong>Name:</strong> {quote.customerName}
                        </p>
                        <p>
                            <strong>Email:</strong> {quote.customerEmail}
                        </p>
                        <p>
                            <strong>Phone:</strong> {quote.customerPhoneNumber || "N/A"}
                        </p>

                        <h2>Event</h2>

                        <p>
                            <strong>Event:</strong> {quote.eventTypeName}
                        </p>
                        <p>
                            <strong>Guests:</strong> {quote.guestCount}
                        </p>
                        <p>
                            <strong>Total:</strong> {quote.totalPrice}
                        </p>

                        <p>
                            <strong>Status:</strong>{" "}
                            <span className={`badge badge-${quote.status.toLowerCase()}`}>
                            {quote.status}
                        </span>
                        </p>

                        <select
                            className="select"
                            value={quote.status}
                            onChange={(event) =>
                                handleStatusChange(
                                    quote.id,
                                    event.target.value as QuoteResponse["status"]
                                )
                            }
                        >
                            <option value="NEW">NEW</option>
                            <option value="CONTACTED">CONTACTED</option>
                            <option value="APPROVED">APPROVED</option>
                            <option value="REJECTED">REJECTED</option>
                        </select>
                    </section>
                ))
            )}
        </main>
    );
}

export default AdminQuotesPage;
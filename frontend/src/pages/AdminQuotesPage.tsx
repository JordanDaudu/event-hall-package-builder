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

        setQuotes((prev) => prev.map((quote) => (quote.id === id ? updated : quote)));
    }

    const totalRevenue = quotes.reduce((sum, quote) => sum + quote.totalPrice, 0);

    const newQuotesCount = quotes.filter((quote) => quote.status === "NEW").length;
    const approvedQuotesCount = quotes.filter(
        (quote) => quote.status === "APPROVED"
    ).length;

    return (
        <main className="page">
            <div className="page-header">
                <div>
                    <h1>Admin Quotes</h1>
                    <p>View customer quote requests and manage their statuses.</p>
                </div>

                <span className="badge">{quotes.length} quotes</span>
            </div>

            <section className="stat-row">
                <div className="stat-card">
                    <span>Total Quotes</span>
                    <strong>{quotes.length}</strong>
                </div>

                <div className="stat-card">
                    <span>New Quotes</span>
                    <strong>{newQuotesCount}</strong>
                </div>

                <div className="stat-card">
                    <span>Approved Quotes</span>
                    <strong>{approvedQuotesCount}</strong>
                </div>

                <div className="stat-card">
                    <span>Visible Total Value</span>
                    <strong>{totalRevenue.toLocaleString()}</strong>
                </div>
            </section>

            <section className="card">
                <div className="toolbar">
                    <label>
                        Filter by status
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
                </div>
            </section>

            {loading ? (
                <section className="card">
                    <p>Loading quotes...</p>
                </section>
            ) : quotes.length === 0 ? (
                <section className="card">
                    <p>No quotes found.</p>
                </section>
            ) : (
                <section className="card table-card">
                    <table className="data-table">
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Customer</th>
                            <th>Contact</th>
                            <th>Event</th>
                            <th>Upgrades</th>
                            <th>Guests</th>
                            <th>Total</th>
                            <th>Status</th>
                            <th>Update</th>
                        </tr>
                        </thead>

                        <tbody>
                        {quotes.map((quote) => (
                            <tr key={quote.id}>
                                <td>#{quote.id}</td>

                                <td>
                                    <strong>{quote.customerName}</strong>
                                </td>

                                <td>
                                    <div>{quote.customerEmail}</div>
                                    <div>{quote.customerPhoneNumber || "N/A"}</div>
                                </td>

                                <td>{quote.eventTypeName}</td>

                                <td>
                                    {quote.upgrades.length === 0 ? (
                                        <span className="muted">None</span>
                                    ) : (
                                        quote.upgrades.map((upgrade) => (
                                            <span key={upgrade} className="badge" style={{ marginRight: "6px" }}>
                                                {upgrade}
                                            </span>
                                        ))
                                    )}
                                </td>

                                <td>{quote.guestCount}</td>

                                <td>{quote.totalPrice.toLocaleString()}</td>

                                <td>
                    <span className={`badge badge-${quote.status.toLowerCase()}`}>
                      {quote.status}
                    </span>
                                </td>

                                <td>
                                    <select
                                        className="select table-select"
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
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </section>
            )}
        </main>
    );
}

export default AdminQuotesPage;
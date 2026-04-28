import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getEventTypes } from "../api/eventTypeApi";
import { getUpgrades } from "../api/upgradeApi";
import { createQuote } from "../api/quoteApi";
import type { EventType, Upgrade } from "../types/api";
import HallPreview from "../components/HallPreview";

function PackageBuilderPage() {
    const navigate = useNavigate();

    const [eventTypes, setEventTypes] = useState<EventType[]>([]);
    const [upgrades, setUpgrades] = useState<Upgrade[]>([]);
    const [customerName, setCustomerName] = useState("");
    const [customerEmail, setCustomerEmail] = useState("");
    const [customerPhoneNumber, setCustomerPhoneNumber] = useState("");

    const [selectedEventTypeId, setSelectedEventTypeId] = useState<number | "">("");
    const [guestCount, setGuestCount] = useState<number>(1);
    const [selectedUpgradeIds, setSelectedUpgradeIds] = useState<number[]>([]);
    const [categoryFilter, setCategoryFilter] = useState<string>("ALL");

    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function loadBuilderData() {
            try {
                const [eventTypeData, upgradeData] = await Promise.all([
                    getEventTypes(),
                    getUpgrades(),
                ]);

                setEventTypes(eventTypeData);
                setUpgrades(upgradeData);
            } catch {
                setError("Failed to load package builder data.");
            } finally {
                setLoading(false);
            }
        }

        loadBuilderData();
    }, []);

    const categories = Array.from(
        new Set(upgrades.map((upgrade) => upgrade.category))
    );

    const visibleUpgrades = upgrades.filter(
        (upgrade) => categoryFilter === "ALL" || upgrade.category === categoryFilter
    );

    const selectedEventType = eventTypes.find(
        (eventType) => eventType.id === selectedEventTypeId
    );

    const selectedUpgrades = upgrades.filter((upgrade) =>
        selectedUpgradeIds.includes(upgrade.id)
    );

    const estimatedTotal = useMemo(() => {
        const baseTotal = selectedEventType
            ? selectedEventType.basePrice * guestCount
            : 0;

        const upgradesTotal = selectedUpgrades.reduce(
            (sum, upgrade) => sum + upgrade.price,
            0
        );

        return baseTotal + upgradesTotal;
    }, [selectedEventType, guestCount, selectedUpgrades]);

    function toggleUpgrade(upgradeId: number) {
        setSelectedUpgradeIds((currentIds) =>
            currentIds.includes(upgradeId)
                ? currentIds.filter((id) => id !== upgradeId)
                : [...currentIds, upgradeId]
        );
    }

    async function handleSubmit() {
        if (!selectedEventTypeId || guestCount < 1) {
            alert("Please choose an event type and enter a valid guest count.");
            return;
        }

        if (!customerName || !customerEmail || !customerPhoneNumber) {
            alert("Please enter all customer details.");
            return;
        }

        try {
            setSubmitting(true);

            const quote = await createQuote({
                eventTypeId: selectedEventTypeId,
                guestCount,
                upgradeIds: selectedUpgradeIds,
                customerName,
                customerEmail,
                customerPhoneNumber,
            });

            navigate(`/quote/${quote.id}`);
        } catch {
            alert("Failed to create quote.");
        } finally {
            setSubmitting(false);
        }
    }

    if (loading) {
        return <p>Loading package builder...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    return (
        <main className="page">
            <h1>Package Builder</h1>

            <section className="card">
                <h2>Event Details</h2>

                <label>
                    Event Type
                    <select
                        className="select"
                        value={selectedEventTypeId}
                        onChange={(event) =>
                            setSelectedEventTypeId(Number(event.target.value))
                        }
                    >
                        <option value="">Select event type</option>

                        {eventTypes.map((eventType) => (
                            <option key={eventType.id} value={eventType.id}>
                                {eventType.name} — {eventType.basePrice} per guest
                            </option>
                        ))}
                    </select>
                </label>

                <label>
                    Guest Count
                    <input
                        className="input"
                        type="number"
                        min="1"
                        value={guestCount}
                        onChange={(event) => setGuestCount(Number(event.target.value))}
                    />
                </label>
            </section>

            <section className="card">
                <h2>Choose Upgrades</h2>

                <label>
                    Filter by Category
                    <select
                        className="select"
                        value={categoryFilter}
                        onChange={(event) => setCategoryFilter(event.target.value)}
                    >
                        <option value="ALL">ALL</option>

                        {categories.map((category) => (
                            <option key={category} value={category}>
                                {category}
                            </option>
                        ))}
                    </select>
                </label>

                {visibleUpgrades.map((upgrade) => (
                    <label key={upgrade.id} style={{ display: "block", margin: "8px 0" }}>
                        <input
                            type="checkbox"
                            checked={selectedUpgradeIds.includes(upgrade.id)}
                            onChange={() => toggleUpgrade(upgrade.id)}
                        />
                        {" "}
                        {upgrade.name} — {upgrade.price.toLocaleString()}
                    </label>
                ))}
            </section>

            <HallPreview
                selectedEventType={selectedEventType}
                selectedUpgrades={selectedUpgrades}
            />

            <section className="card">
                <h2>Customer Details</h2>

                <input
                    className="input"
                    type="text"
                    placeholder="Your Name"
                    value={customerName}
                    onChange={(event) => setCustomerName(event.target.value)}
                />

                <input
                    className="input"
                    type="email"
                    placeholder="Your Email"
                    value={customerEmail}
                    onChange={(event) => setCustomerEmail(event.target.value)}
                />

                <input
                    className="input"
                    type="text"
                    placeholder="Phone Number"
                    value={customerPhoneNumber}
                    onChange={(event) => setCustomerPhoneNumber(event.target.value)}
                />
            </section>

            <section className="card">
                <h2>Estimated Total</h2>
                <p>
                    <strong>{estimatedTotal.toLocaleString()}</strong>
                </p>

                <button className="button" onClick={handleSubmit} disabled={submitting}>
                    {submitting ? "Submitting..." : "Submit Quote"}
                </button>
            </section>
        </main>
    );
}

export default PackageBuilderPage;
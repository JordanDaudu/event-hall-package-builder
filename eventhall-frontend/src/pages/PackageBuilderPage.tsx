import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getEventTypes } from "../api/eventTypeApi";
import { getUpgrades } from "../api/upgradeApi";
import { createQuote } from "../api/quoteApi";
import type { EventType, Upgrade } from "../types/api";

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
                customerName: customerName,
                customerEmail: customerEmail,
                customerPhoneNumber: customerPhoneNumber
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
        <div>
            <h1>Package Builder Page</h1>

            <section>
                <h2>Choose Event Type</h2>

                <select
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
            </section>

            <section>
                <h2>Guest Count</h2>

                <input
                    type="number"
                    min="1"
                    value={guestCount}
                    onChange={(event) => setGuestCount(Number(event.target.value))}
                />
            </section>

            <section>
                <h2>Choose Upgrades</h2>

                {upgrades.map((upgrade) => (
                    <label key={upgrade.id} style={{ display: "block" }}>
                        <input
                            type="checkbox"
                            checked={selectedUpgradeIds.includes(upgrade.id)}
                            onChange={() => toggleUpgrade(upgrade.id)}
                        />
                        {upgrade.name} — {upgrade.price}
                    </label>
                ))}
            </section>

            <section>
                <h2>Estimated Total</h2>
                <p>{estimatedTotal}</p>
            </section>

            <section>
                <h2>Customer Details</h2>

                <input
                    type="text"
                    placeholder="Your Name"
                    value={customerName}
                    onChange={(event) => setCustomerName(event.target.value)}
                />

                <br />

                <input
                    type="email"
                    placeholder="Your Email"
                    value={customerEmail}
                    onChange={(event) => setCustomerEmail(event.target.value)}
                />

                <br />

                <input
                    type="text"
                    placeholder="Phone Number"
                    value={customerPhoneNumber}
                    onChange={(e) => setCustomerPhoneNumber(e.target.value)}
                />
            </section>

            <button onClick={handleSubmit} disabled={submitting}>
                {submitting ? "Submitting..." : "Submit Quote"}
            </button>
        </div>
    );
}

export default PackageBuilderPage;
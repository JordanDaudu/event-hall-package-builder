import { useEffect, useState } from "react";
import {
    createUpgrade,
    getAllAdminUpgrades,
    updateUpgrade,
} from "../api/adminUpgradeApi";
import type { Upgrade } from "../types/api";

type UpgradeForm = {
    name: string;
    description: string;
    category: string;
    price: string;
};

type StatusFilter = "ALL" | "ACTIVE" | "INACTIVE";

function AdminUpgradesPage() {
    const [upgrades, setUpgrades] = useState<Upgrade[]>([]);
    const [loading, setLoading] = useState(true);

    const [categoryFilter, setCategoryFilter] = useState<string>("ALL");
    const [statusFilter, setStatusFilter] = useState<StatusFilter>("ALL");

    const [newUpgrade, setNewUpgrade] = useState<UpgradeForm>({
        name: "",
        description: "",
        category: "",
        price: "",
    });

    const [editForms, setEditForms] = useState<Record<number, UpgradeForm>>({});

    useEffect(() => {
        async function loadUpgrades() {
            const data = await getAllAdminUpgrades();
            setUpgrades(data);

            const initialForms: Record<number, UpgradeForm> = {};

            data.forEach((upgrade) => {
                initialForms[upgrade.id] = {
                    name: upgrade.name,
                    description: upgrade.description,
                    category: upgrade.category,
                    price: String(upgrade.price),
                };
            });

            setEditForms(initialForms);
            setLoading(false);
        }

        loadUpgrades();
    }, []);

    const categories = Array.from(
        new Set(upgrades.map((upgrade) => upgrade.category))
    );

    const visibleUpgrades = upgrades
        .filter((upgrade) =>
            categoryFilter === "ALL" || upgrade.category === categoryFilter
        )
        .filter((upgrade) => {
            if (statusFilter === "ALL") return true;
            if (statusFilter === "ACTIVE") return upgrade.active;
            return !upgrade.active;
        });

    async function handleCreateUpgrade() {
        if (
            !newUpgrade.name ||
            !newUpgrade.description ||
            !newUpgrade.category ||
            !newUpgrade.price
        ) {
            alert("Please fill all upgrade fields.");
            return;
        }

        const created = await createUpgrade({
            name: newUpgrade.name,
            description: newUpgrade.description,
            category: newUpgrade.category,
            price: Number(newUpgrade.price),
        });

        setUpgrades((prev) => [...prev, created]);

        setEditForms((prev) => ({
            ...prev,
            [created.id]: {
                name: created.name,
                description: created.description,
                category: created.category,
                price: String(created.price),
            },
        }));

        setNewUpgrade({
            name: "",
            description: "",
            category: "",
            price: "",
        });
    }

    async function handleSaveUpgrade(upgrade: Upgrade) {
        const form = editForms[upgrade.id];

        if (!form.name || !form.description || !form.category || !form.price) {
            alert("Please fill all upgrade fields.");
            return;
        }

        const updated = await updateUpgrade(upgrade.id, {
            name: form.name,
            description: form.description,
            category: form.category,
            price: Number(form.price),
            active: upgrade.active,
        });

        setUpgrades((prev) =>
            prev.map((item) => (item.id === updated.id ? updated : item))
        );
    }

    async function toggleActive(upgrade: Upgrade) {
        const form = editForms[upgrade.id];

        const updated = await updateUpgrade(upgrade.id, {
            name: form.name,
            description: form.description,
            category: form.category,
            price: Number(form.price),
            active: !upgrade.active,
        });

        setUpgrades((prev) =>
            prev.map((item) => (item.id === updated.id ? updated : item))
        );
    }

    function updateEditForm(
        upgradeId: number,
        field: keyof UpgradeForm,
        value: string
    ) {
        setEditForms((prev) => ({
            ...prev,
            [upgradeId]: {
                ...prev[upgradeId],
                [field]: value,
            },
        }));
    }

    if (loading) return <p>Loading upgrades...</p>;

    return (
        <main className="page">
            <div className="page-header">
                <div>
                    <h1>Admin Upgrades</h1>
                    <p>Manage available package upgrades and customer-facing availability.</p>
                </div>

                <span className="badge">{visibleUpgrades.length} visible</span>
            </div>

            <section className="stat-row">
                <div className="stat-card">
                    <span>Total Upgrades</span>
                    <strong>{upgrades.length}</strong>
                </div>

                <div className="stat-card">
                    <span>Active</span>
                    <strong>{upgrades.filter((upgrade) => upgrade.active).length}</strong>
                </div>

                <div className="stat-card">
                    <span>Inactive</span>
                    <strong>{upgrades.filter((upgrade) => !upgrade.active).length}</strong>
                </div>

                <div className="stat-card">
                    <span>Categories</span>
                    <strong>{categories.length}</strong>
                </div>
            </section>

            <section className="card">
                <h2>Create New Upgrade</h2>

                <div className="form-grid">
                    <input
                        className="input"
                        type="text"
                        placeholder="Name"
                        value={newUpgrade.name}
                        onChange={(event) =>
                            setNewUpgrade({ ...newUpgrade, name: event.target.value })
                        }
                    />

                    <input
                        className="input"
                        type="text"
                        placeholder="Description"
                        value={newUpgrade.description}
                        onChange={(event) =>
                            setNewUpgrade({ ...newUpgrade, description: event.target.value })
                        }
                    />

                    <input
                        className="input"
                        type="text"
                        placeholder="Category"
                        value={newUpgrade.category}
                        onChange={(event) =>
                            setNewUpgrade({ ...newUpgrade, category: event.target.value })
                        }
                    />

                    <input
                        className="input"
                        type="number"
                        placeholder="Price"
                        value={newUpgrade.price}
                        onChange={(event) =>
                            setNewUpgrade({ ...newUpgrade, price: event.target.value })
                        }
                    />
                </div>

                <button className="button" onClick={handleCreateUpgrade}>
                    Create Upgrade
                </button>
            </section>

            <section className="card">
                <h2>Filters</h2>

                <div className="toolbar">
                    <label>
                        Category
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

                    <label>
                        Status
                        <select
                            className="select"
                            value={statusFilter}
                            onChange={(event) =>
                                setStatusFilter(event.target.value as StatusFilter)
                            }
                        >
                            <option value="ALL">ALL</option>
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="INACTIVE">INACTIVE</option>
                        </select>
                    </label>
                </div>
            </section>

            {visibleUpgrades.length === 0 ? (
                <section className="card">
                    <p>No upgrades match the selected filters.</p>
                </section>
            ) : (
                <section className="upgrade-grid">
                    {visibleUpgrades.map((upgrade) => {
                        const form = editForms[upgrade.id];

                        return (
                            <section key={upgrade.id} className="card upgrade-card">
                                <div className="card-header">
                                    <strong>#{upgrade.id}</strong>

                                    <span
                                        className={
                                            upgrade.active
                                                ? "badge badge-active"
                                                : "badge badge-inactive"
                                        }
                                    >
                                    {upgrade.active ? "Active" : "Inactive"}
                                </span>
                                </div>

                                <input
                                    className="input"
                                    type="text"
                                    value={form?.name ?? ""}
                                    onChange={(event) =>
                                        updateEditForm(upgrade.id, "name", event.target.value)
                                    }
                                />

                                <input
                                    className="input"
                                    type="text"
                                    value={form?.description ?? ""}
                                    onChange={(event) =>
                                        updateEditForm(upgrade.id, "description", event.target.value)
                                    }
                                />

                                <input
                                    className="input"
                                    type="text"
                                    value={form?.category ?? ""}
                                    onChange={(event) =>
                                        updateEditForm(upgrade.id, "category", event.target.value)
                                    }
                                />

                                <input
                                    className="input"
                                    type="number"
                                    value={form?.price ?? ""}
                                    onChange={(event) =>
                                        updateEditForm(upgrade.id, "price", event.target.value)
                                    }
                                />

                                <div>
                                    <button
                                        className="button"
                                        onClick={() => handleSaveUpgrade(upgrade)}
                                    >
                                        Save
                                    </button>

                                    <button
                                        className={
                                            upgrade.active
                                                ? "button button-danger"
                                                : "button button-secondary"
                                        }
                                        onClick={() => toggleActive(upgrade)}
                                    >
                                        {upgrade.active ? "Disable" : "Enable"}
                                    </button>
                                </div>
                            </section>
                        );
                    })}
                </section>
            )}
        </main>
    );
}

export default AdminUpgradesPage;
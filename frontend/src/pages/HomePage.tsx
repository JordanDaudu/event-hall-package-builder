import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getPublicConfig } from "../api/configApi";
import type { PublicConfig } from "../types/api";

function HomePage() {
    const [config, setConfig] = useState<PublicConfig | null>(null);

    useEffect(() => {
        async function loadConfig() {
            const data = await getPublicConfig();
            setConfig(data);
        }

        loadConfig();
    }, []);

    return (
        <main className="page">
            <section className="card">
                <h1>{config?.name ?? "Event Hall Package Builder"}</h1>

                <p>
                    Build a custom event package by choosing an event type, guest count,
                    and optional upgrades.
                </p>

                {config && (
                    <p className="muted">
                        Contact: {config.contactEmail} | {config.contactPhone}
                    </p>
                )}

                <Link className="button" to="/builder">
                    Start Building Package
                </Link>
            </section>
        </main>
    );
}

export default HomePage;
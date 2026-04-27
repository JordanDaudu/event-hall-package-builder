import { Link } from "react-router-dom";

function HomePage() {
    return (
        <main className="page">
            <section className="card">
                <h1>Event Hall Package Builder</h1>

                <p>
                    Build a custom event package by choosing an event type, guest count,
                    and optional upgrades.
                </p>

                <Link className="button" to="/builder">
                    Start Building Package
                </Link>
            </section>
        </main>
    );
}

export default HomePage;
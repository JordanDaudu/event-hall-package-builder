import type { EventType, Upgrade } from "../types/api";

import hall1 from "../assets/halls/hall1.png";
import hall2 from "../assets/halls/hall2.png";

import flowers from "../assets/upgrades/flower.png";
import djBooth from "../assets/upgrades/dj.png";

type HallPreviewProps = {
    selectedEventType?: EventType;
    selectedUpgrades: Upgrade[];
};

type UpgradeVisual = {
    imageUrl: string;
    top: string;
    left: string;
    width: string;
};

const HALL_IMAGES: Record<string, string> = {
    Wedding: hall1,
    Birthday: hall2,
};

/*
 * Uses upgrade IDs instead of upgrade names.
 *
 * This is safer because an admin might rename "Flowers" to
 * "Premium Flowers", but the upgrade ID will stay the same.
 *
 * Current seeded upgrade IDs:
 * 1 = Flowers
 * 2 = DJ
 */
const UPGRADE_VISUALS: Record<number, UpgradeVisual> = {
    1: {
        imageUrl: flowers,
        top: "58%",
        left: "18%",
        width: "22%",
    },
    2: {
        imageUrl: djBooth,
        top: "55%",
        left: "60%",
        width: "24%",
    },
};

function HallPreview({ selectedEventType, selectedUpgrades }: HallPreviewProps) {
    const hallImage =
        selectedEventType && HALL_IMAGES[selectedEventType.name]
            ? HALL_IMAGES[selectedEventType.name]
            : hall1;

    return (
        <section className="card">
            <h2>Interactive Hall Preview</h2>

            <div className="hall-preview">
                <img
                    src={hallImage}
                    alt="Event hall preview"
                    className="hall-base-image"
                />

                {selectedUpgrades.map((upgrade) => {
                    const visual = UPGRADE_VISUALS[upgrade.id];

                    if (!visual) return null;

                    return (
                        <img
                            key={upgrade.id}
                            src={visual.imageUrl}
                            alt={upgrade.name}
                            className="hall-upgrade-layer"
                            style={{
                                top: visual.top,
                                left: visual.left,
                                width: visual.width,
                            }}
                        />
                    );
                })}
            </div>

            {selectedUpgrades.length === 0 && (
                <p className="muted" style={{ marginTop: 12 }}>
                    Select upgrades to see them appear in the hall preview.
                </p>
            )}

            <p className="muted">
                Preview updates live based on selected upgrades.
            </p>
        </section>
    );
}

export default HallPreview;
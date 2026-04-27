package com.eventhall.service;

import com.eventhall.dto.CreateUpgradeRequest;
import com.eventhall.dto.UpdateUpgradeRequest;
import com.eventhall.dto.UpgradeDto;
import com.eventhall.entity.Upgrade;
import com.eventhall.repository.UpgradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * Service layer for upgrade logic.
 *
 * This service is used by both:
 * - Public upgrade endpoints, where customers see active upgrades.
 * - Admin upgrade endpoints, where admins create, update, and soft-delete upgrades.
 */
@Service
public class UpgradeService {

    private final UpgradeRepository upgradeRepository;

    public UpgradeService(UpgradeRepository upgradeRepository) {
        this.upgradeRepository = upgradeRepository;
    }

    /*
     * Returns only active upgrades for the public customer UI.
     */
    public List<UpgradeDto> getAllUpgrades() {
        return upgradeRepository.findByActiveTrue()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /*
     * Returns all upgrades for the Admin UI
     */
    public List<UpgradeDto> getAllUpgradesForAdmin() {
        return upgradeRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /*
     * Returns active upgrades by ids.
     *
     * This method is not currently used by QuoteService, but it is useful design
     * because quote creation often needs to load selected upgrades safely.
     */
    public List<UpgradeDto> getUpgradesByIds(List<Long> ids) {
        return upgradeRepository.findAllById(ids)
                .stream()
                .filter(Upgrade::isActive)
                .map(this::toDto)
                .toList();
    }

    /*
     * Creates a new upgrade.
     *
     * The admin sends a CreateUpgradeRequest.
     * The service converts it into an Upgrade entity.
     * The repository saves it to the database.
     * Then we return an UpgradeDto response.
     */
    public UpgradeDto createUpgrade(CreateUpgradeRequest request) {
        Upgrade upgrade = new Upgrade(
                request.name(),
                request.description(),
                request.category(),
                request.price(),
                true
        );

        Upgrade savedUpgrade = upgradeRepository.save(upgrade);
        return toDto(savedUpgrade);
    }

    /*
     * Updates an existing upgrade.
     */
    public UpgradeDto updateUpgrade(Long id, UpdateUpgradeRequest request) {
        Upgrade upgrade = upgradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Upgrade not found with id: " + id));

        upgrade.updateDetails(
                request.name(),
                request.description(),
                request.category(),
                request.price(),
                request.active()
        );

        Upgrade savedUpgrade = upgradeRepository.save(upgrade);
        return toDto(savedUpgrade);
    }

    /*
     * Soft deletes an upgrade by marking it inactive.
     *
     * The row remains in the database.
     * This protects old quotes that may reference the upgrade.
     */
    public void deleteUpgrade(Long id) {
        Upgrade upgrade = upgradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Upgrade not found with id: " + id));

        upgrade.deactivate();
        upgradeRepository.save(upgrade);
    }

    /*
     * Converts Upgrade entity into UpgradeDto.
     */
    private UpgradeDto toDto(Upgrade upgrade) {
        return new UpgradeDto(
                upgrade.getId(),
                upgrade.getName(),
                upgrade.getDescription(),
                upgrade.getCategory(),
                upgrade.getPrice(),
                upgrade.isActive()
        );
    }
}
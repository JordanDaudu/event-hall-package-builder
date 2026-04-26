package com.eventhall.service;

import com.eventhall.dto.CreateUpgradeRequest;
import com.eventhall.dto.UpdateUpgradeRequest;
import com.eventhall.dto.UpgradeDto;
import com.eventhall.entity.Upgrade;
import com.eventhall.repository.UpgradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UpgradeService {

    private final UpgradeRepository upgradeRepository;

    public UpgradeService(UpgradeRepository upgradeRepository) {
        this.upgradeRepository = upgradeRepository;
    }

    public List<UpgradeDto> getAllUpgrades() {
        return upgradeRepository.findByActiveTrue()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<UpgradeDto> getUpgradesByIds(List<Long> ids) {
        return upgradeRepository.findAllById(ids)
                .stream()
                .filter(Upgrade::isActive)
                .map(this::toDto)
                .toList();
    }

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

    public void deleteUpgrade(Long id) {
        Upgrade upgrade = upgradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Upgrade not found with id: " + id));

        upgrade.deactivate();
        upgradeRepository.save(upgrade);
    }

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
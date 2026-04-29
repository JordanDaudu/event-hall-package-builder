package com.eventhall.service;

import com.eventhall.dto.*;
import com.eventhall.entity.PackageOption;
import com.eventhall.repository.PackageOptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Business logic for package option management.
 *
 * Soft-delete convention: "deleting" sets active=false so that existing
 * package requests that reference the option remain consistent.
 * ON DELETE CASCADE in the DB ensures that if a row is ever hard-deleted
 * (e.g. via maintenance SQL), all customer price overrides for it are
 * automatically removed.
 */
@Service
@Transactional
public class PackageOptionService {

    private final PackageOptionRepository optionRepository;

    public PackageOptionService(PackageOptionRepository optionRepository) {
        this.optionRepository = optionRepository;
    }

    // -----------------------------------------------------------------------
    // Public / Customer read
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PackageOptionResponse> listActive() {
        return optionRepository.findAllByActiveTrueOrderBySortOrderAsc()
                .stream().map(PackageOptionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PackageOptionResponse> listActiveByCategory(PackageOptionCategory category) {
        return optionRepository.findAllByActiveTrueAndCategoryOrderBySortOrderAsc(category)
                .stream().map(PackageOptionResponse::from).toList();
    }

    // -----------------------------------------------------------------------
    // Admin read
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PackageOptionResponse> listAll() {
        return optionRepository.findAllByOrderBySortOrderAscCreatedAtDesc()
                .stream().map(PackageOptionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PackageOptionResponse getById(Long id) {
        return PackageOptionResponse.from(require(id));
    }

    // -----------------------------------------------------------------------
    // Admin write
    // -----------------------------------------------------------------------

    public PackageOptionResponse create(CreatePackageOptionRequest req) {
        if (optionRepository.existsByNameHe(req.nameHe())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "כבר קיימת אפשרות עם שם זה");
        }
        PackageOption option = PackageOption.builder()
                .nameHe(req.nameHe())
                .nameEn(req.nameEn())
                .category(req.category())
                .globalPrice(req.globalPrice())
                .active(true)
                .sortOrder(req.effectiveSortOrder())
                .build();
        return PackageOptionResponse.from(optionRepository.save(option));
    }

    public PackageOptionResponse update(Long id, UpdatePackageOptionRequest req) {
        PackageOption option = require(id);

        if (req.nameHe() != null) {
            if (optionRepository.existsByNameHeAndIdNot(req.nameHe(), id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "כבר קיימת אפשרות עם שם זה");
            }
            option.setNameHe(req.nameHe());
        }
        if (req.nameEn() != null)       option.setNameEn(req.nameEn());
        if (req.category() != null)     option.setCategory(req.category());
        if (req.globalPrice() != null)  option.setGlobalPrice(req.globalPrice());
        if (req.sortOrder() != null)    option.setSortOrder(req.sortOrder());

        return PackageOptionResponse.from(optionRepository.save(option));
    }

    public PackageOptionResponse setActive(Long id, boolean active) {
        PackageOption option = require(id);
        option.setActive(active);
        return PackageOptionResponse.from(optionRepository.save(option));
    }

    /** Soft-delete: sets active=false rather than removing the row. */
    public void delete(Long id) {
        PackageOption option = require(id);
        option.setActive(false);
        optionRepository.save(option);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private PackageOption require(Long id) {
        return optionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "אפשרות חבילה לא נמצאה"));
    }
}

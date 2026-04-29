package com.eventhall.repository;

import com.eventhall.dto.PackageOptionCategory;
import com.eventhall.entity.PackageOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageOptionRepository extends JpaRepository<PackageOption, Long> {

    /** Public listing — only active options, sorted for the builder UI. */
    List<PackageOption> findAllByActiveTrueOrderBySortOrderAsc();

    /** Public listing filtered by category (e.g. for a category tab in the builder). */
    List<PackageOption> findAllByActiveTrueAndCategoryOrderBySortOrderAsc(PackageOptionCategory category);

    /** Admin listing — all options regardless of active flag, stable order. */
    List<PackageOption> findAllByOrderBySortOrderAscCreatedAtDesc();

    /** Admin listing filtered by category. */
    List<PackageOption> findAllByCategoryOrderBySortOrderAsc(PackageOptionCategory category);

    /** Name uniqueness check (Hebrew name must be unique across all options). */
    boolean existsByNameHe(String nameHe);

    boolean existsByNameHeAndIdNot(String nameHe, Long excludeId);
}

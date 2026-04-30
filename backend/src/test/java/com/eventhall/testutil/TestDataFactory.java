package com.eventhall.testutil;

import com.eventhall.dto.PackageOptionCategory;
import com.eventhall.entity.PackageOption;
import com.eventhall.entity.UserAccount;
import com.eventhall.entity.Venue;
import com.eventhall.enums.UserRole;

import java.math.BigDecimal;

/**
 * Shared test data factory for building valid current-model entities.
 *
 * All fields that are non-nullable in the database are populated.
 * Entities returned here are NOT persisted — callers must pass them
 * to the appropriate repository.save() call.
 */
public class TestDataFactory {

    private TestDataFactory() {}

    public static UserAccount adminUser(String email, String passwordHash) {
        return UserAccount.builder()
                .fullName("Test Admin")
                .email(email)
                .passwordHash(passwordHash)
                .role(UserRole.ADMIN)
                .active(true)
                .build();
    }

    public static UserAccount customerUser(String email, String passwordHash, BigDecimal basePrice) {
        return UserAccount.builder()
                .fullName("Test Customer")
                .email(email)
                .passwordHash(passwordHash)
                .role(UserRole.CUSTOMER)
                .active(true)
                .basePackagePrice(basePrice)
                .build();
    }

    public static Venue activeVenue(String nameHe) {
        return Venue.builder()
                .nameHe(nameHe)
                .nameEn(nameHe)
                .active(true)
                .sortOrder(99)
                .build();
    }

    public static PackageOption chuppah(String nameHe, BigDecimal price) {
        return PackageOption.builder()
                .nameHe(nameHe)
                .nameEn(nameHe)
                .category(PackageOptionCategory.CHUPPAH)
                .globalPrice(price)
                .imageUrl("/test/chuppah.png")
                .visualBehavior("REPLACE_IMAGE")
                .active(true)
                .sortOrder(1)
                .build();
    }

    public static PackageOption chuppahUpgrade(String nameHe, BigDecimal price) {
        return PackageOption.builder()
                .nameHe(nameHe)
                .nameEn(nameHe)
                .category(PackageOptionCategory.CHUPPAH_UPGRADE)
                .globalPrice(price)
                .imageUrl("/test/chuppah-upgrade.png")
                .visualBehavior("OVERLAY_IMAGE")
                .active(true)
                .sortOrder(1)
                .build();
    }

    public static PackageOption aisle(String nameHe, BigDecimal price) {
        return PackageOption.builder()
                .nameHe(nameHe)
                .nameEn(nameHe)
                .category(PackageOptionCategory.AISLE)
                .globalPrice(price)
                .imageUrl("/test/aisle.png")
                .visualBehavior("REPLACE_IMAGE")
                .active(true)
                .sortOrder(1)
                .build();
    }

    public static PackageOption tableFrame(String nameHe, BigDecimal price, String tableContext) {
        return PackageOption.builder()
                .nameHe(nameHe)
                .nameEn(nameHe)
                .category(PackageOptionCategory.TABLE_FRAME)
                .globalPrice(price)
                .imageUrl("/test/frame.png")
                .visualBehavior("REPLACE_IMAGE")
                .tableContext(tableContext)
                .active(true)
                .sortOrder(1)
                .build();
    }

    public static PackageOption tableFlower(String nameHe, BigDecimal price,
                                            String flowerSize, String tableContext) {
        return PackageOption.builder()
                .nameHe(nameHe)
                .nameEn(nameHe)
                .category(PackageOptionCategory.TABLE_FLOWER)
                .globalPrice(price)
                .imageUrl("/test/flower.png")
                .visualBehavior("REPLACE_IMAGE")
                .flowerSize(flowerSize)
                .tableContext(tableContext)
                .active(true)
                .sortOrder(1)
                .build();
    }

    public static PackageOption tableCandle(String nameHe, BigDecimal price, String tableContext) {
        return PackageOption.builder()
                .nameHe(nameHe)
                .nameEn(nameHe)
                .category(PackageOptionCategory.TABLE_CANDLE)
                .globalPrice(price)
                .imageUrl("/test/candle.png")
                .visualBehavior("REPLACE_IMAGE")
                .tableContext(tableContext)
                .active(true)
                .sortOrder(1)
                .build();
    }

    public static PackageOption napkin(String nameHe, BigDecimal price) {
        return PackageOption.builder()
                .nameHe(nameHe)
                .nameEn(nameHe)
                .category(PackageOptionCategory.NAPKIN)
                .globalPrice(price)
                .imageUrl("/test/napkin.png")
                .visualBehavior("REPLACE_IMAGE")
                .active(true)
                .sortOrder(1)
                .build();
    }

    public static PackageOption tablecloth(String nameHe, BigDecimal price) {
        return PackageOption.builder()
                .nameHe(nameHe)
                .nameEn(nameHe)
                .category(PackageOptionCategory.TABLECLOTH)
                .globalPrice(price)
                .imageUrl("/test/tablecloth.png")
                .visualBehavior("REPLACE_IMAGE")
                .active(true)
                .sortOrder(1)
                .build();
    }

    public static PackageOption brideChair(String nameHe, BigDecimal price) {
        return PackageOption.builder()
                .nameHe(nameHe)
                .nameEn(nameHe)
                .category(PackageOptionCategory.BRIDE_CHAIR)
                .globalPrice(price)
                .imageUrl("/test/bride-chair.png")
                .visualBehavior("REPLACE_IMAGE")
                .active(true)
                .sortOrder(1)
                .build();
    }
}

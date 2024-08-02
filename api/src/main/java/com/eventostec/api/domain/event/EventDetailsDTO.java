package com.eventostec.api.domain.event;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public record EventDetailsDTO(
    UUID id,
    String title,
    String description,
    Date date,
    String imgUrl,
    String city,
    String uf,
    String eventUrl,
    Boolean remote,
    List<CouponDTO> coupons
) {

    public record CouponDTO(
        String code,
        Integer discount,
        Date validUntil
    ) {}

}

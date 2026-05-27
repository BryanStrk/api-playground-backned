package com.bryan.apiplayground.apis.dota;

import java.util.List;

public record DotaHero(
        int id,
        String name,
        String localizedName,
        String primaryAttr,
        String attackType,
        List<String> roles,
        String imgUrl,
        String iconUrl
) {
}

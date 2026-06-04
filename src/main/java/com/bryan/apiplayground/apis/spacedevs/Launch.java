package com.bryan.apiplayground.apis.spacedevs;

public record Launch(
        String id,
        String name,
        String net,
        String status,
        String provider,
        String padName,
        String padLocation,
        String image
) {
}

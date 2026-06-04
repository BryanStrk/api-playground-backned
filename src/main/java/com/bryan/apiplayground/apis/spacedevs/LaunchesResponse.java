package com.bryan.apiplayground.apis.spacedevs;

import java.util.List;

public record LaunchesResponse(
        int count,
        List<Launch> launches
) {
}

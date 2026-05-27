package com.bryan.apiplayground.apis.sports;

import java.util.List;

public record GroupTable(String groupName, List<StandingRow> table) {
}

package com.bryan.apiplayground.apis.qrcode;

import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class QrService {

    private static final String QR_BASE = "https://api.qrserver.com/v1/create-qr-code/";
    private static final String DEFAULT_SIZE = "300x300";
    // Accept any positive WxH (e.g. 100x100, 1000x1000). Anything weirder falls
    // back to DEFAULT_SIZE silently so a bad query never breaks the card.
    private static final Pattern SIZE_PATTERN = Pattern.compile("^\\d+x\\d+$");

    public QrResponse generate(String data, String size) {
        var effectiveSize = (size != null && SIZE_PATTERN.matcher(size).matches())
                ? size
                : DEFAULT_SIZE;
        // The endpoint doesn't proxy the PNG — it returns the upstream URL so
        // the frontend embeds it directly in an <img>. URL-encode data so
        // payloads with spaces, &, = etc. still resolve to a valid query string.
        var encoded = URLEncoder.encode(data, StandardCharsets.UTF_8);
        var qrUrl = QR_BASE + "?data=" + encoded + "&size=" + effectiveSize;
        return new QrResponse(data, effectiveSize, qrUrl);
    }
}

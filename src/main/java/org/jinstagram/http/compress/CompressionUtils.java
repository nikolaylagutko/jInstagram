package org.jinstagram.http.compress;

import org.apache.commons.lang3.StringUtils;
import org.jinstagram.http.Request;
import org.jinstagram.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

public final class CompressionUtils {

    private static final Logger logger = LoggerFactory.getLogger(CompressionUtils.class);

    private static final String REQUEST_HEADER = "Accept-Encoding";

    private static final String RESPONSE_HEADER = "Content-Encoding";

    private static final String GZIP_VALUE = "gzip";

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private CompressionUtils() {

    }

    public static Request addCompression(Request request, CompressionType compression) {
        switch (compression) {
            case GZIP:
                request.addHeader(REQUEST_HEADER, GZIP_VALUE);
                break;
            case NONE:
                //no compression, do nothing
                break;
        }

        return request;
    }

    public static boolean isCompressed(Response response) {
        String encoding = response.getHeader(RESPONSE_HEADER);

        return StringUtils.isEmpty(encoding) ? false : encoding.contains(GZIP_VALUE);
    }

    public static InputStream uncompressedStream(InputStream stream) {
        try {
            return new GZIPInputStream(stream);
        } catch (IOException e) {
            logger.error("Unable to uncompress response", e);
            throw new IllegalStateException("Error while reading response body", e);
        }
    }

}

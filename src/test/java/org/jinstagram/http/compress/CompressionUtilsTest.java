package org.jinstagram.http.compress;

import org.jinstagram.http.Response;
import org.jinstagram.http.StreamUtils;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompressionUtilsTest {

    private static final Map<String, String> GZIPPED_RESPONSE_HEADERS = new HashMap<String, String>();

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private static final String BODY = "body";

    static {
        GZIPPED_RESPONSE_HEADERS.put("Content-Encoding", "gzip");
    }

    @Test
    public void testCheckResponseIsCompressed() throws IOException {
        Response response = response(gzippedStream(BODY), GZIPPED_RESPONSE_HEADERS);

        boolean result = CompressionUtils.isCompressed(response);

        assertTrue("Content-Encoding: gzip should be a compressed response", result);
    }

    @Test
    public void testCheckResponseIsNoHeader() {
        Response response = response(defaultStream(BODY), new HashMap<String, String>());

        boolean result = CompressionUtils.isCompressed(response);

        assertFalse("No-headers response should be a non-compressed response", result);
    }

    @Test
    public void testCheckResponseIfUnknownHeaderValue() {
        GZIPPED_RESPONSE_HEADERS.put("Content-Encoding", "unknown");

        Response response = response(defaultStream(BODY), GZIPPED_RESPONSE_HEADERS);

        boolean result = CompressionUtils.isCompressed(response);

        assertFalse("Unknown header response should be a non-compressed response", result);
    }

    @Test
    public void streamingGzippedBody() throws IOException {
        InputStream result = CompressionUtils.uncompressedStream(gzippedStream(BODY));

        validateStream(BODY, result);
    }

    @Test(expected = IllegalStateException.class)
    public void streamingNonGzippedBody() throws IOException {
        CompressionUtils.uncompressedStream(defaultStream(BODY));
    }

    private void validateStream(String expected, InputStream actual) {
        String actualString = StreamUtils.getStreamContents(actual);

        assertEquals("Unexpected BODY from Stream", expected, actualString);
    }

    private Response response(InputStream stream, Map<String, String> headers) {
        Response result = mock(Response.class);

        when(result.getStream()).thenReturn(stream);

        for (Map.Entry<String, String> entry: headers.entrySet()) {
            when(result.getHeader(entry.getKey())).thenReturn(entry.getValue());
        }

        return result;
    }

    private InputStream gzippedStream(String value) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputStream gzipped = new GZIPOutputStream(output);

        gzipped.write(value.getBytes(CHARSET));
        gzipped.close();

        return new ByteArrayInputStream(output.toByteArray());
    }

    private InputStream defaultStream(String value) {
        return new ByteArrayInputStream(value.getBytes(CHARSET));
    }

}

package my.sample.openfeign.osm;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OSMErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response resp) {
        log.error("Failed to decode the error, methodKey: {}, response: {}", methodKey, resp);
        return null; // don't throw exception so far. if exception then no distance
    }
}

package my.sample.openfeign.web;

import lombok.RequiredArgsConstructor;
import my.sample.openfeign.osm.AddressDecoder;
import my.sample.openfeign.osm.AddressDecoder.Coordinate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.util.Objects.isNull;

@RestController
@RequiredArgsConstructor
public class HelloController {

    private final AddressDecoder addressDecoder;

    @GetMapping(path = "hi")
    String hello(@RequestParam(value = "name", required = false) String name) {
        return String.format("Hi %s \uD83D\uDE80", isNull(name) ? "?" : name);
    }

    @GetMapping(path = "where-are-u")
    String whereAreU(@RequestParam(value = "postalcode1") String postalcode1,
                     @RequestParam(value = "postalcode2") String postalcode2) {
        List<Coordinate> coordinates = addressDecoder.decode(new AddressDecoder.PostalCode(String.format("%s+%s", postalcode1, postalcode2)));
        return String.format("Hi! I'm here: %s \uD83D\uDE80", coordinates.get(0));
    }
}

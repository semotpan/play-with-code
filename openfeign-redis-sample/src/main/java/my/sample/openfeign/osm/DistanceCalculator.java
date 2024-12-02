package my.sample.openfeign.osm;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import my.sample.openfeign.osm.AddressDecoder.Coordinate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
        name = "routing-osm",
        url = "${feign.client.config.routing-osm.url}",
        configuration = FeignConfig.class
)
public interface DistanceCalculator {

    @Cacheable(value = "drivingDistances", unless = "#result == null")
    @GetMapping(path = "/table/v1/driving/{drivingPath}?annotations=distance", consumes = APPLICATION_JSON_VALUE)
    DistancesResponse drivingDistances(@PathVariable("drivingPath") DrivingPath drivingPath);

    @EqualsAndHashCode
    final class DrivingPath implements Serializable {

        public final List<Coordinate> coordinates;

        public DrivingPath(List<Coordinate> coordinates) {
            this.coordinates = requireNonNull(coordinates, "coordinates cannot be null");
        }

        @Override
        public String toString() {
            return coordinates.stream()
                    .map(Coordinate::toString)
                    .collect(Collectors.joining(";"));
        }
    }

    @Setter // used by deserialization
    @EqualsAndHashCode
    final class DistancesResponse implements Serializable{

        private Double[][] distances;

        /**
         * Get the distance by facility order index
         */
        public Double distanceBy(int index) {
            if (distances == null || distances.length == 0) {
                return null;
            }

            if (distances[0].length <= index || index < 0) {
                throw new IndexOutOfBoundsException("index must be valid");
            }

            return distances[0][index] == 0 ? null : distances[0][index];
        }

        static DistancesResponse defaultDistances() {
            return new DistancesResponse();
        }
    }
}

package my.sample.openfeign.osm;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import my.sample.openfeign.osm.AddressDecoder.PostalCode;

import java.util.List;

import static java.util.Objects.requireNonNull;

public interface DistanceOperations {

    List<PointDistance> distances(DistanceRequest request);

    final class DistanceRequest {
        public final PostalCode startAddress;
        public final List<PostalCode> endAddresses;

        public DistanceRequest(PostalCode startAddress, List<PostalCode> endAddresses) {
            this.startAddress = requireNonNull(startAddress, "startAddress cannot be null");
            this.endAddresses = requireNonNull(endAddresses, "endAddresses cannot be null");
        }
    }

    @EqualsAndHashCode
    @ToString
    final class PointDistance {

        private final String postalCode;
        private final Double distance;

        public PointDistance(String postalCode, Double distance) {
            this.postalCode = requireNonNull(postalCode, "postalCode cannot be null");
            this.distance = distance;
        }

        public Double distance() {
            return distance;
        }
    }
}

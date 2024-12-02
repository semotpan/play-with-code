package my.sample.openfeign.osm;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.sample.openfeign.osm.AddressDecoder.Coordinate;
import my.sample.openfeign.osm.AddressDecoder.PostalCode;
import my.sample.openfeign.osm.DistanceCalculator.DrivingPath;
import my.sample.openfeign.osm.DistanceCalculator.DistancesResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static my.sample.openfeign.osm.DistanceCalculator.DistancesResponse.defaultDistances;


// FIXME: provide details about implementation

/**
 * Current implementation uses OpenStreetMap as map
 */
@Component
@RequiredArgsConstructor
@Slf4j
class OSMDistanceOperations implements DistanceOperations {

    private final AddressDecoder addressDecoder;
    private final DistanceCalculator distanceCalculator;

    @Override
    public List<PointDistance> distances(DistanceRequest request) {

        List<Coordinate> endpoints = new ArrayList<>(request.endAddresses.size() + 1);
        boolean[] decodedCodes = new boolean[request.endAddresses.size()];

        try {
            // decode the start postal code
            List<Coordinate> start = addressDecoder.decode(request.startAddress);

            if (isNull(start) || start.isEmpty()) {
                return mapDistanceToPostalCode(request, defaultDistances(), decodedCodes);
            }

            endpoints.add(start.get(0));

        } catch (FeignException ex) {
            log.error("Failed to get start coordinates", ex);
            return mapDistanceToPostalCode(request, defaultDistances(), decodedCodes);
        }

        // decode end postal codes in provided order
        for (int order = 0; order < request.endAddresses.size(); order++) {
            PostalCode postalCode = request.endAddresses.get(order);

            try {
                List<Coordinate> endpoint = addressDecoder.decode(postalCode);

                if (nonNull(endpoint) && !endpoint.isEmpty()) {
                    endpoints.add(endpoint.get(0));
                    decodedCodes[order] = true;
                }
            } catch (FeignException ex) {
                log.error("Failed to get postal code: {} coordinates", postalCode);
            }
        }

        // request distance calculator to get distances from start to endpoints
        try {
            DistancesResponse response = distanceCalculator.drivingDistances(new DrivingPath(endpoints));
            return mapDistanceToPostalCode(request, response, decodedCodes);
        } catch (FeignException ex) {
            log.error("Failed to get distances", ex);
        }

        return mapDistanceToPostalCode(request, defaultDistances(), decodedCodes); // get
    }

    // map distances to postal code in provided request order
    // index 0 is the patient coordinates which are 0.0, query order is from [1-n]
    private List<PointDistance> mapDistanceToPostalCode(DistanceRequest request, DistancesResponse response, boolean[] decodedCodes) {
        List<PointDistance> pointDistances = new ArrayList<>(request.endAddresses.size());

        for (int order = 0, decoded = 0; order < request.endAddresses.size(); order++) {
            PostalCode postalCode = request.endAddresses.get(order);

            Double distance = null;
            if (decodedCodes[order]) {
                distance = response.distanceBy(decoded + 1); // 0 -index is start distance
                decoded++;
            }

            pointDistances.add(new PointDistance(postalCode.toString(), distance));
        }

        return pointDistances;
    }
}

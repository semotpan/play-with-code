package my.sample.openfeign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.sample.openfeign.osm.AddressDecoder;
import my.sample.openfeign.osm.AddressDecoder.PostalCode;
import my.sample.openfeign.osm.DistanceCalculator;
import my.sample.openfeign.osm.DistanceOperations;
import my.sample.openfeign.osm.DistanceOperations.DistanceRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static my.sample.openfeign.osm.DistanceOperations.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CLI implements CommandLineRunner {

    private final AddressDecoder addressDecoder;
    private final DistanceCalculator distanceCalculator;
    private final DistanceOperations distanceOperations;


    @Override
    public void run(String... args) throws Exception {

        // no cache
        submit();

        // from cache
        submit();
//
//        List<AddressDecoder.Coordinate> startCoordinates = addressDecoder.decode("DN31+2AB");
//        List<AddressDecoder.Coordinate> endPoint1Coordinates = addressDecoder.decode("DN32+7QL");
//        List<AddressDecoder.Coordinate> endPoint2Coordinates = addressDecoder.decode("DN35+8EB");
//
//        DistanceCalculator.DistancesResponse distancesResponse = distanceCalculator.drivingDistances(new DistanceCalculator.Coordinates(Arrays.asList(
//                startCoordinates.get(0),
//                endPoint1Coordinates.get(0),
//                endPoint2Coordinates.get(0)
//        )));

//        log.info("=========================================================");
//        log.info("Here is the map: (DN31 2AB) \uD83D\uDE80 (DN32 7QL), (DN35 8EB)");
//        log.info("Start point: (DN31+2AB) => {}", startCoordinates.get(0));
//        log.info("end point1: (DN32+7QL) => {}", endPoint1Coordinates.get(0));
//        log.info("end point2: (DN35+8EB) => {}", endPoint2Coordinates.get(0));
//        log.info("{}", distancesResponse);
//        log.info("distances: ({}) => ({}), ({})", distancesResponse.distanceBy(0), distancesResponse.distanceBy(1), distancesResponse.distanceBy(2));
//        log.info("=========================================================");
    }

    private void submit() {
        List<PointDistance> fromCache = distanceOperations.distances(new DistanceRequest(
                new PostalCode("DN31+2AB"),
                Arrays.asList(
                        new PostalCode("DN32+7QL"),
                        new PostalCode("DN35+8EB")
                )
        ));

        log.info("=========================================================");
        log.info("Here is the map: (DN31 2AB) \uD83D\uDE80 (DN32 7QL), (DN35 8EB)");
        log.info("{}", fromCache);
        log.info("=========================================================");
    }
}

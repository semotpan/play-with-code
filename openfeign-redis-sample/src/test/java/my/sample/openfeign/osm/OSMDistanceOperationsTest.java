package my.sample.openfeign.osm;

import feign.FeignException;
import my.sample.openfeign.osm.DistanceCalculator.DistancesResponse;
import my.sample.openfeign.osm.DistanceCalculator.DrivingPath;
import my.sample.openfeign.osm.DistanceOperations.DistanceRequest;
import my.sample.openfeign.osm.DistanceOperations.PointDistance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static my.sample.openfeign.osm.AddressDecoder.Coordinate;
import static my.sample.openfeign.osm.AddressDecoder.PostalCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@Tag("unit")
class OSMDistanceOperationsTest {

    AddressDecoder addressDecoder;
    DistanceCalculator distanceCalculator;
    OSMDistanceOperations operations;

    @BeforeEach
    void setUp() {
        addressDecoder = Mockito.mock(AddressDecoder.class);
        distanceCalculator = Mockito.mock(DistanceCalculator.class);
        operations = new OSMDistanceOperations(addressDecoder, distanceCalculator);
    }

    @Test
    @DisplayName("should return null distances when decoding start address throws FeignException")
    void nullDistancesWhenStartFeignException() {
        // given: a valid request and address decoder return feign exception
        DistanceRequest request = validRequest();
        givenDecodedAddressFeignException(validStartPostalCode());

        // when: address decoder fails
        List<PointDistance> distances = operations.distances(request);

        // then: all point distances are null
        assertThat(distances)
                .hasSize(2)
                .extracting(PointDistance::distance)
                .containsExactly(null, null);

        // and: ensure address decoder interaction
        ensureDecoderInvoked(validStartPostalCode());
    }

    @Test
    @DisplayName("should return null distances when no decoded result found for start address")
    void nullDistancesWhenStartPointNoDecoded() {
        // given: a valid request and address decoder return empty result
        DistanceRequest request = validRequest();
        givenEmptyDecodedAddress(validStartPostalCode());

        // when: address decoder fails
        List<PointDistance> distances = operations.distances(request);

        // then: all point distances are null
        assertThat(distances)
                .hasSize(2)
                .extracting(PointDistance::distance)
                .containsExactly(null, null);

        // and: ensure address decoder interaction
        ensureDecoderInvoked(validStartPostalCode());
    }

    @Test
    @DisplayName("should return null distances when decoding all endpoints throws FeignException")
    void nullDistancesWhenEndPointsFeignException() {
        // given: a valid request, valid start coordinate and enpoints decoding failures
        DistanceRequest request = validRequest();
        givenValidDecodedAddress(validStartPostalCode());
        givenDecodedAddressFeignException(validEndpoint1PostalCode());
        givenDecodedAddressFeignException(validEndpoint2PostalCode());

        // when: address decoder fails
        List<PointDistance> distances = operations.distances(request);

        // then: all point distances are null
        assertThat(distances)
                .hasSize(2)
                .extracting(PointDistance::distance)
                .containsExactly(null, null);

        // and: ensure address decoder interaction
        ensureDecoderInvoked(validStartPostalCode());
        ensureDecoderInvoked(validEndpoint1PostalCode());
        ensureDecoderInvoked(validEndpoint2PostalCode());
    }

    @Test
    @DisplayName("should return null distances when no decoded result found for all endpoints")
    void nullDistancesWhenEndPointsNoDecoded() {
        // given
        DistanceRequest request = validRequest();
        givenValidDecodedAddress(validStartPostalCode());
        givenEmptyDecodedAddress(validEndpoint1PostalCode());
        givenEmptyDecodedAddress(validEndpoint2PostalCode());

        // when
        List<PointDistance> distances = operations.distances(request);

        // then: all point distances are null
        assertThat(distances)
                .hasSize(2)
                .extracting(PointDistance::distance)
                .containsExactly(null, null);

        // and
        ensureDecoderInvoked(validStartPostalCode());
        ensureDecoderInvoked(validEndpoint1PostalCode());
        ensureDecoderInvoked(validEndpoint2PostalCode());
    }

    @Test
    @DisplayName("should return null distances when distance calculation request throws FeignException")
    void nullDistancesWhenDistanceCalculatorFeignException() {
        // given
        DistanceRequest request = validRequest();
        givenValidDecodedAddress(validStartPostalCode());
        givenValidDecodedAddress(validEndpoint1PostalCode());
        givenValidDecodedAddress(validEndpoint2PostalCode());
        givenDistanceResponseFeignException();

        // when
        List<PointDistance> distances = operations.distances(request);

        // then: all point distances are null
        assertThat(distances)
                .hasSize(2)
                .extracting(PointDistance::distance)
                .containsExactly(null, null);

        // and
        ensureDecoderInvoked(validStartPostalCode());
        ensureDecoderInvoked(validEndpoint1PostalCode());
        ensureDecoderInvoked(validEndpoint2PostalCode());
        ensureDistanceCalculatorInvoked();
    }

    @Test
    @DisplayName("should return partial distances when some endpoints have no decoded results")
    void partialDistancesWhenSomeEndPointsNoDecoded() {
        // given
        DistanceRequest request = validRequest();
        givenValidDecodedAddress(validStartPostalCode());
        givenValidDecodedAddress(validEndpoint1PostalCode());
        givenEmptyDecodedAddress(validEndpoint2PostalCode());
        givenPartialDistanceResponse();

        // when
        List<PointDistance> distances = operations.distances(request);

        // then: all point distances are null
        assertThat(distances)
                .hasSize(2)
                .containsExactly(
                        new PointDistance(validEndpoint1PostalCode().toString(), 1050.6),
                        new PointDistance(validEndpoint2PostalCode().toString(), null)
                );

        // and
        ensureDecoderInvoked(validStartPostalCode());
        ensureDecoderInvoked(validEndpoint1PostalCode());
        ensureDecoderInvoked(validEndpoint2PostalCode());
        ensureDistanceCalculatorInvoked();
    }

    @Test
    @DisplayName("should return distances for valid decoded start and endpoints postal codes")
    void gettingDistances() {
        // given
        DistanceRequest request = validRequest();
        givenValidDecodedAddress(validStartPostalCode());
        givenValidDecodedAddress(validEndpoint1PostalCode());
        givenValidDecodedAddress(validEndpoint2PostalCode());
        givenValidDistanceResponse();

        // when
        List<PointDistance> distances = operations.distances(request);

        // then: all point distances are null
        assertThat(distances)
                .hasSize(2)
                .containsExactly(
                        new PointDistance(validEndpoint1PostalCode().toString(), 1050.6),
                        new PointDistance(validEndpoint2PostalCode().toString(), 5050.4)
                );

        // and
        ensureDecoderInvoked(validStartPostalCode());
        ensureDecoderInvoked(validEndpoint1PostalCode());
        ensureDecoderInvoked(validEndpoint2PostalCode());
        ensureDistanceCalculatorInvoked();
    }

    private DistanceRequest validRequest() {
        return new DistanceRequest(validStartPostalCode(),
                Arrays.asList(validEndpoint1PostalCode(), validEndpoint2PostalCode()));
    }

    private PostalCode validStartPostalCode() {
        return new PostalCode("AB");
    }

    private PostalCode validEndpoint1PostalCode() {
        return new PostalCode("CD");
    }

    private PostalCode validEndpoint2PostalCode() {
        return new PostalCode("DE");
    }

    private void givenValidDecodedAddress(PostalCode postalCode) {
        given(addressDecoder.decode(postalCode)).willReturn(singletonList(new Coordinate()));
    }

    private void givenEmptyDecodedAddress(PostalCode postalCode) {
        given(addressDecoder.decode(postalCode)).willReturn(emptyList());
    }

    private void givenDecodedAddressFeignException(PostalCode postalcode) {
        given(addressDecoder.decode(postalcode)).willThrow(Mockito.mock(FeignException.class));
    }

    private void ensureDecoderInvoked(PostalCode postalCode) {
        verify(addressDecoder).decode(postalCode);
    }

    private void givenValidDistanceResponse() {
        given(distanceCalculator.drivingDistances(any(DrivingPath.class)))
                .willReturn(validDistancesResponse());
    }

    private void givenPartialDistanceResponse() {
        given(distanceCalculator.drivingDistances(any(DrivingPath.class)))
                .willReturn(partialDistancesResponse());
    }

    private void givenDistanceResponseFeignException() {
        given(distanceCalculator.drivingDistances(any(DrivingPath.class)))
                .willThrow(Mockito.mock(FeignException.class));
    }

    private void ensureDistanceCalculatorInvoked() {
        verify(distanceCalculator).drivingDistances(any(DrivingPath.class));
    }

    private DistancesResponse validDistancesResponse() {
        Double[][] distances = new Double[2][3];
        distances[0][1] = 1050.6;
        distances[0][2] = 5050.4;

        DistancesResponse response = new DistancesResponse();
        response.setDistances(distances);
        return response;
    }

    private DistancesResponse partialDistancesResponse() {
        Double[][] distances = new Double[2][3];
        distances[0][1] = 1050.6;

        DistancesResponse response = new DistancesResponse();
        response.setDistances(distances);
        return response;
    }
}

package io.awssample.handler;

import java.util.UUID;

public record ProductNumberLockedCommand(UUID lockId, String productNumber) {
}

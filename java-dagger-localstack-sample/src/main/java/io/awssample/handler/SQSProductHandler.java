package io.awssample.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class SQSProductHandler implements RequestHandler<SQSEvent, List<String>> {

    // first set TTL property
    static {
        java.security.Security.setProperty("networkaddress.cache.ttl", "0");
    }

    private static final Logger logger = LoggerFactory.getLogger(SQSProductHandler.class);

    @Inject
    ObjectMapper objectMapper;

//    public SQSProductHandler() {
//        DaggerProductComponent.builder().build().inject(this);
//    }

    @Override
    public List<String> handleRequest(SQSEvent event, Context context) {
//        for (SQSEvent.SQSMessage record : event.getRecords()) {
//            String messageBody = record.getBody();
//            String messageId = record.getMessageId();
//            String receiptHandle = record.getReceiptHandle();
//            logger.info("SQS Message: {}", messageBody);
//            logger.info("Message ID: {}", messageId);
//            logger.info("Receipt Handle: {}", receiptHandle);
//        }

        LambdaLogger logger = context.getLogger();
        logger.log("EVENT TYPE: " + event.getClass().toString());
        var messagesFound = new ArrayList<String>();
        for(SQSEvent.SQSMessage msg : event.getRecords()){
            messagesFound.add(msg.getBody());
            logger.log("SQS Message: " +  msg.getBody());
            logger.log("Message ID: " + msg.getMessageId());
            logger.log("Receipt Handle: " + msg.getReceiptHandle());
        }
        // FIXME: delete consumed messages?
        return messagesFound;
    }
}

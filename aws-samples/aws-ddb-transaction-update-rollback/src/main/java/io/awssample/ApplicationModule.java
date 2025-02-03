package io.awssample;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class ApplicationModule {

    @Provides
    @Singleton
    public static ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}

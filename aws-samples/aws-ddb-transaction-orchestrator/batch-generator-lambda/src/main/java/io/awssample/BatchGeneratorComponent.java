package io.awssample;

import dagger.Component;
import io.awssample.application.ApplicationModule;
import io.awssample.handler.BatchOrderHandler;
import io.awssample.persistence.PersistenceModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ApplicationModule.class, PersistenceModule.class})
public interface BatchGeneratorComponent {

    void inject(BatchOrderHandler handler);

}

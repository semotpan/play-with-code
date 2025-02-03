package io.awssample;

import dagger.Component;
import io.awssample.handler.SqsLockHandler;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ApplicationModule.class, PersistenceModule.class})
public interface OrderSyncComponent {

    void inject(SqsLockHandler handler);

}

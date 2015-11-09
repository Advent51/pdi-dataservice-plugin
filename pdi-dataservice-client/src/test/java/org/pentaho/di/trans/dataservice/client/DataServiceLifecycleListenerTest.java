/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.dataservice.client;

import com.google.common.base.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.lifecycle.LifeEventHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.dataservice.jdbc.ThinConnection;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author nhudak
 */
@RunWith( MockitoJUnitRunner.class )
public class DataServiceLifecycleListenerTest {

  @Mock Spoon spoon;
  @Mock Supplier<Spoon> spoonSupplier;
  @Mock DataServiceClientService clientService;
  DataServiceLifecycleListener lifecycleListener;

  @Mock Repository repository;
  @Mock DelegatingMetaStore metaStore;

  @Before
  public void setUp() throws Exception {
    when( spoon.getRepository() ).thenReturn( repository );
    when( spoon.getMetaStore() ).thenReturn( metaStore );
    when( spoonSupplier.get() ).thenReturn( spoon );
    lifecycleListener = new DataServiceLifecycleListener( spoonSupplier );
  }

  @Test
  public void testLifecycle() throws Exception {
    lifecycleListener.bind( clientService );
    assertThat( ThinConnection.localClient, nullValue() );

    lifecycleListener.unbind( clientService );
    assertThat( ThinConnection.localClient, nullValue() );

    lifecycleListener.onStart( mock( LifeEventHandler.class ) );
    lifecycleListener.onStart( mock( LifeEventHandler.class ) );
    assertThat( ThinConnection.localClient, nullValue() );

    // Spoon instance is not requested until onStart has been called and service is bound
    verify( spoonSupplier, never() ).get();

    lifecycleListener.bind( clientService );
    assertThat( ThinConnection.localClient, sameInstance( clientService ) );

    lifecycleListener.onExit( mock( LifeEventHandler.class ) );
    lifecycleListener.onExit( mock( LifeEventHandler.class ) );
    assertThat( ThinConnection.localClient, nullValue() );

    verify( spoonSupplier ).get();
    verifyNoMoreInteractions( spoonSupplier );
  }
}

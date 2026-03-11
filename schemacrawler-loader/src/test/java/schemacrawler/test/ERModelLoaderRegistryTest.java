/*
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2026, Sualeh Fatehi <sualeh@hotmail.com>.
 * All rights reserved.
 * SPDX-License-Identifier: EPL-2.0
 */

package schemacrawler.test;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import schemacrawler.tools.loader.ermodel.ChainedERModelLoader;
import schemacrawler.tools.loader.ermodel.ERModelLoaderRegistry;
import schemacrawler.tools.options.ConfigUtility;
import us.fatehi.utility.property.PropertyName;

public class ERModelLoaderRegistryTest {

  private static final int NUM_LOADERS = 2;

  @Test
  public void chainedERModelLoaders() {
    final ChainedERModelLoader chainedLoaders =
        ERModelLoaderRegistry.getERModelLoaderRegistry()
            .newChainedERModelLoader(ConfigUtility.newConfig());
    assertThat(chainedLoaders.size(), is(NUM_LOADERS));
  }

  @Test
  public void registeredPlugins() {
    final Collection<PropertyName> supportedLoaders =
        ERModelLoaderRegistry.getERModelLoaderRegistry().getRegisteredPlugins();
    assertThat(supportedLoaders, hasSize(NUM_LOADERS));
    final List<String> names =
        supportedLoaders.stream().map(PropertyName::getName).collect(toList());
    assertThat(names, containsInAnyOrder("primarymodelloader", "implicitassociationsloader"));
  }

  @Test
  public void name() {
    final ERModelLoaderRegistry registry = ERModelLoaderRegistry.getERModelLoaderRegistry();
    assertThat(registry.getName(), is("ER Model Loaders"));
  }

  /** Reloads the ERModelLoaderRegistry by resetting the singleton and recreating it. */
  private static void reloadRegistry() {
    try {
      // Reset the singleton field
      final Field singletonField =
          ERModelLoaderRegistry.class.getDeclaredField("erModelLoaderRegistrySingleton");
      singletonField.setAccessible(true);
      singletonField.set(null, null);

      // Instantiate a new instance to trigger loading
      final Constructor<ERModelLoaderRegistry> constructor =
          ERModelLoaderRegistry.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
    } catch (final NoSuchFieldException
        | NoSuchMethodException
        | SecurityException
        | InstantiationException
        | IllegalAccessException e) {
      fail(e);
    } catch (final InvocationTargetException e) {
      if (e.getCause() instanceof InternalRuntimeException internalEx) {
        throw internalEx;
      }
      fail(e);
    }
  }
}
